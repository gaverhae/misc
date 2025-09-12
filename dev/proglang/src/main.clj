(ns main
  (:require [clojure.string :as string]
            [instaparse.core :as insta]
            [io.github.gaverhae.clonad :as m :refer [mdo monad]]
            [io.github.gaverhae.vatch :refer [vatch]])
  (:gen-class))

(def parse-string
  (insta/parser
    "S = nl* stmt*
     stmt = indent (def | return | assign | expr | if | else) nl+
     indent = '  '*
     def = <'def'> ws+ identifier ws* <'('> ws* (identifier (ws* <','> ws* identifier)*)? <')'> ws* <':'> ws*
     if = <'if'> ws+ expr ws* <':'>
     else = <'else'> ws* <':'>
     return = <'return'> ws+ expr
     assign = identifier ws* <'='> ws* expr
     <expr> = (atom | sum | product | app | equal) ws*
     app = (identifier | app) ws* <'('> ws* (expr (ws* <','> ws* expr)*)? ws* <')'>
     <atom> = int | pexpr | identifier | bool | app
     equal = atom ws* <'=='> ws* atom
     <pexpr> = <'('> ws* expr ws* <')'>
     sum = (atom | product) (ws* <'+'> ws* (atom | product))+
     product = atom (ws* <'*'> ws* atom)+
     identifier = #'[a-zA-Z_][a-zA-Z0-9_]*'
     int = #'-?\\d+'
     bool = 'True' | 'False'
     <nl> = <'\n'>
     <ws> = <' '>"))

(defn parse-blocks
  [current-indent stmts]
  (if (empty? stmts)
    []
    (let [[[_ indent node] & stmts] stmts]
      (assert (= indent current-indent))
      (case (first node)
        :def (let [[fn-name & arg-names] (map second (rest node))
                   [block cont] (split-with (fn [[_ i _]] (> i current-indent))
                                            stmts)
                   block-indent (-> block first second)]
               (assert (= block-indent (inc current-indent)))
               (cons [:def fn-name arg-names (parse-blocks block-indent block)]
                     (parse-blocks current-indent cont)))
        :if (let [[_ expr] node
                  [block-if cont] (split-with (fn [[_ i _]] (> i current-indent))
                                              stmts)
                  block-indent (-> block-if first second)]
              (assert (= block-indent (inc current-indent)))
              (if (= [:stmt current-indent [:else]] (first cont))
                (let [[block-else cont] (split-with (fn [[_ i _]] (> i current-indent))
                                                    (rest cont))
                      block-indent (-> block-if first second)]
                  (assert (= block-indent (inc current-indent)))
                  (cons [:if
                         expr
                         (parse-blocks block-indent block-if)
                         (parse-blocks block-indent block-else)]
                        (parse-blocks current-indent cont)))
                (cons [:if expr (parse-blocks block-indent block-if)]
                      (parse-blocks current-indent cont))))
        :else (throw (ex-info "Unmatched else."))
        (cons node (parse-blocks current-indent stmts))))))

(defn parse
  [s]
  (->> (parse-string s)
       (insta/transform {:stmt (fn [i s] [:stmt (dec (count i)) s])})
       rest
       (parse-blocks 0)
       (cons :S)))

(def mt-q clojure.lang.PersistentQueue/EMPTY)

(defn init-m
  []
  {:thread-id 0
   :done-threads {}
   :next-addr 3
   :next-thread-id 1
   :default-env {"print" 0
                 "start_t" 1
                 "wait_t" 2}
   :mem {0 [:fn ["s"] [:S [:return [:print [:identifier "s"]]]] {}]
         1 [:fn ["f"] [:S [:return [:start_t [:identifier "f"]]]] {}]
         2 [:fn ["t"] [:S [:return [:wait_t [:identifier "t"]]]] {}]}})

(def ^:dynamic +enable-gc+ false)

(defn run-gc
  [t m]
  (if +enable-gc+
    (let [live-mem (loop [envs-to-check (:stack t)
                          mem-to-check []
                          mem-checked #{}]
                     (cond (and (empty? envs-to-check)
                                (empty? mem-to-check)) mem-checked
                           (empty? mem-to-check)
                           (let [[e & envs-to-check] envs-to-check
                                 envs-to-check (if-let [p (::parent e)]
                                                 (conj envs-to-check p)
                                                 envs-to-check)
                                 e (dissoc e ::parent)]
                             (recur envs-to-check (vals e) mem-checked))
                           :else
                           (let [[t & mem-to-check] mem-to-check]
                             (if (mem-checked t)
                               (recur envs-to-check mem-to-check mem-checked)
                               (let [mem-checked (conj mem-checked t)]
                                 (vatch (get (:mem m) t)
                                   [:int _] (recur envs-to-check mem-to-check mem-checked)
                                   [:bool _] (recur envs-to-check mem-to-check mem-checked)
                                   [:fn args body captured-env] (recur (conj envs-to-check captured-env)
                                                                       mem-to-check mem-checked)))))))]
      (update m :mem select-keys live-mem))
    m))

(defn init-thread
  [m]
  [{:id (:next-thread-id m)
    :env (:default-env m)
    :stack []}
   (update m :next-thread-id inc)])

(defn mrun-envs
  ([mv] (let [m (init-m)
              [t m] (init-thread m)]
          (mrun-envs mv t m)))
  ([mv t m]
   (vatch mv
     [:pure v] [v t m]
     [:bind mv f] (let [[v t m] (mrun-envs mv t m)]
                    (mrun-envs (f v) t m))
     [:assert bool msg] [(if bool :m/continue :m/stop) t m]
     [:add-to-env n v] (if-let [addr (get (:env t) n)]
                         [nil t (update m :mem assoc addr v)]
                         (let [addr (:next-addr m)]
                           [nil
                            (-> t (update :env assoc n addr))
                            (-> m
                                (update :next-addr inc)
                                (update :mem assoc addr v))]))
     [:get-from-env n] [(loop [env (:env t)]
                          (if (nil? env)
                            (throw (ex-info "Name not found." {:name n}))
                            (if-let [addr (get env n)]
                              (get (:mem m) addr)
                              (recur (::parent env)))))
                        t m]
     [:get-env] [(:env t) t m]
     [:push-env base] [nil (-> t
                               (update :stack conj (:env t))
                               (assoc :env {::parent base}))
                       m]
     [:pop-env] [nil
                 (-> t
                     (assoc :env (peek (:stack t)))
                     (update :stack pop))
                 (run-gc t m)]
     [:print v] (do (println (second v))
                    [[:int 0] t m]))))

(defn all-numbers?
  [vs]
  (every? (fn [[tag value]] (= :int tag)) vs))

(defn m-eval
  [expr]
  (vatch expr
    [:int s] [:pure [:int (parse-long s)]]
    [:bool s] (case s
                "True" [:pure [:bool true]]
                "False" [:pure [:bool false]])
    [:sum & args] (monad
                    args :<< (m/m-seq (map m-eval args))
                    [:assert (all-numbers? args) "Tried to add non-numeric values."]
                    [:pure [:int (reduce + 0 (map second args))]])
    [:product & args] (monad
                        args :<< (m/m-seq (map m-eval args))
                        [:assert (all-numbers? args) "Tried to multiply non-numeric values."]
                        [:pure [:int (reduce * 1 (map second args))]])
    [:assign [_ n] v] (monad
                        v :<< (m-eval v)
                        [:add-to-env n v])
    [:def fn-name args body] (monad
                               [:add-to-env fn-name nil]
                               env :<< [:get-env]
                               [:add-to-env fn-name [:fn args (cons :S body) env]])
    [:app f & args] (mdo [[tag params body captured-env] (m-eval f)
                          _ [:assert (= :fn tag) "Tried to apply a non-function value."]
                          args (m/m-seq (map m-eval args))
                          _ [:push-env captured-env ]
                          _ (m/m-seq (map (fn [p a] [:add-to-env p a]) params args))
                          [ret? v] (m-eval body)
                          _ [:assert (= :return ret?) "Function ended without a return."]
                          _ [:pop-env]
                          _ [:pure v]])
    [:return expr] (monad
                     r :<< (m-eval expr)
                     [:pure [:return r]])
    [:identifier n] [:get-from-env n]
    [:equal left right] (monad
                          left :<< (m-eval left)
                          right :<< (m-eval right)
                          [:pure [:bool (= left right)]])
    [:if condition if-true & [if-false]] (monad
                                           condition :<< (m-eval condition)
                                           (if (contains? #{[:bool false] [:int 0]} condition)
                                             (m-eval (cons :S if-false))
                                             (m-eval (cons :S if-true))))
    [:print expr] (monad
                    v :<< (m-eval expr)
                    [:print v])
    [:S] [:pure nil]
    [:S head] (m-eval head)
    [:S head & tail] (mdo [[ret? v] (m-eval head)
                           _ (if (= :return ret?)
                               [:pure [:return v]]
                               (m-eval (cons :S tail)))])))

(defn eval-pl
  [expr]
  (mrun-envs (m-eval expr)))

(defn shell
  []
  (loop [[t m] (init-thread (init-m))
         prev-lines ""
         multi-line? false]
    (if multi-line?
      (print "...")
      (print ">  "))
    (flush)
    (let [entry (read-line)
          line (str entry "\n")]
      (cond (or (= entry "quit") (= entry nil))
            (println "Bye!")
            (= entry ":env")
            (do (prn (:env t))
                (recur [t m] "" false))
            (= entry ":mem")
            (do (printf "{:next-addr %d,\n :mem %s\n"
                        (:next-addr m)
                        (if (< (count (:mem m)) 10)
                          (pr-str (:mem m))
                          (format "<:count %d, :sample %s>}"
                                  (count (:mem m))
                                  (->> m :mem seq shuffle (take 10) (into {})))))
                (recur [t m] "" false))
            (and (not multi-line?) (not (string/ends-with? (string/trim line) ":")))
            (let [[v t m] (mrun-envs (m-eval (parse line)) t m)]
              (println "=> " v)
              (recur [t m] "" false))
            (and (not multi-line?) (string/ends-with? (string/trim line) ":"))
            (recur [t m] line true)
            (and multi-line? (= line "\n"))
            (let [[v t m] (mrun-envs (m-eval (parse prev-lines)) t m)]
              (println "=> " v)
              (recur [t m] "" false))
            multi-line?
            (recur [t m] (str prev-lines line) true)
            :else "Unsupported sequence"))))

(defn run-file
  [file]
  (let [[res state] (eval-pl (parse (slurp file)))]
    res))

(defn usage
  []
  (println "Usage: proglang [file]")
  (println)
  (println "\tIf file is provided, runs it. Otherwise, starts a shell."))

(defn -main
  [& args]
  (case (count args)
    0 (shell)
    1 (println (run-file (first args)))
    (usage)))

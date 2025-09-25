(ns main
  (:require [clojure.string :as string]
            [instaparse.core :as insta]
            [io.github.gaverhae.clonad :as m :refer [m-let]]
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

(def mt-q clojure.lang.PersistentQueue/EMPTY)

(defn init-m
  []
  {:done-threads {}
   :threads mt-q
   :next-addr 3
   :next-thread-id 0
   :default-env {"print" 0
                 "start_t" 1
                 "wait_t" 2}
   :mem {0 [:fn ["s"] [:S [:return [:print [:identifier "s"]]]] {}]
         1 [:fn ["f"] [:S [:return [:start_t [:identifier "f"]]]] {}]
         2 [:fn ["t"] [:S [:return [:wait_t [:identifier "t"]]]] {}]}})

(defn add-thread
  [m mv]
  (-> m
      (update :next-thread-id inc)
      (update :threads conj [mv {:id (:next-thread-id m)
                                 :env (:default-env m)
                                 :stack []}])))

(defn mrun-step
  [mv t m]
  (vatch mv
    [:pure v] [[:pure v] t m]
    [:bind mv f] (vatch mv
                   [:pure v] [(f v) t m]
                   otherwise (let [[mv t m] (mrun-step mv t m)]
                               [[:bind mv f] t m]))
    [:assert bool msg] (if bool
                         [[:pure nil] t m]
                         (throw (ex-info msg {})))
    [:add-to-env n v] (if-let [addr (get (:env t) n)]
                        [[:pure nil] t (update m :mem assoc addr v)]
                        (let [addr (:next-addr m)]
                          [[:pure nil]
                           (-> t (update :env assoc n addr))
                           (-> m
                               (update :next-addr inc)
                               (update :mem assoc addr v))]))
    [:get-from-env n] (loop [env (:env t)]
                        (if (nil? env)
                          (throw (ex-info "Name not found." {:name n}))
                          (if-let [addr (get env n)]
                            [[:pure (get (:mem m) addr)] t m]
                            (recur (::parent env)))))
    [:get-env] [[:pure (:env t)] t m]
    [:push-env base] [[:pure nil]
                      (-> t
                          (update :stack conj (:env t))
                          (assoc :env {::parent base}))
                      m]
    [:pop-env] [[:pure nil]
                (-> t
                    (assoc :env (peek (:stack t)))
                    (update :stack pop))
                (run-gc t m)]
    [:start-thread mv] (let [id (:next-thread-id m)]
                         [[:pure [:thread-id id]] t (add-thread m mv)])
    [:wait-for-thread t-id] (do (assert (and (vector? t-id)
                                             (= :thread-id (first t-id))
                                             (= 2 (count t-id))
                                             (int? (second t-id))
                                             (< 0 (second t-id) (:next-thread-id m))))
                                (let [id (second t-id)]
                                  (if (contains? (:done-threads m) id)
                                    [[:pure (get-in m [:done-threads id 0])] t m]
                                    [[:wait-for-thread t-id] t m])))
    [:print v] (do (println (second v))
                   [[:pure [:int 0]] t m])))

(defn mrun
  [m]
  (loop [m m]
    (if (empty? (:threads m))
      (let [[v0 t0] (get-in m [:done-threads 0])]
        [v0 t0 m])
      (let [[mv t] (peek (:threads m))
            m (update m :threads pop)
            [mv t m] (mrun-step mv t m)]
        (vatch mv
          [:pure v] (recur (update m :done-threads assoc (:id t) [v t]))
          otherwise (recur (update m :threads conj [mv t])))))))

(defn mrun-envs
  [mv]
  (mrun (add-thread (init-m) mv)))

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
    [:fn args body env] [:pure [:fn args body env]]
    [:sum & args] (m-let
                    [args (m/m-seq (map m-eval args))
                     _ [:assert (all-numbers? args) "Tried to add non-numeric values."]]
                    [:pure [:int (reduce + 0 (map second args))]])
    [:product & args] (m-let
                        [args (m/m-seq (map m-eval args))
                         _ [:assert (all-numbers? args) "Tried to multiply non-numeric values."]]
                        [:pure [:int (reduce * 1 (map second args))]])
    [:assign [_ n] v] (m-let
                        [v (m-eval v)]
                        [:add-to-env n v])
    [:def fn-name args body] (m-let
                               [_ [:add-to-env fn-name nil]
                                env [:get-env]]
                               [:add-to-env fn-name [:fn args (cons :S body) env]])
    [:app f & args] (m-let
                      [[tag params body captured-env] (m-eval f)
                       _ [:assert (= :fn tag) "Tried to apply a non-function value."]
                       args (m/m-seq (map m-eval args))
                       _ [:push-env captured-env ]
                       _ (m/m-seq (map (fn [p a] [:add-to-env p a]) params args))
                       [ret? v] (m-eval body)
                       _ [:assert (= :return ret?) "Function ended without a return."]
                       _ [:pop-env]]
                       [:pure v])
    [:return expr] (m-let
                     [r (m-eval expr)]
                     [:pure [:return r]])
    [:identifier n] [:get-from-env n]
    [:equal left right] (m-let
                          [left (m-eval left)
                           right (m-eval right)]
                          [:pure [:bool (= left right)]])
    [:if condition if-true & [if-false]] (m-let
                                           [condition (m-eval condition)]
                                           (if (contains? #{[:bool false] [:int 0]} condition)
                                             (m-eval (cons :S if-false))
                                             (m-eval (cons :S if-true))))
    [:print expr] (m-let
                    [v (m-eval expr)]
                    [:print v])
    [:start_t f] (m-let
                   [f (m-eval f)]
                   [:start-thread (m-eval [:app f])])
    [:wait_t arg] (m-let
                    [t (m-eval arg)]
                    [:wait-for-thread t])
    [:S] [:pure nil]
    [:S head] (m-eval head)
    [:S head & tail] (m-let [[ret? v] (m-eval head)]
                       (if (= :return ret?)
                         [:pure [:return v]]
                         (m-eval (cons :S tail))))))

(defn eval-pl
  [expr]
  (mrun-envs (m-eval expr)))

#_(defn shell
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
    #_#_0 (shell)
    1 (println (run-file (first args)))
    (usage)))

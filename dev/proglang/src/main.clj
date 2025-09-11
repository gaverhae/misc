(ns main
  (:require [clojure.string :as string]
            [instaparse.core :as insta]
            [io.github.gaverhae.clonad :as m :refer [mdo]]
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

(defn init-m-state
  []
  {:thread-id 0
   :env {"print" 0}
   :stack []
   :ready-threads mt-q
   :suspended-threads {}
   :done-threads {}
   :next-addr 1
   :next-thread-id 1
   :mem {0 [:fn ["s"]
            [:S
             [:print [:identifier "s"]]
             [:return [:int "0"]]]
            {}]}})

(def ^:dynamic +enable-gc+ false)

(defn run-gc
  [m-state]
  (if +enable-gc+
    (let [{:keys [thread-id env stack]} m-state
          other-thread-stacks (->> (:ready-threads m-state)
                                   (mapcat (fn [[f v [thread-id env stack]]]
                                             (conj stack env))))
          live-mem (loop [envs-to-check (concat stack other-thread-stacks)
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
                                 (vatch (get (:mem m-state) t)
                                   [:int _] (recur envs-to-check mem-to-check mem-checked)
                                   [:bool _] (recur envs-to-check mem-to-check mem-checked)
                                   [:fn args body captured-env]
                                   (recur (conj envs-to-check captured-env)
                                          mem-to-check mem-checked)))))))]
      (update m-state :mem select-keys live-mem))
    m-state))

(defn mrun-envs
  ([mv] (mrun-envs mv (init-m-state)))
  ([mv m-state]
   (vatch mv
     [:pure v] [v m-state]
     [:bind mv f] (let [[v m-state] (mrun-envs mv m-state)]
                    (cond (= v :m/stop)
                          :m/error
                          (and (seq? v) (= 2 (count v)) (= :m/thread-finished (first v)))
                          (let [{:keys [thread-id env stack]} m-state
                                m-state (update m-state :done-threads assoc thread-id [(second v) env stack])]
                            (if (empty? (:ready-threads m-state))
                              (let [[result env stack] (get (:done-threads m-state) 0)]
                                [result (assoc m-state :env env :stack stack)])
                              (let [[f v [thread-id env stack]] (peek (:ready-threads m-state))]
                                (mrun-envs (f v) (-> m-state
                                                     (assoc :thread-id thread-id :env env :stack stack)
                                                     (update :ready-threads pop))))))
                          (empty? (:ready-threads m-state))
                          (mrun-envs (f v) m-state)
                          :else
                          (let [parked-thread [f v [(:thread-id m-state) (:env m-state) (:stack m-state)]]
                                [f v [thread-id env stack]] (peek (:ready-threads m-state))
                                m-state (update m-state :ready-threads (fn [r] (conj (pop r) parked-thread)))]
                            (mrun-envs (f v) m-state))))
     [:start-thread f v] (let [new-thread-id (:next-thread-id m-state)
                               env (:env m-state)
                               m-state (-> m-state
                                           (update :next-thread-id inc)
                                           (update :ready-threads conj [f v [new-thread-id env []]]))]
                           [[:thread-id new-thread-id] m-state])
     [:wait-for-thread id] (if (contains? (:done-threads m-state) id)
                             (let [res (get (:done-threads m-state) id)]
                               [res m-state])
                             (let [[f v [id env stack]] (peek (:ready-threads m-state))
                                   current-thread [(:thread-id m-state) (:env m-state) (:stack m-state)]
                                   m-state (-> m-state
                                               (assoc :thread-id id :env env :stack stack)
                                               (update :ready-threads conj current-thread))]
                               (mrun-envs (f v) m-state)))
     [:assert bool msg] [(if bool :m/continue :m/stop) m-state]
     [:add-to-env n v] (if-let [addr (get (:env m-state) n)]
                         [nil (update m-state :mem assoc addr v)]
                         (let [addr (:next-addr m-state)]
                           [nil (-> m-state
                                    (update :next-addr inc)
                                    (update :mem assoc addr v)
                                    (update :env assoc n addr))]))
     [:get-from-env n] [(loop [env (:env m-state)]
                          (if (nil? env)
                            :m/stop
                            (if-let [addr (get env n)]
                              (get (:mem m-state) addr)
                              (recur (::parent env)))))
                        m-state]
     [:get-env] [(:env m-state) m-state]
     [:push-env base] (let [{:keys [env stack]} m-state]
                        [nil (-> m-state
                                 (assoc :env {::parent base})
                                 (update :stack conj env))])
     [:pop-env] (let [{:keys [thread-id env stack]} m-state]
                  [nil (-> m-state
                           run-gc
                           (assoc :env (peek (:stack m-state)))
                           (update :stack pop))])
     [:print v] (do (println (second v))
                    [nil m-state]))))

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
    [:sum & args] (mdo [args (m/m-seq (map m-eval args))
                        _ [:assert (all-numbers? args) "Tried to add non-numeric values."]
                        _ [:pure [:int (reduce + 0 (map second args))]]])
    [:product & args] (mdo [args (m/m-seq (map m-eval args))
                            _ [:assert (all-numbers? args) "Tried to multiply non-numeric values."]
                            _ [:pure [:int (reduce * 1 (map second args))]]])
    [:assign [_ n] v] (mdo [v (m-eval v)
                            _ [:add-to-env n v]])
    [:def fn-name args body] (mdo [_ [:add-to-env fn-name nil]
                                   env [:get-env]
                                   _ [:add-to-env fn-name [:fn args (cons :S body) env]]])
    [:app f & args] (mdo [[tag params body captured-env] (m-eval f)
                          _ [:assert (= :fn tag) "Tried to apply a non-function value."]
                          args (m/m-seq (map m-eval args))
                          _ [:push-env captured-env ]
                          _ (m/m-seq (map (fn [p a] [:add-to-env p a]) params args))
                          [ret? v] (m-eval body)
                          _ [:assert (= :return ret?) "Function ended without a return."]
                          _ [:pop-env]
                          _ [:pure v]])
    [:return expr] (mdo [r (m-eval expr)
                         _ [:pure [:return r]]])
    [:identifier n] (mdo [_ [:get-from-env n]])
    [:equal left right] (mdo [left (m-eval left)
                              right (m-eval right)
                              _ [:pure [:bool (= left right)]]])
    [:if condition if-true & [if-false]] (mdo [condition (m-eval condition)
                                               _ (if (contains? #{[:bool false] [:int 0]} condition)
                                                   (m-eval (cons :S if-false))
                                                   (m-eval (cons :S if-true)))])
    [:print expr] (mdo [v (m-eval expr)
                        _ [:print v]])
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
  (loop [m-state (init-m-state)
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
            (do (prn (:env m-state))
                (recur m-state "" false))
            (= entry ":mem")
            (do (printf "{:next-addr %d,\n :mem %s\n"
                        (:next-addr m-state)
                        (if (< (count (:mem m-state)) 10)
                          (pr-str (:mem m-state))
                          (format "<:count %d, :sample %s>}"
                                  (count (:mem m-state))
                                  (->> m-state :mem seq shuffle (take 10) (into {})))))
                (recur m-state "" false))
            (and (not multi-line?) (not (string/ends-with? (string/trim line) ":")))
            (let [[v m-state] (mrun-envs (m-eval (parse line)) m-state)]
              (println "=> " v)
              (recur m-state "" false))
            (and (not multi-line?) (string/ends-with? (string/trim line) ":"))
            (recur m-state line true)
            (and multi-line? (= line "\n"))
            (let [[v state] (mrun-envs (m-eval (parse prev-lines)) m-state)]
              (println "=> " v)
              (recur m-state "" false))
            multi-line?
            (recur m-state (str prev-lines line) true)
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

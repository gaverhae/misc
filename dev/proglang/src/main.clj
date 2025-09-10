(ns main
  (:require [clojure.string :as string]
            [instaparse.core :as insta]
            [io.github.gaverhae.clonad :refer [mdo match]])
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

(defn init-mem
  []
  {:next-addr 1
   :next-thread 1
   :mem {0 [:fn ["s"]
            [:S
             [:print [:identifier "s"]]
             [:return [:int "0"]]]
            {}]}})

(defn init-env
  []
  {"print" 0})

(def ^:dynamic +enable-gc+ false)

(def mt-q clojure.lang.PersistentQueue/EMPTY)

(defn mrun-envs
  ([mv] (mrun-envs mv [0 (init-env) []] (init-mem) mt-q {}))
  ([mv current-thread mem ready-threads done-threads]
   (match mv
     [:pure v] [v current-thread mem ready-threads done-threads]
     [:bind mv f] (let [[v current-thread mem ready-threads done-threads] (mrun-envs mv current-thread mem ready-threads done-threads)]
                    (cond (= v :m/stop)
                          :m/error
                          (and (seq? v) (= 2 (count v)) (= :m/thread-finished (first v)))
                          (let [[thread-id env stack] current-thread
                                done-threads (assoc done-threads thread-id [(second v) env stack])]
                            (if (empty? ready-threads)
                              (let [[result env stack] (get done-threads 0)]
                                [result [env stack] mem ready-threads done-threads])
                              (let [[f v current-thread] (peek ready-threads)]
                                (mrun-envs (f v) current-thread mem (pop ready-threads) done-threads))))
                          (empty? ready-threads)
                          (mrun-envs (f v) current-thread mem ready-threads done-threads)
                          :else
                          (let [parked-thread [f v current-thread]
                                [f v current-thread] (peek ready-threads)
                                ready-threads (conj (pop ready-threads) parked-thread)]
                            (mrun-envs (f v) current-thread mem ready-threads done-threads))))
     [:assert bool msg] [(if bool :m/continue :m/stop) current-thread mem ready-threads done-threads]
     [:add-to-env n v] (let [[thread-id env stack] current-thread]
                         (if-let [addr (get env n)]
                           [nil [thread-id env stack] (update mem :mem assoc addr v) ready-threads done-threads]
                           (let [addr (:next-addr mem)]
                             [nil
                              [thread-id (assoc env n addr) stack]
                              (-> mem
                                  (update :mem assoc addr v)
                                  (update :next-addr inc))
                              ready-threads
                              done-threads])))
     [:get-from-env n] (let [[thread-id env stack] current-thread]
                         [(loop [env env]
                            (if (nil? env)
                              nil
                              (if-let [addr (get env n)]
                                (get (:mem mem) addr)
                                (recur (::parent env)))))
                          [thread-id env stack]
                          mem
                          ready-threads
                          done-threads])
     [:get-env] (let [[thread-id env stack] current-thread]
                  [env [thread-id env stack] mem ready-threads done-threads])
     [:push-env base] (let [[thread-id env stack] current-thread]
                        [nil [thread-id {::parent base} (conj stack env)] mem ready-threads done-threads])
     [:pop-env] (let [[thread-id env stack] current-thread]
                  (if +enable-gc+
                    (let [other-thread-stacks (->> ready-threads
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
                                                 (match (get (:mem mem) t)
                                                   [:int _] (recur envs-to-check mem-to-check mem-checked)
                                                   [:bool _] (recur envs-to-check mem-to-check mem-checked)
                                                   [:fn args body captured-env]
                                                   (recur (conj envs-to-check captured-env)
                                                          mem-to-check mem-checked)))))))]
                      [nil [thread-id (peek stack) (pop stack)] (update mem :mem select-keys live-mem) ready-threads done-threads])
                    [nil [thread-id (peek stack) (pop stack)] mem ready-threads done-threads]))
     [:print v] (do (println (second v))
                    [nil current-thread mem ready-threads done-threads]))))

(defn sequenceM
  "[m v] -> m [v]"
  [mvs]
  (if (empty? mvs)
    [:pure ()]
    (mdo [v (first mvs)
          r (sequenceM (rest mvs))
          _ [:pure (cons v r)]])))

(defn all-numbers?
  [vs]
  (every? (fn [[tag value]] (= :int tag)) vs))

(defn m-eval
  [expr]
  (match expr
    [:int s] [:pure [:int (parse-long s)]]
    [:bool s] (case s
                "True" [:pure [:bool true]]
                "False" [:pure [:bool false]])
    [:sum & args] (mdo [args (sequenceM (map m-eval args))
                        _ [:assert (all-numbers? args) "Tried to add non-numeric values."]
                        _ [:pure [:int (reduce + 0 (map second args))]]])
    [:product & args] (mdo [args (sequenceM (map m-eval args))
                            _ [:assert (all-numbers? args) "Tried to multiply non-numeric values."]
                            _ [:pure [:int (reduce * 1 (map second args))]]])
    [:assign [_ n] v] (mdo [v (m-eval v)
                            _ [:add-to-env n v]])
    [:def fn-name args body] (mdo [_ [:add-to-env fn-name nil]
                                   env [:get-env]
                                   _ [:add-to-env fn-name [:fn args (cons :S body) env]]])
    [:app f & args] (mdo [[tag params body captured-env] (m-eval f)
                          _ [:assert (= :fn tag) "Tried to apply a non-function value."]
                          args (sequenceM (map m-eval args))
                          _ [:push-env captured-env ]
                          _ (sequenceM (map (fn [p a] [:add-to-env p a]) params args))
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
    [:if condition if-true if-false] (mdo [condition (m-eval condition)
                                           _ (if (contains? #{[:bool false] [:int 0]} condition)
                                               (m-eval (cons :S if-false))
                                               (m-eval (cons :S if-true)))])
    [:print expr] (mdo [v (m-eval expr)
                        _ [:print v]])
    [:S head & tail] (cond (nil? head) [:pure nil]
                           (empty? tail) (m-eval head)
                           :else (mdo [[ret? v] (m-eval head)
                                       _ (if (= :return ret?)
                                           [:pure [:return v]]
                                           (m-eval (cons :S tail)))]))))

(defn eval-pl
  [expr]
  (mrun-envs (m-eval expr)))

(defn shell
  []
  (loop [current-thread [0 (init-env) []]
         mem (init-mem)
         ready-threads mt-q
         done-threads {}
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
            (do (prn current-thread)
                (recur current-thread mem ready-threads done-threads "" false))
            (= entry ":mem")
            (do (printf "{:next-addr %d,\n :mem %s\n"
                        (:next-addr mem)
                        (if (< (count (:mem mem)) 10)
                          (pr-str (:mem mem))
                          (format "<:count %d, :sample %s>}"
                                  (count (:mem mem))
                                  (->> mem :mem seq shuffle (take 10) (into {})))))
                (recur current-thread mem ready-threads done-threads "" false))
            (and (not multi-line?) (not (string/ends-with? (string/trim line) ":")))
            (let [[v current-thread mem ready-threads done-threads] (mrun-envs (m-eval (parse line)) current-thread mem ready-threads done-threads)]
              (println "=> " v)
              (recur current-thread mem ready-threads done-threads "" false))
            (and (not multi-line?) (string/ends-with? (string/trim line) ":"))
            (recur current-thread mem ready-threads done-threads line true)
            (and multi-line? (= line "\n"))
            (let [[v current-thread mem ready-threads done-threads] (mrun-envs (m-eval (parse prev-lines)) current-thread mem ready-threads done-threads)]
              (println "=> " v)
              (recur current-thread mem ready-threads done-threads "" false))
            multi-line?
            (recur current-thread mem ready-threads done-threads (str prev-lines line) true)
            :else "Unsupported sequence"))))

(defn run-file
  [file]
  (let [[res current-thread mem ready-threads done-threads] (eval-pl (parse (slurp file)))]
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

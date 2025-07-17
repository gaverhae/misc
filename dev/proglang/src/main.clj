(ns main
  (:require [instaparse.core :as insta])
  (:gen-class))

(def parse-string
  (insta/parser
    "S = nl* stmt*
     stmt = indent (def | return | assign | expr) nl+
     indent = '  '*
     def = <'def'> ws identifier ws <'('> (identifier (ws <','> ws identifier)*)? <')'> ws <':'> ws
     return = <'return'> ws expr
     assign = identifier ws <'='> ws expr
     <expr> = (atom | sum | product | app) ws
     app = (identifier | app) ws <'('> ws (expr (ws , ws expr)*)? ws <')'>
     <atom> = int | pexpr | identifier
     <pexpr> = <'('> ws expr ws <')'>
     sum = (atom | product) (ws <'+'> ws (atom | product))+
     product = atom (ws <'*'> ws atom)+
     identifier = #'[a-zA-Z_][a-zA-Z0-9_]*'
     int = #'\\d+'
     <nl> = <'\n'>
     <ws> = <' '*>"))

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
        (cons node (parse-blocks current-indent stmts))))))

(defn parse
  [s]
  (->> (parse-string s)
       (insta/transform {:stmt (fn [i s] [:stmt (dec (count i)) s])})
       rest
       (parse-blocks 0)
       (cons :S)))

(declare eval-pl)

(defn eval-args
  [env mem args]
  (reduce (fn [[env mem vs] n]
            (let [[env mem v] (eval-pl env mem n)]
              [env mem (conj vs v)]))
          [env mem []]
          args))

(defn add-env
  [env mem n v]
  (if-let [at (get env n)]
    (do (reset! at v)
        [env mem nil])
    [(assoc env n (atom v)) mem nil]))

(defn get-value
  [env mem n]
  (loop [e env]
    (if-let [v (get e n)]
      [env mem @v]
      (when-let [p (get e ::parent)]
        (recur p)))))

(defn create-env
  [env mem]
  [{::parent env} mem nil])

(defn init-mem
  []
  {})

(defn eval-pl
  [env mem node]
  (case (first node)
    :int (let [[_ i] node]
           [env mem [:int (parse-long i)]])
    :sum (let [[_ & terms] node
               [env mem vs] (eval-args env mem terms)]
           (assert (every? (fn [[t v]] (= t :int)) vs))
           [env mem [:int (reduce + 0 (map second vs))]])
    :product (let [[_ & factors] node
                   [env mem vs] (eval-args env mem factors)]
               (assert (every? (fn [[t v]] (= t :int)) vs))
               [env mem [:int (reduce * 1 (map second vs))]])
    :assign (let [[_ [_ n] expr] node
                  [env mem v] (eval-pl env mem expr)]
              (add-env env mem n v))
    :def (let [[_ fn-name args body] node
               ;; for recursion to work, fn needs to know about itself in its own environment
               [env mem _] (add-env env mem fn-name nil)
               [env mem _] (add-env env mem fn-name [:fn args (cons :S body) env])]
           [env mem nil])
    :app (let [[_ f & args] node
               [env mem evaled-f] (eval-pl env mem f)
               _ (assert (= :fn (first evaled-f)))
               [_ params body captured-env] evaled-f
               [env mem evaled-args] (eval-args env mem args)
               [closure-env mem _] (reduce (fn [[env mem _] [n v]]
                                             (add-env env mem n v))
                                           (create-env captured-env mem)
                                           (map vector params evaled-args))
               [closure-env mem app-val] (eval-pl closure-env mem body)]
           [env mem app-val])
    :return (let [[_ expr] node]
              (eval-pl env mem expr))
    :identifier (let [[_ n] node]
                  (get-value env mem n))
    :S (let [[_ & stmts] node]
         (reduce (fn [[env mem v] stmt]
                   (let [[env mem v] (eval-pl env mem stmt)]
                     [env mem v]))
                 [env mem nil]
                 stmts))))

(defn shell
  []
  :todo
  #_(loop [env {}]
    (print "> ")
    (flush)
    (let [line (read-line)]
      (when (and (not= line "quit")
                 (not= line nil))
        (let [[env v] (eval-pl env (init-mem) (parse line))]
          (println "    => " (pr-str v))
          (recur env))))))

(defn run-file
  [file]
  (let [[env mem res] (eval-pl {} (init-mem) (parse (slurp file)))]
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

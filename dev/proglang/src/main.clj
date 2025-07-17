(ns main
  (:require [instaparse.core :as insta])
  (:gen-class))

(def parse-string
  (insta/parser
    "S = nl* stmt*
     stmt = indent (def | return | assign | expr) nl+
     indent = ' '*
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
  [env args]
  (reduce (fn [[env vs] n]
            (let [[env v] (eval-pl env n)]
              [env (conj vs v)]))
          [env []]
          args))

(defn add-env
  [env n v]
  (if-let [at (get env n)]
    (do (reset! at v)
        env)
    (assoc env n (atom v))))

(defn eval-pl
  [env node]
  (case (first node)
    :int (let [[_ i] node]
           [env [:int (parse-long i)]])
    :sum (let [[_ & terms] node
               [env vs] (eval-args env terms)]
           (assert (every? (fn [[t v]] (= t :int)) vs))
           [env [:int (reduce + 0 (map second vs))]])
    :product (let [[_ & factors] node
                   [env vs] (eval-args env factors)]
               (assert (every? (fn [[t v]] (= t :int)) vs))
               [env [:int (reduce * 1 (map second vs))]])
    :assign (let [[_ [_ n] expr] node
                  [env v] (eval-pl env expr)]
              [(add-env env n v) nil])
    :def (let [[_ fn-name args body] node
               ;; for recursion to work, fn needs to know about itself in its own environment
               env (add-env env fn-name nil)
               env (add-env env fn-name [:fn args (cons :S body) env])]
           [env nil])
    :app (let [[_ f & args] node
               [env evaled-f] (eval-pl env f)
               _ (assert (= :fn (first evaled-f)))
               [_ params body captured-env] evaled-f
               [env evaled-args] (eval-args env args)
               [closure-env app-val] (eval-pl (reduce (fn [env [n v]]
                                                        (add-env env n v))
                                                      {::parent captured-env}
                                                      (map vector params evaled-args))
                                              body)]
           [env app-val])
    :return (let [[_ expr] node]
              (eval-pl env expr))
    :identifier (let [[_ n] node]
                  [env (loop [env env]
                         (if-let [v (get env n)]
                           @v
                           (when-let [p (get env ::parent)]
                             (recur p))))])
    :S (let [[_ & stmts] node]
         (reduce (fn [[env v] stmt]
                   (let [[env v] (eval-pl env stmt)]
                     [env v]))
                 [env nil]
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
        (let [[env v] (eval-pl env (parse line))]
          (println "    => " (pr-str v))
          (recur env))))))

(defn run-file
  [file]
  (second (second (eval-pl {} (parse (slurp file))))))

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

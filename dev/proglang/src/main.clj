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
     <expr> = (atom | sum | product) ws
     <atom> = int | pexpr | identifier
     <pexpr> = <'('> ws expr ws <')'>
     sum = (atom | product) (ws <'+'> ws (atom | product))+
     product = atom (ws <'*'> ws atom)+
     identifier = #'[a-zA-Z_][a-zA-Z0-9_]*'
     int = #'\\d+'
     <nl> = <'\n'>
     <ws> = <' '*>"))

(defn parse-blocks
  [stmts current-indent]
  (if (empty? stmts)
    []
    (let [[[_ indent node] & stmts] stmts]
      (assert (= indent current-indent))
      (case (first node)
        :def (let [[fn-name & arg-names] (map second (rest node))
                   [block cont] (split-with (fn [[_ i _]] (> i current-indent))
                                            stmts)
                   block-indent (-> block first second)]
               (cons [:def fn-name arg-names (parse-blocks block block-indent)]
                     (parse-blocks cont current-indent)))
        (cons node (parse-blocks stmts current-indent))))))

(defn parse
  [s]
  (insta/transform
    {:stmt (fn [i s] [:stmt (dec (count i)) s])}
    (parse-string s)))

(defn eval-pl
  [env node]
  (case (first node)
    :stmt (let [[_ _ s] node]
            (eval-pl env s))
    :int (let [[_ i] node]
           [env (parse-long i)])
    :sum (let [[_ & terms] node
               [env vs] (reduce (fn [[env vs] n]
                                  (let [[env v] (eval-pl env n)]
                                    [env (conj vs v)]))
                                [env []]
                                terms)]
           [env (reduce + 0 vs)])
    :product (let [[_ & factors] node
                   [env vs] (reduce (fn [[env vs] n]
                                      (let [[env v] (eval-pl env n)]
                                        [env (conj vs v)]))
                                    [env []]
                                    factors)]
               [env (reduce * 1 vs)])
    :assign (let [[_ [_ n] expr] node
                  [env v] (eval-pl env expr)]
              [(assoc env n v) nil])
    :identifier (let [[_ n] node]
                  [env (get env n)])
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
  (second (eval-pl {} (parse (slurp file)))))

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

(ns main
  (:require [instaparse.core :as insta])
  (:gen-class))

(def parse
  (insta/parser
    "S = nl* stmt (nl+ stmt)* nl*
     <stmt> = assign | expr
     assign = identifier ws <'='> ws expr
     <expr> = ws (atom | sum | product) ws
     <atom> = int | pexpr | identifier
     <pexpr> = <'('> ws expr ws <')'>
     sum = (atom | product) (ws <'+'> ws (atom | product))+
     product = atom (ws <'*'> ws atom)+
     identifier = #'[a-zA-Z_][a-zA-Z0-9_]*'
     int = #'\\d+'
     <nl> = <'\n'>
     <ws> = <' '*>"))

(defn eval-pl
  ([ast] (eval-pl ast {}))
  ([node env]
   (case (first node)
     :int (let [[_ i] node]
            [env (parse-long i)])
     :sum (let [[_ & terms] node
                [env vs] (reduce (fn [[env vs] n]
                                   (let [[env v] (eval-pl n env)]
                                     [env (conj vs v)]))
                                 [env []]
                                 terms)]
            [env (reduce + 0 vs)])
     :product (let [[_ & factors] node
                    [env vs] (reduce (fn [[env vs] n]
                                       (let [[env v] (eval-pl n env)]
                                         [env (conj vs v)]))
                                     [env []]
                                     factors)]
                [env (reduce * 1 vs)])
     :assign (let [[_ [_ n] expr] node
                   [env v] (eval-pl expr env)]
               [(assoc env n v) nil])
     :identifier (let [[_ n] node]
                   [env (get env n)])
     :S (let [[_ & stmts] node]
          (reduce (fn [[env v] stmt]
                    (let [[env v] (eval-pl stmt env)]
                      [env v]))
                  [env nil]
                  stmts)))))

(defn shell
  []
  (loop []
    (print "> ")
    (flush)
    (let [line (read-line)]
      (when (not= line "quit")
        (println "    =>" (eval-pl (parse line)))
        (recur)))))

(defn run-file
  [file]
  (second (eval-pl (parse (slurp file)))))

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

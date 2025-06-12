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

(defn eval-expr
  [string]
  (let [ast (parse string)]
    (insta/transform {:int parse-long
                      :sum (fn [& args] (apply + args))
                      :product (fn [& args] (apply * args))
                      :S (fn [& args] (last args))}
                     ast)))

(defn shell
  []
  (loop []
    (print "> ")
    (flush)
    (let [line (read-line)]
      (when (not= line "quit")
        (println "    =>" (eval-expr line))
        (recur)))))

(defn run-file
  [file]
  (eval-expr (slurp file)))

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

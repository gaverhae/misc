(ns main
  (:require [instaparse.core :as insta])
  (:gen-class))

(def parse
  (insta/parser
    "S = expr
     <expr> = <w> (int | pexpr | sum | product) <w>
     <pexpr> = <'('> <w> expr <w> <')'>
     sum = (int | pexpr | product) (<w> <'+'> <w> (int | pexpr | product))+
     product = (int | pexpr) (<w> <'*'> <w> (int | pexpr))+
     int = #'\\d+'
     w = #'\\s'*"))

(defn eval-expr
  [string]
  (let [ast (parse string)]
    (insta/transform {:int parse-long
                      :sum (fn [& args] (apply + args))
                      :product (fn [& args] (apply * args))
                      :S identity}
                     ast)))

(defn shell
  []
  (loop []
    (print "> ")
    (flush)
    (let [line (read-line)]
      (println "    =>" (eval-expr line))
      (recur))))

(defn run-file
  [file]
  (println "WIP."))

(defn usage
  []
  (println "Usage: proglang [file]")
  (println)
  (println "\tIf file is provided, runs it. Otherwise, starts a shell."))

(defn -main
  [& args]
  (case (count args)
    0 (shell)
    1 (run-file (first args))
    2 (usage)))

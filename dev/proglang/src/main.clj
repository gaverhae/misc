(ns main
  (:require [instaparse.core :as insta])
  (:gen-class))

(def parse
  (insta/parser
    "S = expr
     <expr> = <w> (atom | sum | product) <w>
     <atom> = int | pexpr
     <pexpr> = <'('> <w> expr <w> <')'>
     sum = (atom | product) (<w> <'+'> <w> (atom | product))+
     product = atom (<w> <'*'> <w> atom)+
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

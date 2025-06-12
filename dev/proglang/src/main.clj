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

(defn run
  [opts]
  (println "Hello!")
  (prn opts))

(defn -main
  [& args]
  (println "Hello from main."))

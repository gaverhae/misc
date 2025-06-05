(ns main
  (:require [instaparse.core :as insta]))

(def parse
  (insta/parser
    "S = expr
     <expr> = <w> (int | pexpr | sum | product) <w>
     <pexpr> = <'('> <w> expr <w> <')'>
     sum = (int | pexpr | product) (<w> <'+'> <w> (int | pexpr | product))+
     product = (int | pexpr) (<w> <'*'> <w> (int | pexpr))+
     int = #'\\d+'
     w = #'\\s'*"))

(defn pl-eval
  [string]
  (let [p (parse string)
        t (insta/transform {:int parse-long
                            :sum (fn [& args] (apply + args))
                            :product (fn [& args] (apply * args))
                            :S identity}
                           p)]
    t))

(defn run
  [opts]
  (println "Hello!")
  (prn opts))

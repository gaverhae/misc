(ns main
  (:require [instaparse.core :as insta]))

(def parse
  (insta/parser
    "S = <w> (expr | pexpr) <w>
     pexpr = '(' <w> expr <w> ')'
     expr = term (<w> '+' <w> (term | pexpr))*
     term = factor (<w> '*' <w> (factor | pexpr))*
     factor = num
     num = #'\\d+'
     w = #'\\s'*"))

(defn run
  [opts]
  (println "Hello!")
  (prn opts))

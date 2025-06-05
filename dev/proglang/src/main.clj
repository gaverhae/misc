(ns main
  (:require [instaparse.core :as insta]))

(def parse
  (insta/parser
    "S = term ('+' term)*
     term = factor ('*' factor)*
     factor = num
     num = #'\\d+'"))

(defn run
  [opts]
  (println "Hello!")
  (prn opts))

(ns main
  (:require [instaparse.core :as insta]))

(def parse
  (insta/parser
    "S = num '+' num
     num = #'\\d+'"))

(defn run
  [opts]
  (println "Hello!")
  (prn opts))

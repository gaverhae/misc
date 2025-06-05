(ns main-test
  (:require [main :as s]
            [clojure.test :refer [deftest are]]))

(deftest ploup
  (are [string tree] (= tree (s/parse string))
    "34+123" [:S [:term [:factor [:num "34"]]] "+" [:term [:factor [:num "123"]]]]
    "1+2*3" [:S [:term [:factor [:num "1"]]] "+" [:term [:factor [:num "2"]] "*" [:factor [:num "3"]]]]
    "2*3+1" [:S [:term [:factor [:num "2"]] "*" [:factor [:num "3"]]] "+" [:term [:factor [:num "1"]]]]))

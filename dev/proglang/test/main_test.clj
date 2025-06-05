(ns main-test
  (:require [main :as s]
            [clojure.test :refer [deftest are]]))

(deftest basic-expressions
  (are [string tree] (= tree (s/parse string))
    "34+123" [:S [:expr [:term [:factor [:num "34"]]] "+" [:term [:factor [:num "123"]]]]]
    "1+2*3" [:S [:expr [:term [:factor [:num "1"]]] "+" [:term [:factor [:num "2"]] "*" [:factor [:num "3"]]]]]
    "2*3+1" [:S [:expr [:term [:factor [:num "2"]] "*" [:factor [:num "3"]]] "+" [:term [:factor [:num "1"]]]]]))

(deftest whitespace-ignored
  (are [strings tree] (apply = tree (map s/parse strings))
    ["34+123" " 34 + 123" "34 +   123"] [:S [:expr [:term [:factor [:num "34"]]] "+" [:term [:factor [:num "123"]]]]]
    ["1+2*3" "1 + 2 * 3" "       1+2  *  3"] [:S [:expr [:term [:factor [:num "1"]]] "+" [:term [:factor [:num "2"]] "*" [:factor [:num "3"]]]]]
    ["2*3+1" "2 * 3 + 1"] [:S [:expr [:term [:factor [:num "2"]] "*" [:factor [:num "3"]]] "+" [:term [:factor [:num "1"]]]]]))

(deftest parens
  (are [string tree] (= tree (s/parse string))
    "1 + (2 * 3)" [:S [:expr [:term [:factor [:num "1"]]] "+" [:pexpr "(" [:expr [:term [:factor [:num "2"]] "*" [:factor [:num "3"]]]] ")" ]]]
    ;; this one does not parse
    #_#_
    "(1 + 2) * 3" []
    "(1 + 2 * 3)" [:S [:pexpr "(" [:expr [:term [:factor [:num "1"]]] "+" [:term [:factor [:num "2"]] "*" [:factor [:num "3"]]]] ")" ]]
    ;; this one does not parse
    #_#_
    "(1) + (2 * 3)" []
    ))

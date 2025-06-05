(ns main-test
  (:require [main :as s]
            [clojure.test :refer [deftest are]]))

(deftest basic-expressions
  (are [string tree] (= tree (s/parse string))
    "34+123" [:S [:sum [:int "34"] [:int "123"]]]
    "1+2*3" [:S [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
    "2*3+1" [:S [:sum [:product [:int "2"] [:int "3"]] [:int "1"]]]))

(deftest whitespace-ignored
  (are [strings tree] (apply = tree (map s/parse strings))
    ["34+123" " 34 + 123" "34 +   123"] [:S [:sum [:int "34"] [:int "123"]]]
    ["1+2*3" "1 + 2 * 3" "       1+2  *  3"] [:S [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
    ["2*3+1" "2 * 3 + 1"] [:S [:sum [:product [:int "2"] [:int "3"]] [:int "1"]]]))

(deftest parens
  (are [string tree] (= tree (s/parse string))
    "1 + (2 * 3)" [:S [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
    "(1 + 2) * 3" [:S [:product [:sum [:int "1"] [:int "2"]] [:int "3"]]]
    "(1 + 2 * 3)" [:S [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
    "(1) + (2 * 3)" [:S [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]))

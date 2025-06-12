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

(deftest ast
  (are [strings tree] (= tree (s/parse (->> strings (interpose "\n") (apply str))))
    ["1 + (2 * 3)"] [:S [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
    ["a = 2" "b = 5" "a + b"]
    [:S
     [:assign [:identifier "a"] [:int "2"]]
     [:assign [:identifier "b"] [:int "5"]]
     [:sum [:identifier "a"] [:identifier "b"]]]))

(deftest pl-eval
  (are [string result] (= result (s/eval-expr string))
    "1+2+3" 6
    "(1) + (2 * 3)" 7
    " 1  +  2 * 3 " 7
    "(1  +  2)* 3 " 9))

(deftest multiline-expr
  (are [string result] (= result (s/eval-expr string))
    "4\n1+2+3" 6
    "(1+2)\n(1) + (2 * 3)" 7
    "\n6 * 4 \n\n 1  +  2 * 3 " 7
    "\n\n(1  +  2)* 3 \n" 9))

(deftest files
  (are [path result] (= result (s/run-file (str "test-resources/" path ".pl")))
    "plain-sum" 42
    "parens" 154
    "multi-line" 12))

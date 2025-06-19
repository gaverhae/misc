(ns main-test
  (:require [main :as s]
            [clojure.test :refer [deftest are]]))

(deftest basic-expressions
  (are [string tree] (= tree (first (s/iparse string :start :expr)))
    "34+123" [:sum [:int "34"] [:int "123"]]
    "1+2*3" [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    "2*3+1" [:sum [:product [:int "2"] [:int "3"]] [:int "1"]]))

(deftest whitespace-ignored
  (are [strings tree] (apply = tree (map (fn [s] (first (s/iparse s :start :expr))) strings))
    ["34+123" "34 + 123" "34 +   123"] [:sum [:int "34"] [:int "123"]]
    ["1+2*3" "1 + 2 * 3" "1+2  *  3"] [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    ["2*3+1" "2 * 3 + 1"] [:sum [:product [:int "2"] [:int "3"]] [:int "1"]]))

(deftest parens
  (are [string tree] (= tree (first (s/iparse string :start :expr)))
    "1 + (2 * 3)" [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    "(1 + 2) * 3" [:product [:sum [:int "1"] [:int "2"]] [:int "3"]]
    "(1 + 2 * 3)" [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    "(1) + (2 * 3)" [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]))

(defn ->lines
  [strings]
  (->> strings
       (map (fn [s] (str s "\n")))
       (apply str)))

(deftest ast
  (are [strings tree] (= tree (s/parse (->lines strings)))
    ["1 + (2 * 3)"] [:S [:stmt 0 [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]]
    ["a = 2" "b = 5" "a + b"]
    [:S
     [:stmt 0 [:assign [:identifier "a"] [:int "2"]]]
     [:stmt 0 [:assign [:identifier "b"] [:int "5"]]]
     [:stmt 0 [:sum [:identifier "a"] [:identifier "b"]]]]
    ["def fib(n):"]
    [:S [:stmt 0 [:def [:identifier "fib"] [:identifier "n"]]]]))

(deftest pl-eval
  (are [string result] (= result (second (s/eval-pl {} (s/parse (str string "\n")))))
    "1+2+3" 6
    "(1) + (2 * 3)" 7
    "1  +  2 * 3 " 7
    "(1  +  2)* 3 " 9))

(deftest multiline-expr
  (are [strings tree] (= tree (second (s/eval-pl {} (s/parse (->lines strings)))))
    ["4" "1+2+3"] 6
    ["(1+2)" "(1) + (2 * 3)"] 7
    ["" "6 * 4 " "" "1  +  2 * 3"] 7
    ["" "" "(1  +  2)* 3 "] 9))

(deftest files
  (are [path result] (= result (s/run-file (str "test-resources/" path ".pl")))
    "plain-sum" 42
    "parens" 154
    "multi-line" 12
    "assign" 42))

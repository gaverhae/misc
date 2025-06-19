(ns main-test
  (:require [main :as s]
            [clojure.test :refer [deftest are]]))

(deftest basic-expressions
  (are [string tree] (= tree (first (s/parse-string string :start :expr)))
    "34+123" [:sum [:int "34"] [:int "123"]]
    "1+2*3" [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    "2*3+1" [:sum [:product [:int "2"] [:int "3"]] [:int "1"]]))

(deftest whitespace-ignored
  (are [strings tree] (apply = tree (map (fn [s] (first (s/parse-string s :start :expr))) strings))
    ["34+123" "34 + 123" "34 +   123"] [:sum [:int "34"] [:int "123"]]
    ["1+2*3" "1 + 2 * 3" "1+2  *  3"] [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    ["2*3+1" "2 * 3 + 1"] [:sum [:product [:int "2"] [:int "3"]] [:int "1"]]))

(deftest parens
  (are [string tree] (= tree (first (s/parse-string string :start :expr)))
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
    ["1 + (2 * 3)"] [:S [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
    ["a = 2" "b = 5" "a + b"]
    [:S
     [:assign [:identifier "a"] [:int "2"]]
     [:assign [:identifier "b"] [:int "5"]]
     [:sum [:identifier "a"] [:identifier "b"]]]
    ["def fib(n):"]
    [:S [:def "fib" ["n"] []]]
    ["1 + 2"
     "a = 5"
     "def square(x):"
     "  return x * x"
     "b + 2"]
    [:S
     [:sum [:int "1"] [:int "2"]]
     [:assign [:identifier "a"] [:int "5"]]
     [:def "square" ["x"] [[:return [:product [:identifier "x"] [:identifier "x"]]]]]
     [:sum [:identifier "b"] [:int "2"]]]
    ["1 + 2"]
    [:S [:sum [:int "1"] [:int "2"]]]
    ["1 + 2"
     "a = 5"
     "def square(x):"
     "  return x * x"
     "square(a)"]
    [:S
     [:sum [:int "1"] [:int "2"]]
     [:assign [:identifier "a"] [:int "5"]]
     [:def "square" ["x"] [[:return [:product [:identifier "x"] [:identifier "x"]]]]]
     [:app [:identifier "square"] [:identifier "a"]]]
    ["1 + 3"
     "def complex(a, b, c):"
     "  def helper(a, b):"
     "    d = a * c"
     "    return d + 1"
     "  x = 1"
     "  return helper(x)"
     "complex = 3"]
    [:S
     [:sum [:int "1"] [:int "3"]]
     [:def "complex" ["a" "b" "c"]
      [[:def "helper" ["a" "b"]
        [[:assign [:identifier "d"] [:product [:identifier "a"] [:identifier "c"]]]
         [:return [:sum [:identifier "d"] [:int "1"]]]]]
       [:assign [:identifier "x"] [:int "1"]]
       [:return [:app [:identifier "helper"] [:identifier "x"]]]]]
     [:assign [:identifier "complex"] [:int "3"]]]))

(deftest pl-eval
  (are [string result] (= result (second (second (s/eval-pl {} (s/parse (str string "\n"))))))
    "1+2+3" 6
    "(1) + (2 * 3)" 7
    "1  +  2 * 3 " 7
    "(1  +  2)* 3 " 9))

(deftest multiline-expr
  (are [strings tree] (= tree (second (second (s/eval-pl {} (s/parse (->lines strings))))))
    ["4" "1+2+3"] 6
    ["(1+2)" "(1) + (2 * 3)"] 7
    ["" "6 * 4 " "" "1  +  2 * 3"] 7
    ["" "" "(1  +  2)* 3 "] 9))

(deftest files
  (are [path result] (= result (s/run-file (str "test-resources/" path ".py")))
    "plain-sum" 42
    "parens" 154
    "multi-line" 12
    "assign" 42
    "thrice" 31
    "global-side-effect" 12
    "global-shadowing" 2
    "global-unaffected" 5))

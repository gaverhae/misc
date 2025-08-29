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
    ["def fib(n):"
     "  return 1"]
    [:S [:def "fib" ["n"] [[:return [:int "1"]]]]]
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
     [:assign [:identifier "complex"] [:int "3"]]]
    ["a = 0"
     "if 0:"
     "  a = 1"
     "else:"
     "  a = 2"
     "a"]
    [:S
     [:assign [:identifier "a"] [:int "0"]]
     [:if
      [:int "0"]
      [[:assign [:identifier "a"] [:int "1"]]]
      [[:assign [:identifier "a"] [:int "2"]]]]
     [:identifier "a"]]
    ["True == (1 == 2)"]
    [:S
     [:equal
      [:bool "True"]
      [:equal
       [:int "1"]
       [:int "2"]]]]
    ["1 + fact(3)"]
    [:S
     [:sum
      [:int "1"]
      [:app [:identifier "fact"] [:int "3"]]]]))

(deftest pl-eval
  (are [string expected] (let [[env mem actual] (s/eval-pl (s/init-env) (s/init-mem) (s/parse (str string "\n")))]
                           #_(prn [expected actual])
                           (= expected actual))
    "1+2+3" [:int 6]
    "(1) + (2 * 3)" [:int 7]
    "1  +  2 * 3 " [:int 7]
    "(1  +  2)* 3 " [:int 9]
    "True" [:bool "True"]
    "1 == 1" [:bool "True"]
    "1 == 2" [:bool "False"]
    "True == False" [:bool "False"]))

(deftest multiline-expr
  (are [strings expected] (let [[env mem actual] (s/eval-pl (s/init-env) (s/init-mem) (s/parse (->lines strings)))]
                            #_(prn [expected actual])
                            (= expected actual))
    ["4" "1+2+3"] [:int 6]
    ["(1+2)" "(1) + (2 * 3)"] [:int 7]
    ["" "6 * 4 " "" "1  +  2 * 3"] [:int 7]
    ["" "" "(1  +  2)* 3 "] [:int 9]
    ["a = 1" "a + 2"] [:int 3]
    ["if True:"  "  1" "else:" "  2"] [:int 1]
    ["if False:" "  1" "else:" "  2"] [:int 2]
    ["def has_dead_code():"
     "  return 5"
     "  2"
     "has_dead_code()"] [:int 5]
    ["def fib(n):"
     "  if n == 0:"
     "    return 5"
     "  return 2"
     "fib(0)"] [:int 5]))

(deftest files
  (are [path result] (= result (s/run-file (str "test-resources/" path ".py")))
    "plain-sum" [:int 42]
    "parens" [:int 154]
    "multi-line" [:int 12]
    "assign" [:int 42]
    "thrice" [:int 31]
    "global-side-effect" [:int 12]
    "global-shadowing" [:int 2]
    "global-unaffected" [:int 5]
    "fact" [:int 3628800]
    "fib" [:int 89]
    "fib-flat" [:int 89]))

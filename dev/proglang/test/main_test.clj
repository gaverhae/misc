(ns main-test
  (:require [main :as s]
            [lisp :as l]
            [clojure.string :as string]
            [clojure.test :refer [deftest are is]]))

(deftest basic-expressions
  (are [string tree l-string l-tree] (and (= tree (first (s/parse-string string :start :expr)))
                                          (= [l-tree] (l/parse-string l-string :start :expr)))
    "34+123"
    [:sum [:int "34"] [:int "123"]]
    "(+ 34 123)"
    [:list [:symbol "+"] [:int "34"] [:int "123"]]

    "1+2*3"
    [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    "(+ 1 (* 2 3))"
    [:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]

    "2*3+1"
    [:sum [:product [:int "2"] [:int "3"]] [:int "1"]]
    "(+ (* 2 3) 1)"
    [:list [:symbol "+"] [:list [:symbol "*"] [:int "2"] [:int "3"]] [:int "1"]]))

(deftest whitespace-ignored
  (are [strings tree l-strings l-tree] (and (->> strings
                                                 (map (fn [s] (first (s/parse-string s :start :expr))))
                                                 (every? #(= tree %)))
                                            (->> l-strings
                                                 (map (fn [s] (l/parse-string s :start :expr)))
                                                 (every? #(= [l-tree] %))))
    ["34+123"
     "34 + 123"
     "34 +   123"]
    [:sum [:int "34"] [:int "123"]]

    ["(+ 34 123)"
     "(  +    34  123)"
     "(+ 34
         123)"]
    [:list [:symbol "+"] [:int "34"] [:int "123"]]

    ["1+2*3"
     "1 + 2 * 3"
     "1+2  *  3"]
    [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    ["(+ 1 (* 2 3))"
     "(+ 1(* 2 3))"
     "(
     +
     1
     (
     *
     2
     3
     )
     )"]
     [:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]

    ["2*3+1"
     "2 * 3 + 1"]
    [:sum [:product [:int "2"] [:int "3"]] [:int "1"]]
    ["(+ (* 2 3) 1)"
     "(+ ( * 2 3) 1)"]
    [:list [:symbol "+"] [:list [:symbol "*"] [:int "2"] [:int "3"]] [:int "1"]]))

(deftest parens
  (are [string tree l-string l-tree] (and (= tree (first (s/parse-string string :start :expr)))
                                          (= [l-tree] (l/parse-string l-string :start :expr)))
    "1 + (2 * 3)" [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    "(+ 1 (* 2 3))" [:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]

    "(1 + 2) * 3" [:product [:sum [:int "1"] [:int "2"]] [:int "3"]]
    "(* (+ 1 2) 3)" [:list [:symbol "*"] [:list [:symbol "+"] [:int "1"] [:int "2"]] [:int "3"]]

    "(1 + 2 * 3)" [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    "(+ 1 (* 2 3))" [:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]

    "(1) + (2 * 3)" [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]
    "(+ 1 (* 2 3))" [:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]))

(defn ->lines
  [strings]
  (->> strings
       (map (fn [s] (str s "\n")))
       (apply str)))

(deftest ast
  (are [strings tree l-strings l-tree] (and (= tree (s/parse (->lines strings)))
                                            (= l-tree (l/parse (->lines l-strings))))
    ["1 + (2 * 3)"] [:S [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
    ["(+ 1 (* 2 3))"] [:S [:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]]

    ["a = 2"
     "b = 5"
     "a + b"]
    [:S
     [:assign [:identifier "a"] [:int "2"]]
     [:assign [:identifier "b"] [:int "5"]]
     [:sum [:identifier "a"] [:identifier "b"]]]
    ["(let [a 2"
     "      b 5]"
     "  (+ a b))"]
    [:S [:list
         [:symbol "let"]
         [:vector [:symbol "a"] [:int "2"] [:symbol "b"] [:int "5"]]
         [:list [:symbol "+"] [:symbol "a"] [:symbol "b"]]]]

    ["def fib(n):"
     "  return 1"]
    [:S [:def "fib" ["n"] [[:return [:int "1"]]]]]
    ["(defn fib [n]"
     "  1)"]
    [:S [:list [:symbol "defn"] [:symbol "fib"] [:vector [:symbol "n"]] [:int "1"]]]

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
    ["(+ 1 2)"
     "(def a 5)"
     "(defn square [x] (* x x))"
     "(+ b 2)"]
    [:S
     [:list [:symbol "+"] [:int "1"] [:int "2"]]
     [:list [:symbol "def"] [:symbol "a"] [:int "5"]]
     [:list [:symbol "defn"] [:symbol "square"] [:vector [:symbol "x"]] [:list [:symbol "*"] [:symbol "x"] [:symbol "x"]]]
     [:list [:symbol "+"] [:symbol "b"] [:int "2"]]]

    ["1 + 2"]
    [:S [:sum [:int "1"] [:int "2"]]]
    ["(+ 1 2)"]
    [:S [:list [:symbol "+"] [:int "1"] [:int "2"]]]

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
    ["(+ 1 2)"
     "(def a 5)"
     "(def square (fn [x] (* x x)))"
     "(square 5)"]
    [:S
     [:list [:symbol "+"] [:int "1"] [:int "2"]]
     [:list [:symbol "def"] [:symbol "a"] [:int "5"]]
     [:list [:symbol "def"] [:symbol "square"] [:list [:symbol "fn"] [:vector [:symbol "x"]] [:list [:symbol "*"] [:symbol "x"] [:symbol "x"]]]]
     [:list [:symbol "square"] [:int "5"]]]

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
    ["(+ 1 3)"
     "(defn complex [a b c]"
     "  (let [helper (fn [a b]"
     "                 (let [d (* a c)]"
     "                   (+ d 1)))"
     "        x 1]"
     "    (helper x)))"
     "(def complex 3)"]
    [:S
     [:list [:symbol "+"] [:int "1"] [:int "3"]]
     [:list
      [:symbol "defn"] [:symbol "complex"] [:vector [:symbol "a"] [:symbol "b"] [:symbol "c"]]
      [:list
       [:symbol "let"]
       [:vector
        [:symbol "helper"] [:list [:symbol "fn"] [:vector [:symbol "a"] [:symbol "b"]]
                            [:list [:symbol "let"] [:vector [:symbol "d"] [:list [:symbol "*"] [:symbol "a"] [:symbol "c"]]]
                             [:list [:symbol "+"] [:symbol "d"] [:int "1"]]]]
        [:symbol "x"] [:int "1"]]
       [:list [:symbol "helper"] [:symbol "x"]]]]
     [:list [:symbol "def"] [:symbol "complex"] [:int "3"]]]

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
    ["(let [a (if false 1 2)]"
     "  a)"]
    [:S [:list [:symbol "let"] [:vector [:symbol "a"] [:list [:symbol "if"] [:bool "false"] [:int "1"] [:int "2"]]]
         [:symbol "a"]]]

    ["True == (1 == 2)"]
    [:S
     [:equal
      [:bool "True"]
      [:equal
       [:int "1"]
       [:int "2"]]]]
    ["(= true (= 1 2))"]
    [:S [:list [:symbol "="] [:bool "true"] [:list [:symbol "="] [:int "1"] [:int "2"]]]]

    ["1 + fact(3)"]
    [:S
     [:sum
      [:int "1"]
      [:app [:identifier "fact"] [:int "3"]]]]
    ["(+ 1 (fact 3))"]
    [:S [:list [:symbol "+"] [:int "1"] [:list [:symbol "fact"] [:int "3"]]]]))

(deftest pl-eval
  (are [string expected] (let [[actual t m] (s/eval-pl (s/parse (str string "\n")))]
                           #_(prn [expected actual])
                           (= expected actual))
    "1+2+3" [:int 6]
    "(1) + (2 * 3)" [:int 7]
    "1  +  2 * 3 " [:int 7]
    "(1  +  2)* 3 " [:int 9]
    "True" [:bool true]
    "1 == 1" [:bool true]
    "1 == 2" [:bool false]
    "True == False" [:bool false]))

(deftest multiline-expr
  (are [strings expected] (let [[actual t m] (s/eval-pl (s/parse (->lines strings)))]
                            #_(prn [:exp expected :act actual])
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
  (are [path result] (let [s (with-out-str (s/run-file (str "test-resources/" path ".py")))]
                       #_(prn [:exp result :act (string/split-lines s)])
                       (= result (string/split-lines s)))
    "plain-sum" ["42"]
    "parens" ["154"]
    "multi-line" ["12"]
    "assign" ["42"]
    "thrice" ["31"]
    "global-side-effect" ["12"]
    "global-shadowing" ["2"]
    "global-unaffected" ["5"]
    "fact" ["3628800"]
    "fib" ["89"]
    "fib-flat" ["89"]))

(deftest gc
  (let [fib (s/m-eval (s/parse (->lines ["def fib(n):"
                                         "  if n == 0:"
                                         "    return 1"
                                         "  if n == 1:"
                                         "    return 1"
                                         "  return fib(n + -1) + fib(n + -2)"
                                         "fib(12)"])))]
    (let [[v {:keys [env stack]} {:keys [mem next-addr]}] (s/mrun-envs fib)]
      (is (= {"print" 0, "start_t" 1, "wait_t" 2, "fib" 3} env))
      (is (= 469 next-addr))
      (is (= 469 (count mem)))
      (is (= [] stack))
      (is (= [:int 233] v)))
    (let [[v {:keys [env stack]} {:keys [mem next-addr]}] (binding [s/+enable-gc+ true]
                                                            (s/mrun-envs fib))]
      (is (= {"print" 0, "start_t" 1, "wait_t" 2, "fib" 3} env))
      (is (= 469 next-addr))
      (is (= 4 (count mem)))
      (is (= [] stack))
      (is (= [:int 233] v)))))

(deftest multi-thread
  (let [multifib (s/m-eval (s/parse (->lines ["def fib(n):"
                                              "  if n == 0:"
                                              "    return 1"
                                              "  if n == 1:"
                                              "    return 1"
                                              "  return fib(n + -1) + fib(n + -2)"
                                              "def fib3():"
                                              "  return fib(3)"
                                              "def fib4():"
                                              "  return fib(4)"
                                              "t1 = start_t(fib3)"
                                              "t2 = start_t(fib4)"
                                              "r1 = wait_t(t1)"
                                              "r2 = wait_t(t2)"
                                              "r1 + r2"])))
        [v t m] (s/mrun-envs multifib)]
    (is (= [:int 8] v))
    (is (= [:int 8] (get-in m [:done-threads 0 0])))
    (is (= [:int 3] (get-in m [:done-threads 1 0])))
    (is (= [:int 5] (get-in m [:done-threads 2 0]))))
  (let [multiprint (s/m-eval (s/parse (->lines ["def prints(n):"
                                                "  def h():"
                                                "    print(n)"
                                                "    print(n)"
                                                "    print(n)"
                                                "    print(n)"
                                                "    print(n)"
                                                "    return n"
                                                "  return h"
                                                "print(0)"
                                                "t1 = start_t(prints(1))"
                                                "t2 = start_t(prints(2))"
                                                "print(0)"
                                                "r1 = wait_t(t1)"
                                                "r2 = wait_t(t2)"
                                                "print(0)"
                                                "r1 + r2"])))]
    (binding [*out* (java.io.StringWriter.)]
      (let [[v t m] (s/mrun-envs multiprint)
            output (-> (str *out*) string/split-lines)]
        (is (= output
               [;; before we start any other thread, we have one print from 0
                "0"
                ;; while 0 prepares 2, 1 starts running
                "1" "1" "1"
                ;; 2 starts running, and now w're interleaved
                "2"
                "0"
                "1"
                "2"
                "1"
                ;; 1 has finished, and 0 is waiting for 1 and 2, so just 2 for a while
                "2" "2" "2"
                ;; final print from 0
                "0"]))
        (is (= v [:int 3]))))))

(comment
  (def fib (s/m-eval (s/parse (->lines ["def fib(n):"
                                        "  if n == 0:"
                                        "    return 1"
                                        "  if n == 1:"
                                        "    return 1"
                                        "  return fib(n + -1) + fib(n + -2)"
                                        "fib(25)"]))))
  (with-out-str (time (s/mrun-envs fib)))
"\"Elapsed time: 38808.007334 msecs\"\n"
  (with-out-str (time (binding [s/+enable-gc+ true]
                        (s/mrun-envs fib))))
"\"Elapsed time: 40742.647542 msecs\"\n"
)

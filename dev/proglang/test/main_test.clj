(ns main-test
  (:require [main :as s]
            [lisp :as l]
            [clojure.string :as string]
            [expectations :refer [expect]]))

(defn ->lines
  [strings]
  (->> strings
       (map (fn [s] (str s "\n")))
       (apply str)))

;; Lisp Reader

(let [l (fn [s] (l/read-forms (->lines s)))]
  (expect [[:v/int 4]]
          (l ["4"]))
  (expect [[:v/vector [:v/int 4] [:v/int 5] [:v/symbol "sym"] [:v/bool true] [:v/vector [:v/int 1] [:v/list [:v/int 2]]]] [:v/int 4]]
          (l ["[4 5 sym true [1 (2)]] 4"]))
  (expect [[:v/string "this is a \"complex string\""]]
          (l ["\"this is a \"complex string\"\""])))

;; Basic expressions

(let [p (fn [s] (s/parse-string s :start :expr))
      l (fn [s] (l/parse s :start :expr))]
  (expect [[:sum [:int "34"] [:int "123"]]]
          (p "34+123"))
  (expect [[:list [:symbol "+"] [:int "34"] [:int "123"]]]
          (l "(+ 34 123)"))

  (expect [[:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
          (p "1+2*3"))
  (expect [[:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]]
          (l "(+ 1 (* 2 3))"))

  (expect [[:sum [:product [:int "2"] [:int "3"]] [:int "1"]]]
          (p "2*3+1"))
  (expect [[:list [:symbol "+"] [:list [:symbol "*"] [:int "2"] [:int "3"]] [:int "1"]]]
          (l "(+ (* 2 3) 1)")))

;; Ensure whitespace is not significant in places where it shouldn't.

(let [p (fn [s] (s/parse-string s :start :expr))
      l (fn [s] (l/parse s :start :expr))]
  (let [exp [[:sum [:int "34"] [:int "123"]]]]
    (expect exp (p "34+123"))
    (expect exp (p "34 + 123"))
    (expect exp (p "34 +   123")))
  (let [exp [[:list [:symbol "+"] [:int "34"] [:int "123"]]]]
    (expect exp (l "(+ 34 123)"))
    (expect exp (l "(  +    34  123)"))
    (expect exp (l "(+ 34
                   123)")))

  (let [exp [[:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]]
    (expect exp (p "1+2*3"))
    (expect exp (p "1 + 2 * 3"))
    (expect exp (p "1+2  *  3")))
  (let [exp [[:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]]]
    (expect exp (l "(+ 1 (* 2 3))"))
    (expect exp (l "(+ 1(* 2 3))"))
    (expect exp (l "(
                   +
                   1
                   (
                   *
                   2
                   3
                   )
                   )")))

  (let [exp [[:sum [:product [:int "2"] [:int "3"]] [:int "1"]]]]
    (expect exp (p "2*3+1"))
    (expect exp (p "2 * 3 + 1")))
  (let [exp [[:list [:symbol "+"] [:list [:symbol "*"] [:int "2"] [:int "3"]] [:int "1"]]]]
    (expect exp (l "(+ (* 2 3) 1)"))
    (expect exp (l "(+ ( * 2 3) 1)"))))

;; Parens serve as grouping in main (operation call in lisp).

(let [p (fn [s] (s/parse-string s :start :expr))
      l (fn [s] (l/parse s :start :expr))]
  (expect [[:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
          (p "1 + (2 * 3)" ))
  (expect [[:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]]
          (l "(+ 1 (* 2 3))" ))

  (expect [[:product [:sum [:int "1"] [:int "2"]] [:int "3"]]]
          (p "(1 + 2) * 3" ))
  (expect [[:list [:symbol "*"] [:list [:symbol "+"] [:int "1"] [:int "2"]] [:int "3"]]]
          (l "(* (+ 1 2) 3)" ))

  (expect [[:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
          (p "(1 + 2 * 3)" ))
  (expect [[:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]]
          (l "(+ 1 (* 2 3))" ))

  (expect [[:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
          (p "(1) + (2 * 3)" ))
  (expect [[:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]]
          (l "(+ 1 (* 2 3))" )))

;; Check expected AST on more complex examples.

(let [p (fn [s] (s/parse (->lines s)))
      l (fn [s] (l/parse (->lines s)))]
  (expect [:S [:sum [:int "1"] [:product [:int "2"] [:int "3"]]]]
          (p ["1 + (2 * 3)"]))
  (expect [:S [:list [:symbol "+"] [:int "1"] [:list [:symbol "*"] [:int "2"] [:int "3"]]]]
          (l ["(+ 1 (* 2 3))"]))

  (expect [:S
           [:assign [:identifier "a"] [:int "2"]]
           [:assign [:identifier "b"] [:int "5"]]
           [:sum [:identifier "a"] [:identifier "b"]]]
          (p ["a = 2"
              "b = 5"
              "a + b"]))
  (expect [:S [:list
               [:symbol "let"]
               [:vector [:symbol "a"] [:int "2"] [:symbol "b"] [:int "5"]]
               [:list [:symbol "+"] [:symbol "a"] [:symbol "b"]]]]
          (l ["(let [a 2"
              "      b 5]"
              "  (+ a b))"]))

  (expect [:S [:def "fib" ["n"] [[:return [:int "1"]]]]]
          (p ["def fib(n):"
              "  return 1"]))
  (expect [:S [:list [:symbol "defn"] [:symbol "fib"] [:vector [:symbol "n"]] [:int "1"]]]
          (l ["(defn fib [n]"
              "  1)"]))

  (expect [:S
           [:sum [:int "1"] [:int "2"]]
           [:assign [:identifier "a"] [:int "5"]]
           [:def "square" ["x"] [[:return [:product [:identifier "x"] [:identifier "x"]]]]]
           [:sum [:identifier "b"] [:int "2"]]]
          (p ["1 + 2"
              "a = 5"
              "def square(x):"
              "  return x * x"
              "b + 2"]))
  (expect [:S
           [:list [:symbol "+"] [:int "1"] [:int "2"]]
           [:list [:symbol "def"] [:symbol "a"] [:int "5"]]
           [:list [:symbol "defn"] [:symbol "square"] [:vector [:symbol "x"]] [:list [:symbol "*"] [:symbol "x"] [:symbol "x"]]]
           [:list [:symbol "+"] [:symbol "b"] [:int "2"]]]
          (l ["(+ 1 2)"
              "(def a 5)"
              "(defn square [x] (* x x))"
              "(+ b 2)"]))

  (expect [:S [:sum [:int "1"] [:int "2"]]]
          (p ["1 + 2"]))
  (expect [:S [:list [:symbol "+"] [:int "1"] [:int "2"]]]
          (l ["(+ 1 2)"]))

  (expect [:S
           [:sum [:int "1"] [:int "2"]]
           [:assign [:identifier "a"] [:int "5"]]
           [:def "square" ["x"] [[:return [:product [:identifier "x"] [:identifier "x"]]]]]
           [:app [:identifier "square"] [:identifier "a"]]]
          (p ["1 + 2"
              "a = 5"
              "def square(x):"
              "  return x * x"
              "square(a)"]))
  (expect [:S
           [:list [:symbol "+"] [:int "1"] [:int "2"]]
           [:list [:symbol "def"] [:symbol "a"] [:int "5"]]
           [:list [:symbol "def"] [:symbol "square"]
            [:list [:symbol "fn"] [:vector [:symbol "x"]]
             [:list [:symbol "*"] [:symbol "x"] [:symbol "x"]]]]
           [:list [:symbol "square"] [:int "5"]]]
          (l ["(+ 1 2)"
              "(def a 5)"
              "(def square (fn [x] (* x x)))"
              "(square 5)"]))

  (expect [:S
           [:sum [:int "1"] [:int "3"]]
           [:def "complex" ["a" "b" "c"]
            [[:def "helper" ["a" "b"]
              [[:assign [:identifier "d"] [:product [:identifier "a"] [:identifier "c"]]]
               [:return [:sum [:identifier "d"] [:int "1"]]]]]
             [:assign [:identifier "x"] [:int "1"]]
             [:return [:app [:identifier "helper"] [:identifier "x"]]]]]
           [:assign [:identifier "complex"] [:int "3"]]]
          (p ["1 + 3"
              "def complex(a, b, c):"
              "  def helper(a, b):"
              "    d = a * c"
              "    return d + 1"
              "  x = 1"
              "  return helper(x)"
              "complex = 3"]))
  (expect [:S
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
          (l ["(+ 1 3)"
              "(defn complex [a b c]"
              "  (let [helper (fn [a b]"
              "                 (let [d (* a c)]"
              "                   (+ d 1)))"
              "        x 1]"
              "    (helper x)))"
              "(def complex 3)"]))

  (expect [:S
           [:assign [:identifier "a"] [:int "0"]]
           [:if
            [:int "0"]
            [[:assign [:identifier "a"] [:int "1"]]]
            [[:assign [:identifier "a"] [:int "2"]]]]
           [:identifier "a"]]
          (p ["a = 0"
              "if 0:"
              "  a = 1"
              "else:"
              "  a = 2"
              "a"]))
  (expect [:S [:list [:symbol "let"] [:vector [:symbol "a"] [:list [:symbol "if"] [:bool "false"] [:int "1"] [:int "2"]]]
               [:symbol "a"]]]
          (l ["(let [a (if false 1 2)]"
              "  a)"]))

  (expect [:S
           [:equal
            [:bool "True"]
            [:equal
             [:int "1"]
             [:int "2"]]]]
          (p ["True == (1 == 2)"]))
  (expect [:S [:list [:symbol "="] [:bool "true"] [:list [:symbol "="] [:int "1"] [:int "2"]]]]
          (l ["(= true (= 1 2))"]))

  (expect [:S
           [:sum
            [:int "1"]
            [:app [:identifier "fact"] [:int "3"]]]]
          (p ["1 + fact(3)"]))
  (expect [:S [:list [:symbol "+"] [:int "1"] [:list [:symbol "fact"] [:int "3"]]]]
          (l ["(+ 1 (fact 3))"])))

;; Evaluation of simple expressions.

(let [p (fn [s] (first (s/eval-pl (s/parse (str s "\n")))))
      l (fn [s] (l/eval-forms (l/read-forms (str s "\n"))))]
  (expect [:int 6] (p "1+2+3"))
  (expect [:v/int 6] (l "(+ 1 2 3)"))

  (expect [:int 7] (p "(1) + (2 * 3)"))
  (expect [:int 7] (p "1  +  2 * 3 "))
  (expect [:v/int 7] (l "(+ 1 (* 2 3))"))

  (expect [:int 9] (p "(1  +  2)* 3 "))
  (expect [:v/int 9] (l "(* (+ 1 2) 3)"))

  (expect [:bool true] (p "True"))
  (expect [:v/bool true] (l "true"))

  (expect [:bool true] (p "1 == 1" ))
  (expect [:v/bool true] (l "(= 1 1)"))

  (expect [:bool false] (p "1 == 2" ))
  (expect [:v/bool false] (l "(= 1 2)"))

  (expect [:bool false] (p "True == False" ))
  (expect [:v/bool false] (l "(= true false)"))

  (expect [:v/vector [:v/int 2]]
          (l "[(+ 1 1)]"))

  (expect [:v/error "Tried to add non-numeric values."]
          (l "(+ 1 true)"))
  (expect [:v/error "Tried to add non-numeric values."]
          (l "[(+ 1 true) 0]"))
  (expect [:v/error "Tried to add non-numeric values."]
          (l "(+ 1 2) (+ true false) (+ 1 3)"))
  (expect [:v/error "Tried to add non-numeric values."]
          (l "(do (+ 1 2) (+ true false) (+ 1 3))")))

;; Evaluation of multi-line programs.

(let [p (fn [s] (first (s/eval-pl (s/parse (->lines s)))))
      l (fn [s] (l/eval-forms (l/read-forms (->lines s))))]
  (expect [:int 6]
          (p ["4"
              "1+2+3"]))
  (expect [:v/int 6]
          (l ["4"
              "(+ 1 2 3)"]))

  (expect [:int 7]
          (p ["(1+2)"
              "(1) + (2 * 3)"]))
  (expect [:v/int 7]
          (l ["(+ 1 2)"
              "(+ 1 (* 2 3))"]))

  (expect [:int 7]
          (p [""
              "6 * 4 "
              ""
              "1  +  2 * 3"]))
  (expect [:v/int 7]
          (l [""
              "(* 6 4)"
              ""
              "(+ 1 (* 2 3))"]))

  (expect [:int 9]
          (p [""
              ""
              "(1  +  2)* 3 "]))
  (expect [:v/int 9]
          (l [""
              ""
              "(* (+ 1 2) 3)"]))

  (expect [:int 3]
          (p ["a = 1"
              "a + 2"]))
  (expect [:v/int 3]
          (l ["(def a 1)"
              "(+ a 2)"]))
  (expect [:v/int 3]
          (l ["(let [a 1] (+ a 2))"]))
  (expect [:v/int 3]
          (l ["(let [a 1 b 0] (+ 1 b) (+ 2 a))"]))
  (expect [:v/vector [:v/int 1] [:v/int 3] [:v/int 5] [:v/int 1]]
          (l ["(let [a 1]
                [a (+ a 2) (let [a 3] (+ a 2)) a])"]))

  (expect [:int 1]
          (p ["if True:"
              "  1"
              "else:"
              "  2"]))
  (expect [:int 2]
          (p ["if False:"
              "  1"
              "else:"
              "  2"]))
  (expect [:int 5]
          (p ["def has_dead_code():"
              "  return 5"
              "  2"
              "has_dead_code()"]))
  (expect [:int 5]
          (p ["def fib(n):"
              "  if n == 0:"
              "    return 5"
              "  return 2"
              "fib(0)"]))

  (expect [:v/int 4]
          (l ["(if true 4 5)"]))
  (expect [:v/int 5]
          (l ["(if false 4 5)"]))

  (expect [:v/int 5]
          (l ["((fn [x] (+ x 1)) 4)"]))

  (expect [:v/vector
           [:v/int 1]
           [:v/int 1]
           [:v/int 2]
           [:v/int 3]
           [:v/int 5]
           [:v/int 8]
           [:v/int 13]
           [:v/int 21]
           [:v/int 34]
           [:v/int 55]]
          (l ["(let [g (fn [f]"
              "          (fn [n]"
              "            (if (= 0 n)"
              "              1"
              "              (if (= 1 n)"
              "                1"
              "                (+ ((f f) (+ n -1)) ((f f) (+ n -2)))))))]"
              "  (let [fib (g g)]"
              "    [(fib 0)"
              "     (fib 1)"
              "     (fib 2)"
              "     (fib 3)"
              "     (fib 4)"
              "     (fib 5)"
              "     (fib 6)"
              "     (fib 7)"
              "     (fib 8)"
              "     (fib 9)]))"]))
  (expect [:v/int 233]
          (l ["((fn fib [n]"
              "   (if (= 0 n)"
              "     (let [fib 1]"
              "       fib)"
              "     (if (= 1 n)"
              "       1"
              "       (+ (fib (+ n -1))"
              "          (fib (+ n -2))))))"
              "  12)"])))

;; Processing entire files.

(let [p (fn [p] (let [s (with-out-str (s/run-file (str "test-resources/" p ".py")))]
                  (string/split-lines s)))]
  (expect ["42"] (p "plain-sum"))
  (expect ["154"] (p "parens"))
  (expect ["12"] (p "multi-line"))
  (expect ["42"] (p "assign"))
  (expect ["31"] (p "thrice"))
  (expect ["12"] (p "global-side-effect"))
  (expect ["2"] (p "global-shadowing"))
  (expect ["5"] (p "global-unaffected"))
  (expect ["3628800"] (p "fact"))
  (expect ["89"] (p "fib"))
  (expect ["89"] (p "fib-flat")))

;; Testing GC.

(let [fib (s/m-eval (s/parse (->lines ["def fib(n):"
                                       "  if n == 0:"
                                       "    return 1"
                                       "  if n == 1:"
                                       "    return 1"
                                       "  return fib(n + -1) + fib(n + -2)"
                                       "fib(12)"])))]
  (let [[v {:keys [env stack]} {:keys [mem next-addr]}] (s/mrun-envs fib)]
    (expect {"print" 0, "start_t" 1, "wait_t" 2, "fib" 3} env)
    (expect 469 next-addr)
    (expect 469 (count mem))
    (expect [] stack)
    (expect [:int 233] v))
  (let [[v {:keys [env stack]} {:keys [mem next-addr]}] (binding [s/+enable-gc+ true]
                                                          (s/mrun-envs fib))]
    (expect {"print" 0, "start_t" 1, "wait_t" 2, "fib" 3} env)
    (expect 469 next-addr)
    (expect 4 (count mem))
    (expect [] stack)
    (expect [:int 233] v)))

;; Multi-threading with explicit interleaving.

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
  (expect [:int 8] v)
  (expect [:int 8] (get-in m [:done-threads 0 0]))
  (expect [:int 3] (get-in m [:done-threads 1 0]))
  (expect [:int 5] (get-in m [:done-threads 2 0])))

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
      (expect [;; before we start any other thread, we have one print from 0
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
               "0"]
              output)
      (expect v [:int 3]))))

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

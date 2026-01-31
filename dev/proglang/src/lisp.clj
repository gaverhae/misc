(ns lisp
  (:require [instaparse.core :as insta]
            [io.github.gaverhae.clonad :as m :refer [mdo monad]]
            [io.github.gaverhae.vatch :refer [vatch]]))

(def parse
  (insta/parser
    "S := (ws* expr ws*)*
     <expr> := list | vector | int | (bool / symbol)
     list := <'('> ws* (expr ws*)* <')'>
     vector := <'['> ws* (expr ws*)* <']'>
     symbol := #'[\\w+_*=-]+' | '/'
     int := #'[+-]?[0-9]+'
     bool := 'true' | 'false'
     <ws> = <#'\\s'>"))

(defn read-forms
  [s]
  (let [h (fn ! [p]
            (vatch p
              [:S & fs] (map ! fs)
              [:int s] [:v/int (parse-long s)]))]
    (h (parse s))))

(def init-state
  {:top-level {}})

(defn m-run
  [mv state]
  (vatch mv
    [:pure v] [v state]
    [:bind mv f] (let [[v state] (m-run mv state)]
                   (m-run (f v) state))
    [:error msg] [mv state]
    [:add-top-level n v] [[:v/int 0] (update state :top-level assoc n v)]
    [:lookup x] [(get-in state [:top-level x]) state]))

(defn m-eval
  [node]
  (vatch node
    [:int n] [:pure [:v/int (parse-long n)]]
    [:bool t] [:pure [:v/bool (case t
                                "true" true
                                "false" false)]]
    [:symbol x] [:lookup x]
    [:list op & args] (vatch op
                        [:symbol "+"] (monad
                                        args :<< (m/m-seq (map m-eval args))
                                        (if (->> args (map first) (every? #{:v/int}))
                                          [:pure [:v/int (reduce + 0 (map second args))]]
                                          [:error "Tried to add non-numeric values."]))
                        [:symbol "*"] (monad
                                        args :<< (m/m-seq (map m-eval args))
                                        (if (->> args (map first) (every? #{:v/int}))
                                          [:pure [:v/int (reduce * 1 (map second args))]]
                                          [:error "Tried to multiply non-numeric values."]))
                        [:symbol "="] (monad
                                        args :<< (m/m-seq (map m-eval args))
                                        [:pure [:v/bool (or (empty? args)
                                                            (apply = args))]])
                        [:symbol "def"] (vatch args
                                          [[:symbol x] expr] (monad
                                                               v :<< (m-eval expr)
                                                               [:add-top-level x v])
                                          otherwise [:error "Invalid syntax: def."]))
    [:S & exprs] (monad
                   values :<< (m/m-seq (map m-eval exprs))
                   [:pure (last values)])))

(defn eval-pl
  [expr]
  (m-run (m-eval expr) init-state))

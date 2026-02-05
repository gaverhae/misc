(ns lisp
  (:require [instaparse.core :as insta]
            [io.github.gaverhae.clonad :as m :refer [m-let]]
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
              [:bool b] [:v/bool (case b
                                   "true" true
                                   "false" false)]
              [:int s] [:v/int (parse-long s)]
              [:list & vs] (vec (cons :v/list (map ! vs)))
              [:symbol n] [:v/symbol n]
              [:vector & vs] (vec (cons :v/vector (map ! vs)))))]
    (h (parse s))))

(def init-state
  {:top-level {}})

(defn m-run
  [mv state]
  (vatch mv
    [:m/pure v] [v state]
    [:m/bind mv f] (let [[v state] (m-run mv state)]
                     (m-run (f v) state))
    [:m/error msg] [mv state]
    [:m/add-top-level n v] [[:v/int 0] (update state :top-level assoc n v)]
    [:m/lookup x] [(get-in state [:top-level x]) state]))

(defn m-eval
  [node]
  (vatch node
    [:v/symbol x] [:m/lookup x]
    [:v/list op & args] (vatch op
                          [:v/symbol "+"] (m-let :m
                                            [args (m/m-seq :m (map m-eval args))]
                                            (if (->> args (map first) (every? #{:v/int}))
                                              [:m/pure [:v/int (reduce + 0 (map second args))]]
                                              [:m/error "Tried to add non-numeric values."]))
                          [:v/symbol "*"] (m-let :m
                                            [args (m/m-seq :m (map m-eval args))]
                                            (if (->> args (map first) (every? #{:v/int}))
                                              [:m/pure [:v/int (reduce * 1 (map second args))]]
                                              [:m/error "Tried to multiply non-numeric values."]))
                          [:v/symbol "="] (m-let :m
                                            [args (m/m-seq :m (map m-eval args))]
                                            [:m/pure [:v/bool (or (empty? args)
                                                                  (apply = args))]])
                          [:v/symbol "def"] (vatch args
                                              [[:v/symbol x] expr] (m-let :m
                                                                     [v (m-eval expr)]
                                                                     [:m/add-top-level x v])
                                              otherwise [:m/error "Invalid syntax: def."]))
    otherwise [:m/pure node]))

(defn eval-forms
  [forms]
  (first (m-run (m-let :m
                  [values (m/m-seq :m (map m-eval forms))]
                  [:m/pure (last values)])
                init-state)))

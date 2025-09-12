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

(def init-state {})

(defn m-run
  [mv state]
  (vatch mv
    [:pure v] [v state]
    [:bind mv f] (let [[v state] (m-run mv state)]
                   (m-run (f v) state))
    [:assert a f msg] (if (f a)
                        [nil state]
                        (throw (ex-info {:value a} msg)))))

(defn m-eval
  [node]
  (vatch node
    [:int n] [:pure [:v/int (parse-long n)]]
    [:list op & args] (vatch op
                        [:symbol "+"] (monad
                                        args :<< (m/m-seq (map m-eval args))
                                        [:assert args #(every? #{:v/int} (map first %)) "Tried to add non-numeric values."]
                                        [:pure [:v/int (reduce + 0 (map second args))]]))
    [:S & exprs] (monad
                   values :<< (m/m-seq (map m-eval exprs))
                   [:pure (last values)])))

(defn eval-pl
  [expr]
  (m-run (m-eval expr) init-state))

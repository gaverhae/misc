(ns lisp
  (:require [instaparse.core :as insta]
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

(defn m-eval
  ([node] (m-eval node {}))
  ([node state]
   (vatch node
     [:int n] [[:v/int (parse-long n)] state]
     [:list op & args] (vatch op
                         [:symbol "+"] (let [[args state] (reduce (fn [[values state] expr]
                                                                    (let [[v state] (m-eval expr state)]
                                                                      [(conj values v) state]))
                                                                  [[] state]
                                                                  args)]
                                         (assert (every? #{:v/int} (map first args)))
                                         [[:v/int (reduce + 0 (map second args))] state]))
     [:S & exprs] (reduce (fn [[prev state] expr]
                            (m-eval expr state))
                          [nil state]
                          exprs))))

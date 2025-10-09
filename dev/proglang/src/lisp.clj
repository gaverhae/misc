(ns lisp
  (:require [instaparse.core :as insta]))

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

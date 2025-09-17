(ns lisp
  (:require [instaparse.core :as insta]))

(def parse-string
  (insta/parser
    "<expr> := list | symbol | int
     list := <'('> ws* (expr ws*)* <')'>
     symbol := #'[\\w+_*-]+' | '/'
     int := #'[+-]?[0-9]+'
     <ws> = <#'\\s'>"))

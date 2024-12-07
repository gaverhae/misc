(ns t.day07
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [instaparse.core :as insta]
            [t.lib :as lib :refer [->long]]))

(def parser
  (insta/parser
    "<S> = n <':'> (<w> n)+
     n = #'\\d+'
     w = #'\\W+'"))

(defn parse
  [lines]
  (->> lines
       (map parser)
       (map (fn [l] (->> l (map (fn [[_ s]] (->long s))))))
       (map (fn [[tot & ts]] [tot ts]))))

(defn part1
  [input]
  input)

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 0
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

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

(defn can-work?
  [[tot [t & ts]]]
  (loop [possible-values #{t}
         remaining-terms ts]
    (if (empty? remaining-terms)
      (contains? possible-values tot)
      (let [[t & ts] remaining-terms]
        (recur (->> possible-values
                    (mapcat (fn [p] [(+ p t) (* p t)]))
                    set)
               ts)))))

(defn part1
  [input]
  (->> input
       (filter can-work?)
       (map first)
       (reduce + 0)))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 3749
  [part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

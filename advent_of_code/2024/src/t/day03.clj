(ns t.day03
  (:require [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (mapcat (fn [l]
                 (re-seq #"mul\(\d{1,3},\d{1,3}\)" l)))))

(defn solve
  [input]
  (->> input
       (map (fn [m]
              (let [[_ a b] (re-matches #"mul\((\d+),(\d+)\)" m)]
                (* (->long a) (->long b)))))
       (reduce + 0)))

(defn part1
  [input]
  (solve input))

(defn part2
  [input]
  (solve input))

(lib/check
  [part1 sample] 161
  [part1 puzzle] 156388521
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

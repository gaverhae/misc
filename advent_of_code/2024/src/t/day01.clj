(ns t.day01
  (:require [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (mapcat (fn [s] (string/split s #" +")))
       (map ->long)
       (partition 2 2)
       lib/transpose))

(defn solve
  [cols]
  (->> cols
       (map sort)
       lib/transpose
       (map (fn [[a b]]
              (abs (- a b))))
       (reduce + 0)))

(defn part1
  [input]
  (solve input))

(defn part2
  [input]
  (solve input))

(lib/check
  [part1 sample] 11
  [part1 puzzle] 2375403
  #_#_[part2 sample] 1
  #_#_[part2 puzzle] 1)

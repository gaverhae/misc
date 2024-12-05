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

(defn part1
  [cols]
  (->> cols
       (map sort)
       lib/transpose
       (map (fn [[a b]]
              (abs (- a b))))
       (reduce + 0)))

(defn part2
  [[left right]]
  (let [freqs (frequencies right)]
    (->> left
         (map (fn [n] (* n (freqs n 0))))
         (reduce + 0))))

(lib/check
  [part1 sample] 11
  [part1 puzzle] 2375403
  [part2 sample] 31
  [part2 puzzle] 23082277)

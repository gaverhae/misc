(ns t.day02
  (:require [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (map (fn [s] (->> (string/split s #" ")
                         (map ->long))))))

(defn solve
  [lines]
  (->> lines
       (filter (fn [r]
                 (and (or (= r (sort r))
                          (= r (reverse (sort r))))
                      (->> r
                           (partition 2 1)
                           (every? (fn [[a b]] (<= 1 (abs (- a b)) 3)))))))
       count))

(defn part1
  [input]
  (solve input))

(defn part2
  [input]
  (solve input))

(lib/check
  [part1 sample] 2
  [part1 puzzle] 564
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

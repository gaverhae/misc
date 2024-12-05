(ns t.day02
  (:require [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (map (fn [s] (->> (string/split s #" ")
                         (map ->long))))))

(defn safe?
  [r]
  (and (or (= r (sort r))
           (= r (reverse (sort r))))
       (->> r
            (partition 2 1)
            (every? (fn [[a b]] (<= 1 (abs (- a b)) 3))))))

(defn part1
  [input]
  (->> input
       (filter safe?)
       count))

(defn part2
  [input]
  (->> input
       (filter (fn [r] (some safe? (for [p (range (count r))]
                                  (concat (take p r) (drop (inc p) r))))))
       count))

(lib/check
  [part1 sample] 2
  [part1 puzzle] 564
  [part2 sample] 4
  [part2 puzzle] 604)

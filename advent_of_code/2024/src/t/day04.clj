(ns t.day04
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (let [t (->> lines
               (map vec)
               vec)
        X [\X \M \A \S]]
    (for [tpl [[[0 0] [0 1] [0 2] [0 3]]
               [[0 0] [1 0] [2 0] [3 0]]
               [[0 0] [1 1] [2 2] [3 3]]
               [[0 0] [1 -1] [2 -2] [3 -3]]]
          line (range (count t))
          col (range (count (t line)))
          :let [c (->> tpl
                       (map (fn [[x y]]
                              [(+ x col) (+ y line)]))
                       (map (fn [p] (get-in t p))))]
          :when (or (= c X) (= (reverse c) X))]
      1)))

(defn solve
  [lines]
  (count lines))

(defn part1
  [input]
  (solve input))

(defn part2
  [input]
  (solve input))

(lib/check
  [part1 sample] 18
  [part1 puzzle] 2468
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

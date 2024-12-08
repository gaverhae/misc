(ns t.day04
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (map vec)
       vec))

(defn part1
  [t]
  (let [X [\X \M \A \S]]
    (->> (for [tpl [[[0 0] [0 1] [0 2] [0 3]]
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
           1)
         count)))

(defn part2
  [t]
  (let [tpl [[0 0]       [0 2]
                   [1 1]
             [2 0]       [2 2]]
        X [\M \A \S]]
    (->> (for [line (range (count t))
               col (range (count (t line)))
               :let [c (->> tpl
                            (map (fn [[x y]]
                                   [(+ x col) (+ y line)]))
                            (mapv (fn [p] (get-in t p))))
                     b1 [(c 0) (c 2) (c 4)]
                     b2 [(c 1) (c 2) (c 3)]]
               :when (and (or (= b1 X) (= (reverse b1) X))
                          (or (= b2 X) (= (reverse b2) X)))]
           1)
         count)))

(lib/check
  [part1 sample] 18
  [part1 puzzle] 2468
  [part2 sample] 9
  [part2 puzzle] 1864)

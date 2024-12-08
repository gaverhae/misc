(ns t.day08
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  {:antennas (->> (for [y (range (count lines))
                        :let [line (get lines y)]
                        x (range (count line))
                        :let [c (get line x)]
                        :when (not= c \.)]
                    {c #{[y x]}})
                  (apply merge-with set/union {}))
   :max-x (dec (count (first lines)))
   :max-y (dec (count lines))})

(defn antinode
  [[y1 x1] [y2 x2]]
  (let [dy (- y2 y1)
        dx (- x2 x1)]
    [(+ y2 dy) (+ x2 dx)]))

(defn part1
  [{:keys [antennas max-y max-x]}]
  (->> antennas
       (mapcat (fn [[k vs]]
                 (for [v1 vs
                       v2 vs
                       :when (not= v1 v2)
                       :let [[y x] (antinode v1 v2)]
                       :when (and (<= 0 y max-y)
                                  (<= 0 x max-x))]
                   [y x])))
       set
       count))


(defn part2
  [{:keys [antennas max-y max-x]}]
  (->> antennas
       (mapcat (fn [[k vs]]
                 (for [[y1 x1] vs
                       [y2 x2] vs
                       :when (not= [y1 x1] [y2 x2])
                       :let [dy (- y2 y1)
                             dx (- x2 x1)]]
                   (->> (iterate (fn [[y x]] [(+ y dy) (+ x dx)]) [y1 x1])
                        (take-while (fn [[y x]]
                                      (and (<= 0 y max-y)
                                           (<= 0 x max-x))))))))
       (apply concat)
       set
       count))

(lib/check
  [part1 sample] 14
  [part1 puzzle] 354
  [part2 sample] 34
  [part2 puzzle] 0)

(ns t.day08
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  {:freq-to-ant (->> (for [y (range (count lines))
                           :let [line (get lines y)]
                           x (range (count line))
                           :let [c (get line x)]
                           :when (not= c \.)]
                       {c #{[y x]}})
                     (apply merge-with set/union {}))
   :in-bounds? (let [max-y (dec (count lines))
                     max-x (dec (count (first lines)))]
                 (fn [[y x]]
                   (and (<= 0 y max-y)
                        (<= 0 x max-x))))})

(defn step
  [[y1 x1] [y2 x2]]
  (let [dy (- y2 y1)
        dx (- x2 x1)]
    (fn [[y x]] [(+ y dy) (+ x dx)])))

(defn part1
  [{:keys [freq-to-ant in-bounds?]}]
  (->> freq-to-ant
       (mapcat (fn [[_frequency antennas]]
                 (for [a1 antennas
                       a2 antennas
                       :when (not= a1 a2)
                       :let [antinode ((step a1 a2) a2)]
                       :when (in-bounds? antinode)]
                   antinode)))
       set
       count))

(defn part2
  [{:keys [freq-to-ant in-bounds?]}]
  (->> freq-to-ant
       (mapcat (fn [[_frequency antennas]]
                 (for [a1 antennas
                       a2 antennas
                       :when (not= a1 a2)]
                   (->> (iterate (step a1 a2) a1)
                        (take-while in-bounds?)))))
       (apply concat)
       set
       count))

(lib/check
  [part1 sample] 14
  [part1 puzzle] 354
  [part2 sample] 34
  [part2 puzzle] 1263)

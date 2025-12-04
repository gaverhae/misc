(ns t.day04
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (->> text
       (string/split-lines)
       (map-indexed (fn [y line]
                      (->> line
                           (keep-indexed (fn [x c]
                                          (when (= \@ c)
                                            [y x]))))))
       (apply concat)
       set))

(defn neighbours
  [[y x]]
  #{[(dec y) (dec x)] [(dec y) x] [(dec y) (inc x)]
    [     y  (dec x)]             [     y  (inc x)]
    [(inc y) (dec x)] [(inc y) x] [(inc y) (inc x)]})

(defn part1
  [input]
  (->> input
       (filter (fn [p] (< (count (set/intersection input (neighbours p))) 4)))
       count))

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day04-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day04-sample.txt")
      (slurp)
      (parse)
      part1)
13

  (-> (io/resource "day04-input.txt")
      (slurp)
      (parse)
      part1)
1533

  (-> (io/resource "day04-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day04-input.txt")
      (slurp)
      (parse)
      part2)

         )

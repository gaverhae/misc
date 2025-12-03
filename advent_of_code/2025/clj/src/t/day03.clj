(ns t.day03
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (-> text))

(defn part1
  [input]
  (->> input))

(defn part2
  [input]
  (->> input))

(comment

  (-> (io/resource "day03-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day03-sample.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day03-input.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day03-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day03-input.txt")
      (slurp)
      (parse)
      part2)

         )

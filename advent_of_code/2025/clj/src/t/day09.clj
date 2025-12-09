(ns t.day09
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (->> text
       string/split-lines
       (map (fn [line] (let [[x y] (string/split line #",")]
                         [(parse-long x)
                          (parse-long y)])))
       set))

(defn part1
  [input]
  )

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day09-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day09-sample.txt")
      (slurp)
      (parse)
      (part1))

  (-> (io/resource "day09-input.txt")
      (slurp)
      (parse)
      (part1))

  (-> (io/resource "day09-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day09-input.txt")
      (slurp)
      (parse)
      part2)

         )

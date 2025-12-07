(ns t.day07
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  )

(defn part1
  [text]
  )

(defn part2
  [text]
  )

(comment

  (-> (io/resource "day07-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day07-sample.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day07-input.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day07-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day07-input.txt")
      (slurp)
      (parse)
      part2)

         )

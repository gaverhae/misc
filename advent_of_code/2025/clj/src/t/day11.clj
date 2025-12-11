(ns t.day11
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [instaparse.core :as insta]))

(defn parse
  [text]
  )

(defn part1
  [input]
  )

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day11-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day11-sample.txt")
      (slurp)
      (parse)
      (part1))

  (-> (io/resource "day11-input.txt")
      (slurp)
      (parse)
      (part1))

  (-> (io/resource "day11-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day11-input.txt")
      (slurp)
      (parse)
      part2)

         )

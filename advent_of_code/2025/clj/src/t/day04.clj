(ns t.day04
  (:require [clojure.string :as string]
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

(defn part1
  [input]
  )

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

  (-> (io/resource "day04-input.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day04-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day04-input.txt")
      (slurp)
      (parse)
      part2)

         )

(ns t.day03
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (-> text
      (string/split-lines)
      (->> (map (fn [s] (map (comp parse-long str) s))))))

(defn max-from-bank
  [bank]
  (let [d1 (apply max (butlast bank))
        d2 (->> bank
                (drop-while #(not= d1 %))
                rest
                (apply max))]
    (+ (* 10 d1) d2)))

(defn part1
  [input]
  (->> input
       (map max-from-bank)
       (reduce + 0)))

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
357

  (-> (io/resource "day03-input.txt")
      (slurp)
      (parse)
      part1)
17524

  (-> (io/resource "day03-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day03-input.txt")
      (slurp)
      (parse)
      part2)

         )

(ns t.day03
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (-> text
      (string/split-lines)
      (->> (map (fn [s] (map (comp parse-long str) s))))))

(defn max-from-bank
  [bank n]
  (loop [n n
         bank bank
         result 0]
    (if (= n 0)
      result
      (let [d (apply max (drop-last (dec n) bank))]
        (recur (dec n)
               (->> bank
                    (drop-while #(not= d %))
                    rest)
               (+ (* 10 result) d))))))

(defn part1
  [input]
  (->> input
       (map #(max-from-bank % 2))
       (reduce + 0)))

(defn part2
  [input]
  (->> input
       (map #(max-from-bank % 12))
       (reduce + 0)))

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
3121910778619

  (-> (io/resource "day03-input.txt")
      (slurp)
      (parse)
      part2)
173848577117276

         )

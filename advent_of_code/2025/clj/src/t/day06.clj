(ns t.day06
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (let [numbers (->> text
                     string/split-lines
                     butlast
                     (map (fn [line]
                            (->> (string/split line #" +")
                                 (remove #{""})
                                 (map parse-long))))
                     (apply mapv vector))
        ops (-> text
                string/split-lines
                last
                (string/split #" +")
                vec)]
    (->> numbers
         (map-indexed (fn [idx line]
                        (cons (get ops idx) line))))))

(defn part1
  [input]
  (->> input
       (map (fn [[op & args]]
              (apply (case op "+" + "*" *) args)))
       (reduce + 0)))

(defn part2
  [nput]
  )

(comment

  (-> (io/resource "day06-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day06-sample.txt")
      (slurp)
      (parse)
      part1)
4277556

  (-> (io/resource "day06-input.txt")
      (slurp)
      (parse)
      part1)
5524274308182

  (-> (io/resource "day06-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day06-input.txt")
      (slurp)
      (parse)
      part2)

         )

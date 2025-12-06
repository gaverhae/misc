(ns t.day06
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  text)

(defn part1
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
                        (cons (get ops idx) line)))
         (map (fn [[op & args]]
                (apply (case op "+" + "*" *) args)))
         (reduce + 0))))

(defn part2
  [text]
  (let [numbers (->> text
                     string/split-lines
                     butlast
                     (apply mapv vector)
                     (partition-by (fn [n] (every? #{\space} n)))
                     (remove (fn [n] (and (= 1 (count n))
                                          (every? #{\space} (first n)))))
                     (map (fn [line]
                            (->> line
                                 (map (fn [n]
                                        (->> n
                                             (remove #{\space})
                                             (apply str)
                                             parse-long)))
                                 reverse))))
        ops (-> text
                string/split-lines
                last
                (string/split #" +"))]
    (->> (map cons ops numbers)
         reverse
         (map (fn [[op & args]]
                (apply (case op "+" + "*" *) args)))
         (reduce + 0))))

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
3263827

  (-> (io/resource "day06-input.txt")
      (slurp)
      (parse)
      part2)
8843673199391

         )

(ns t.day05
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (let [fresh (->> text
                   (string/split-lines)
                   (take-while #(not= "" %))
                   (map (fn [line] (let [[begin end] (string/split line #"-")]
                                     [(parse-long begin) (parse-long end)]))))
        available (->> text
                       (string/split-lines)
                       (drop-while #(not= "" %))
                       rest
                       (map parse-long))]
    {:fresh fresh
     :available available}))

(defn is-fresh?
  [fresh]
  (fn [ingredient]
    (loop [to-check fresh]
      (if (empty? to-check)
        false
        (let [[[begin end] & to-check] to-check]
          (if (<= begin ingredient end)
            true
            (recur to-check)))))))

(defn part1
  [{:keys [fresh available]}]
  (->> available
       (filter (is-fresh? fresh))
       count))

(defn part2
  [{:keys [fresh]}]
  (->> fresh
       (mapcat (fn [[begin end]] (range begin (inc end))))
       set
       count))

(comment

  (-> (io/resource "day05-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day05-sample.txt")
      (slurp)
      (parse)
      part1)
3

  (-> (io/resource "day05-input.txt")
      (slurp)
      (parse)
      part1)
558

  (-> (io/resource "day05-sample.txt")
      (slurp)
      (parse)
      part2)
14

  (-> (io/resource "day05-input.txt")
      (slurp)
      (parse)
      part2)

         )

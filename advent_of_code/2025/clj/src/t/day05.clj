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

(defn part1
  [input]
  )

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day05-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day05-sample.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day05-input.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day05-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day05-input.txt")
      (slurp)
      (parse)
      part2)

         )

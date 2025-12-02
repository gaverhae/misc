(ns t.day02
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (-> text
      (string/split #",")
      (->> (map (fn [r] (string/split r #"-")))
           (map (fn [[a b]] [(parse-long a) (parse-long b)])))))

(defn part1
  [input])

(defn part2
  [input])

(comment

  (-> (io/resource "day02-sample.txt")
      (slurp)
      (parse))


         )

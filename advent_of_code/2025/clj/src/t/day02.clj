(ns t.day02
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (-> text
      (string/split-lines)
      first
      (string/split #",")
      (->> (map (fn [r]  (string/split r #"-")))
           (map (fn [[a b]] [(parse-long a) (parse-long b)])))))

(defn part1
  [input]
  (->> input
       (mapcat (fn [[a b]] (range a (inc b))))
       (filter (fn [d]
                 (let [s (str d)
                       c (count s)
                       c2 (quot c 2)]
                   (and (zero? (mod c 2))
                        (= (take c2 s) (drop c2 s))))))
       (reduce + 0)))

(defn part2
  [input]
  (->> input
       (mapcat (fn [[a b]] (range a (inc b))))
       (filter (fn [d]
                 (let [s (str d)
                       c (count s)]
                   (->> (range 2 (inc (count s)))
                        (filter (fn [n] (zero? (mod c n))))
                        (map (fn [n] (partition (/ c n) s)))
                        (some (fn [d] (apply = d)))))))
       (reduce + 0)))

(comment

  (-> (io/resource "day02-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day02-sample.txt")
      (slurp)
      (parse)
      part1)
1227775554

  (-> (io/resource "day02-input.txt")
      (slurp)
      (parse)
      part1)
24157613387

  (-> (io/resource "day02-sample.txt")
      (slurp)
      (parse)
      part2)
4174379265

  (-> (io/resource "day02-input.txt")
      (slurp)
      (parse)
      part2)
33832678380

         )

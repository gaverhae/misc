(ns t.day02
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (-> text
      (string/split-lines)
      first
      (string/split #",")
      (->> (map (fn [r] (prn r)  (string/split r #"-")))
           (map (fn [[a b]] [(parse-long a) (parse-long b)])))))

(defn part1
  [input]
  (->> input
       (mapcat (fn [[a b]] (prn [a b]) (range a (inc b))))
       (filter (fn [d]
                 (let [s (str d)
                       c (count s)
                       c2 (quot c 2)]
                   (and (zero? (mod c 2))
                        (= (take c2 s) (drop c2 s))))))
       (reduce + 0)))

(defn part2
  [input])

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

         )

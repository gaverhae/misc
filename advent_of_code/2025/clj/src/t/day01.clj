(ns t.day01
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            #_[t.lib :as lib]))

(defn parse
  [text]
  (->> text
       (string/split-lines)
       (map (fn [line]
              (* (case (first line)
                   \L -1
                   \R +1)
                 (parse-long (subs line 1)))))))

(defn part1
  [input]
  (reduce (fn [[num-0 cur-pos] steps]
            (let [new-pos (mod (+ cur-pos steps) 100)]
              [(cond-> num-0
                 (zero? new-pos) inc)
               new-pos]))
          [0 50]
          input))

(defn part2
  [input]
  (reduce (fn [[num-0 cur-pos] steps]
            (let [sum (+ cur-pos steps)]
              (prn [num-0 cur-pos :-> sum :-> (cond (zero? sum) (inc num-0)
                     (and (neg? sum) (pos? cur-pos)) (+ num-0 (quot sum 100) 1)
                     :else (+ num-0 (quot sum 100))) (mod sum 100)])
              [(cond (zero? sum) (inc num-0)
                     (and (neg? sum) (pos? cur-pos)) (+ num-0 (quot sum 100) 1)
                     :else (+ num-0 (quot sum 100)))
               (mod sum 100)]))
          [0 50]
          input))

(comment
  (-> (io/resource "day01_sample.txt")
      (slurp)
      (parse))
  (-> (io/resource "day01_sample.txt")
      (slurp)
      (parse)
      part1)
[3 32]
  (-> (io/resource "day01_input.txt")
      (slurp)
      (parse)
      part1)
[1048 8]

  (-> (io/resource "day01_sample.txt")
      (slurp)
      (parse)
      part2)
[6 32]

(-> (io/resource "day01_input.txt")
    (slurp)
    (parse)
    part2)
[2450 8]
[2242 8]
[6504 8]


  )



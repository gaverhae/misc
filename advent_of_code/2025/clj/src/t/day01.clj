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
            (let [new-pos (+ cur-pos steps)]
              [(loop [n num-0
                      p new-pos]
                 (cond (<= 0 p 99) n
                       (< p 0) (recur (inc n) (+ p 100))
                       (> p 99) (recur (inc n) (- p 100))))
               (mod new-pos 100)]))
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
[6504 8]


  )



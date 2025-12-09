(ns t.day09
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (->> text
       string/split-lines
       (map (fn [line] (let [[x y] (string/split line #",")]
                         [(parse-long x)
                          (parse-long y)])))
       set))

(defn part1
  [input]
  (let [ts (vec input)
        c (count ts)]
    (->> (for [i1 (range c)
               i2 (range i1 c)
               :let [[x1 y1] (get ts i1)
                     [x2 y2] (get ts i2)]]
           (abs (* (- x2 x1 -1) (- y2 y1 -1))))
         sort
         last)))

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day09-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day09-sample.txt")
      (slurp)
      (parse)
      (part1))
50

  (-> (io/resource "day09-input.txt")
      (slurp)
      (parse)
      (part1))
4763802550

  (-> (io/resource "day09-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day09-input.txt")
      (slurp)
      (parse)
      part2)

         )

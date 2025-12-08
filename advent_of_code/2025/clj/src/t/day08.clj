(ns t.day08
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (->> text
       string/split-lines
       (mapv (fn [line]
               (let [[x y z] (string/split line #",")]
                 [(parse-long x)
                  (parse-long y)
                  (parse-long z)])))))

(defn sq
  ^long [^long n]
  (* n n))

(defn distance
  [[^long x1 ^long y1 ^long z1] [^long x2 ^long y2 ^long z2]]
  (+ (sq (- x2 x1))
     (sq (- y2 y1))
     (sq (- z2 z1))))

(defn part1
  [input n]
  (let [size (count input)
        distances (->> (for [idx1 (range size)
                             idx2 (range idx1 size)
                             :let [j1 (get input idx1)
                                   j2 (get input idx2)]]
                         [(distance j1 j2) j1 j2]

  )

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day08-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day08-sample.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day08-input.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day08-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day08-input.txt")
      (slurp)
      (parse)
      part2)

         )

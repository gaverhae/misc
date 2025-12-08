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
  (loop [circuits {}
         to-process (->> (for [idx1 (range (count input))
                               idx2 (range (inc idx1) (count input))
                               :let [j1 (get input idx1)
                                     j2 (get input idx2)]]
                           [(distance j1 j2) j1 j2])
                         sort
                         (take n))]
    (if (empty? to-process)
      (->> circuits
           vals
           set
           (sort-by count)
           reverse
           (take 3)
           )
      (let [[[_ j1 j2] & to-process] to-process]
        (recur (-> circuits
                   (update j1 (fnil conj #{j1}) j2)
                   (update j2 (fnil conj #{j2}) j1))
               to-process)))))

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
      (part1 10))

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

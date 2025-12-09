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
       vec))

(defn part1
  [input]
  (let [c (count input)]
    (->> (for [i1 (range c)
               i2 (range i1 c)
               :let [[x1 y1] (get input i1)
                     [x2 y2] (get input i2)]]
           (* (inc (abs (- x2 x1)))
              (inc (abs (- y2 y1)))))
         sort
         last)))

(defn inside?
  [[_ [x1 y1] [x2 y2]] [px py]]
  (and (< x1 px x2)
       (< y1 py y2)))

(defn has-bad-point?
  ;; assume "interior" is on the right of traveler
  [path]
  (let [bad-points (->> path
                        (mapcat (fn [[[x1 y1] [x2 y2]]]
                                  (cond (and (= x1 x2) (< y1 y2)) (->> (range (inc y1) y2)
                                                                       (map (fn [y]
                                                                              [(inc x1) y])))
                                        (and (= x1 x2) (> y1 y2)) (->> (range (dec y1) y2 -1)
                                                                       (map (fn [y]
                                                                              [(dec x1) y])))
                                        (and (= y1 y2) (< x1 x2)) (->> (range (inc x1) x2)
                                                                       (map (fn [x]
                                                                              [x (dec y1)])))
                                        (and (= y1 y2) (> x1 x2)) (->> (range (dec x1) x2 -1)
                                                                       (map (fn [x]
                                                                              [x (inc y1)]))))))
                        set)]
    (fn [r]
      (->> bad-points
           (filter #(inside? r %))
           first
           boolean))))

(defn part2
  [input]
  (let [borders (concat (partition 2 1 input)
                        [[(last input) (first input)]])
        c (count input)]
    (->> (for [i1 (range c)
               i2 (range i1 c)
               :let [[x1 y1] (get input i1)
                     [x2 y2] (get input i2)]]
           [(* (inc (abs (- x2 x1)))
               (inc (abs (- y2 y1))))
            [(min x1 x2) (min y1 y2)]
            [(max x1 x2) (max y1 y2)]])
         sort
         reverse
         (remove (has-bad-point? borders))
         first)))

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
4764078684

  (-> (io/resource "day09-sample.txt")
      (slurp)
      (parse)
      part2)
[24 [2 3] [9 5]]

  (-> (io/resource "day09-input.txt")
      (slurp)
      (parse)
      part2)
[1652344888 [5786 50191] [94817 68749]]

         )

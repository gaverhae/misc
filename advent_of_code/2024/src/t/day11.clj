(ns t.day11
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (-> lines
      first
      (string/split #" ")
      (->> (map ->long))))

(defn step
  [stones]
  (->> stones
       (mapcat (fn [s]
                 (let [c (count (str s))]
                   (cond (zero? s) [1]
                         (even? c) [(->long (subs (str s) 0 (/ c 2)))
                                    (->long (subs (str s) (/ c 2) c))]
                         :else [(* 2024 s)]))))))

(defn part1
  [stones n]
  (->> (iterate step stones)
       (drop n)
       first
       count))

(defn memo-walk
  [memo stone n]
  (cond (zero? n) [memo 1]
        (contains? memo [stone n]) [memo (get memo [stone n])]
        :else
        (let [[memo c] (reduce (fn [[memo tot] s]
                                 (let [[memo c] (memo-walk memo s (dec n))]
                                   [memo (+ c tot)]))
                               [memo 0]
                               (step [stone]))]
          [(assoc memo [stone n] c) c])))

(defn part2
  [stones n]
  (->> stones
       (reduce (fn [[m tot] stone]
                 (let [[m c] (memo-walk m stone n)]
                   [m (+ c tot)]))
               [{} 0])
       second))

(lib/check
  [part1 sample 1] 7
  [part1 sample1 6] 22
  [part1 sample1 25] 55312
  [part1 puzzle 25] 217812
  [part2 puzzle 25] 217812
  [part2 puzzle 75] 259112729857522)

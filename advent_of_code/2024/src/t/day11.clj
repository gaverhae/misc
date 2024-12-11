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

(defn part2
  [input]
  input)

(lib/check
  [part1 sample 1] 7
  [part1 sample1 6] 22
  [part1 sample1 25] 55312
  [part1 puzzle 25] 217812
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

(ns t.day03
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (mapcat (fn [l]
                 (re-seq #"mul\(\d{1,3},\d{1,3}\)|do\(\)|don't\(\)" l)))
       (map (fn [s]
              (case (subs s 0 3)
                "mul" (let [[_ a b] (re-matches #"mul\((\d+),(\d+)\)" s)]
                        (* (->long a) (->long b)))
                "don" :off
                "do(" :on)))))

(defn part1
  [input]
  (->> input
       (remove #{:on :off})
       (reduce + 0)))

(defn part2
  [input]
  (->> input
       (reduce (fn [[on? tot] el]
                 (match [on? el]
                   [_ :on] [true tot]
                   [_ :off] [false tot]
                   [true n] [true (+ tot n)]
                   [false _] [false tot]))
               [true 0])
       second))

(lib/check
  [part1 sample] 161
  [part1 puzzle] 156388521
  [part2 sample1] 48
  [part2 puzzle] 75920122)

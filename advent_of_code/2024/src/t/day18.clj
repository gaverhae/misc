(ns ^:test-refresh/focus t.day18
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (map (fn [l] (-> l (string/split #",") (->> (map parse-long)))))))

(defn generate-moves
  [grid size]
  (fn [[[x y] cost]]
    (->> [[(inc x) y] [(dec x) y] [x (inc y)] [x (dec y)]]
         (filter (fn [[x y]] (and (<= 0 x (dec size)) (<= 0 y (dec size)))))
         (filter (fn [[x y]] (not= :wall (grid [x y]))))
         (map (fn [p] [p (inc cost)])))))

(defn part1
  [input size num-bytes]
  (let [grid (->> input
                  (take num-bytes)
                  (reduce (fn [acc el] (assoc acc (vec el) :wall)) {}))]
    (lib/dijkstra-search
      [0 0]
      (fn [[x y]] (= (dec size) x y))
      (generate-moves grid size))))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample 7 12] 22
  [part1 puzzle 71 1024] 316
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

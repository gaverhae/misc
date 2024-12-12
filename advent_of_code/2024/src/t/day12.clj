(ns ^:test-refresh/focus t.day12
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> (for [j (range (count lines))
             i (range (count (get lines j)))]
         [[j i] (get-in lines [j i])])
       (into {})))

(defn neighbours
  [[y x]]
  [[(inc y) x] [(dec y) x] [y (inc x)] [y (dec x)]])

(defn calculate-fence-cost
  [grid p]
  (let [plant (get grid p)]
    (loop [perimeter 0
           area 0
           to-process #{p}
           used #{}
           seen #{}]
      (if (empty? to-process)
        [used (* perimeter area)]
        (let [[p & to-process] to-process]
          (if (= plant (get grid p))
              (recur (+ perimeter (->> (neighbours p)
                                       (remove (fn [p]
                                                 (= (get grid p) plant)))
                                       count))
                     (inc area)
                     (set/union (set to-process)
                                (->> (neighbours p)
                                     (remove seen)
                                     (filter (fn [p]
                                               (get grid p)))
                                     set))
                     (conj used p)
                     (conj seen p))
            (recur perimeter area to-process used (conj seen p))))))))

(defn part1
  [grid]
  (loop [to-process (set (keys grid))
         total-so-far 0]
    (if (empty? to-process)
      total-so-far
      (let [p (first to-process)
            to-process (set (rest to-process))
            [used fence-cost] (calculate-fence-cost grid p)]
        (recur (set/difference to-process used)
               (+ total-so-far fence-cost))))))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 140
  [part1 sample1] 772
  [part1 sample2] 1930
  [part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

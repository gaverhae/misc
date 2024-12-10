(ns t.day10
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  {:grid (->> (for [y (range (count lines))
                    x (range (count (get lines y)))]
                [[y x] (->long (str (get-in lines [y x])))])
              (into {}))
   :width (count (first lines))
   :height (count lines)})

(defn neighbours
  [[y x]]
  [[(inc y) x] [(dec y) x] [y (inc x)] [y (dec x)]])

(defn count-trails
  [{:keys [grid width height]} agg]
  (->> (for [y (range height)
             x (range width)
             :when (zero? (get grid [y x]))]
         (loop [ps (agg [[y x]])
                v 0]
           (if (= 9 v)
             (count ps)
             (recur (->> ps
                         (mapcat neighbours)
                         (filter (fn [p] (get grid p)))
                         (filter (fn [p] (= (inc v) (get grid p))))
                         agg)
                    (inc v)))))
       (reduce + 0)))

(defn part1
  [input]
  (count-trails input set))


(defn part2
  [input]
  (count-trails input vec))

(lib/check
  [part1 sample] 36
  [part1 puzzle] 754
  [part2 sample] 81
  [part2 puzzle] 1609)

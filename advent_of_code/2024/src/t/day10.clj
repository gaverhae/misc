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

(defn part1
  [{:keys [grid width height]}]
  (->> (for [y0 (range height)
             x0 (range width)
             :when (zero? (get grid [y0 x0]))]
         (loop [ps #{[y0 x0]}
                v 0]
           (if (= 9 v)
             (count ps)
             (recur (->> ps
                         (mapcat neighbours)
                         (filter (fn [p] (get grid p)))
                         (filter (fn [p] (= (inc v) (get grid p))))
                         set)
                    (inc v)))))
       (reduce + 0)))


(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 36
  [part1 puzzle] 754
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

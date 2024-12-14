(ns ^:test-refresh/focus t.day14
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (map (fn [line]
              (re-find #"p=(\d+),(\d+) v=(-?\d+),(-?\d+)" line)))
       (map (fn [[_ & p]] (map ->long p)))
       (mapv (fn [[x y dx dy]] [[y x] [dy dx]]))))

(defn part1
  [robots width height]
  robots)

(defn part2
  [input]
  input)

(lib/check
  [part1 sample 11 7] 12
  #_#_[part1 puzzle 101 103] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

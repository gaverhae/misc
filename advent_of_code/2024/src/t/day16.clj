(ns ^:test-refresh/focus t.day16
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  {:grid (->> lines (mapv vec))
   :start (->> lines
               (keep-indexed (fn [y line]
                               (->> line
                                    (keep-indexed (fn [x c]
                                                    (when (= c \S)
                                                      [y x])))
                                    first)))
               first)
   :end (->> lines
             (keep-indexed (fn [y line]
                             (->> line
                                  (keep-indexed (fn [x c]
                                                  (when (= c \E)
                                                    [y x])))
                                  first)))
             first)})

(def clockwise
  (->> [[0 1] [1 0] [0 -1] [-1 0]]
       cycle
       (partition 2 1)
       (take 4)
       (reduce #(apply assoc %1 %2) {})))

(def counter-clockwise
  (->> clockwise
       (map (fn [[k v]] [v k]))
       (into {})))

(defn part1
  [{:keys [grid start end]}]
  (lib/dijkstra-search
    [start [0 1]]
    (fn [[p _]] (= p end))
    (fn [[[[y x] [dy dx]] cost :as arg]]
      (->> [[[[y x] (clockwise [dy dx])] (+ 1000 cost)]
            [[[y x] (counter-clockwise [dy dx])] (+ 1000 cost)]
            [[[(+ dy y) (+ dx x)] [dy dx]] (+ 1 cost)]]
           (filter (fn [[[p d] c]]
                     (contains? #{\S \E \.} (get-in grid p))))))))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 7036
  [part1 sample1] 11048
  [part1 puzzle] 103512
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

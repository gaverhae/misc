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

(defn step
  [width height]
  (fn [robot]
    (let [[[y x] [dy dx]] robot]
      [[(mod (+ y dy) height)
        (mod (+ x dx) width)]
       [dy dx]])))

(defn by-quadrants
  [width height]
  (let [y-split (quot height 2)
        x-split (quot width 2)
        find-q (fn [s p]
                 (cond (< p s) 0
                       (= p s) nil
                       (> p s) 1))]
    (fn [robot]
      (let [[[y x] _] robot]
        (when-let [h (find-q x x-split)]
          (when-let [v (find-q y y-split)]
            [v h]))))))

(defn part1
  [robots width height]
  (->> robots
       (map (fn [r] (-> (iterate (step width height) r)
                        (nth 100))))
       (keep (by-quadrants width height))
       frequencies
       vals
       (reduce * 1)))

(defn draw
  [left top right bottom robots]
  (let [as-grid (->> robots
                     (map first)
                     frequencies)]
    (->> (range top (inc bottom))
         (map (fn [y]
                (->> (range left (inc right))
                     (map (fn [x] (if (as-grid [y x]) "•" " ")))
                     (apply str)))))))

(defn part2
  [robots width height n-steps]
  (->> robots
       (map (fn [r] (nth (iterate (step width height) r)
                         n-steps)))
       (draw 39 35 69 67)))

(lib/check
  [part1 sample 11 7] 12
  [part1 puzzle 101 103] 236628054
  [part2 puzzle 101 103 7584] ["•••••••••••••••••••••••••••••••"
                               "•                             •"
                               "•                             •"
                               "•                             •"
                               "•                             •"
                               "•              •              •"
                               "•             •••             •"
                               "•            •••••            •"
                               "•           •••••••           •"
                               "•          •••••••••          •"
                               "•            •••••            •"
                               "•           •••••••           •"
                               "•          •••••••••          •"
                               "•         •••••••••••         •"
                               "•        •••••••••••••        •"
                               "•          •••••••••          •"
                               "•         •••••••••••         •"
                               "•        •••••••••••••        •"
                               "•       •••••••••••••••       •"
                               "•      •••••••••••••••••      •"
                               "•        •••••••••••••        •"
                               "•       •••••••••••••••       •"
                               "•      •••••••••••••••••      •"
                               "•     •••••••••••••••••••     •"
                               "•    •••••••••••••••••••••    •"
                               "•             •••             •"
                               "•             •••             •"
                               "•             •••             •"
                               "•                             •"
                               "•                             •"
                               "•                             •"
                               "•                             •"
                               "•••••••••••••••••••••••••••••••"])

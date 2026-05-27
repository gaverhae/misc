(ns t.day06
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (let [w (count (first lines))
        h (count lines)]
    (->> (for [y (range h)
               x (range w)]
           [y x])
         (reduce (fn [acc p]
                   (case (get-in lines p)
                     \. acc
                     \# (update acc 2 conj p)
                     \^ (assoc acc 3 p)))
                 [w h #{} nil]))))

(def turn
  (->> [[-1 0] [0 1] [1 0] [0 -1]]
       cycle (partition 2 1) (take 4) (map vec) (into {})))

(defn step
  [p d]
  [(+ (get p 0) (get d 0))
   (+ (get p 1) (get d 1))])

(defn inside?
  [p w h]
  (let [y (get p 0)
        x (get p 1)]
    (and (<= 0 x) (< x w) (<= 0 y) (< y h))))

(defn find-path
  [w h obs g d]
  (loop [p g
         d d
         visited? #{[p d]}
         path [p]]
    (let [nxt (step p d), v [nxt d]]
      (cond
        (visited? v) :loop
        (contains? obs nxt) (recur p (turn d) visited? path)
        (inside? nxt w h) (recur nxt d (conj visited? v) (conj path nxt))
        :else path))))

(defn part1
  [[w h o g]]
  (count (into #{} (find-path w h o g [-1 0]))))

(defn part2
  [[w h o g]]
  (->> (find-path w h o g [-1 0])
       (partition 2 1)
       (filter (fn [[g n]]
                 (let [dy (- (get n 0) (get g 0))
                       dx (- (get n 1) (get g 1))]
                   (= :loop (find-path w h (conj o n) g [dy dx])))))
       count))

(lib/check
  [part1 sample] 41
  [part1 puzzle] 5067
  [part2 sample] 6
  [part2 puzzle] 1793)

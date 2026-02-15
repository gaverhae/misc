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
                     \# (update acc :obstacles conj p)
                     \^ (assoc acc :guard p)))
                 {:width w, :height h, :obstacles #{}}))))

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
  [w h obs g]
  (loop [p g
         d [-1 0]
         visited? #{[p d]}]
    (let [nxt (step p d), v [nxt d]]
      (cond
        (visited? v) :loop
        (contains? obs nxt) (recur p (turn d) visited?)
        (inside? nxt w h) (recur nxt d (conj visited? v))
        :else (into #{} (map first) visited?)))))

(defn part1
  [{:keys [width height obstacles guard]}]
  (count (find-path width height obstacles guard)))

(defn part2
  [{:keys [width height obstacles guard]}]
  (->> (find-path width height obstacles guard)
       (filter (fn [obs]
                 (= :loop (find-path width height (conj obstacles obs) guard))))
       count))

(lib/check
  [part1 sample] 41
  [part1 puzzle] 5067
  [part2 sample] 6
  [part2 puzzle] 1793)

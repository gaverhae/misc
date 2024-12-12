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

(defn find-area
  [grid p]
  (let [plant (get grid p)]
    (loop [to-process #{p}
           seen #{}
           area #{}]
      (if (empty? to-process)
        area
        (let [[p & to-process] to-process]
          (recur (set (concat to-process
                              (->> (neighbours p)
                                   (filter (fn [p] (= plant (get grid p))))
                                   (remove seen)
                                   (filter grid))))
                 (conj seen p)
                 (if (= plant (get grid p))
                   (conj area p)
                   area)))))))

(defn find-fences
  [area]
  (->> area
       (mapcat (fn [[y0 x0 :as p]]
                 (->> (neighbours p)
                      (remove area)
                      (map (fn [[y x]]
                             (cond (= x0 x) [[:hori y0 y] x (inc x)]
                                   (= y0 y) [[:vert x0 x] y (inc y)]))))))))

(defn part1
  [grid]
  (loop [to-process (set (keys grid))
         total-so-far 0]
    (if (empty? to-process)
      total-so-far
      (let [p (first to-process)
            area (find-area grid p)
            fences (find-fences area)]
        (recur (set/difference to-process area)
               (+ total-so-far
                  (* (count area) (count fences))))))))

(defn find-sides
  [area]
  (let [same-side? (fn [[b1 from1 to1]]
                     (fn [[b2 from2 to2]]
                       (when (and (= b1 b2) (or (= to1 from2) (= to2 from1)))
                         [b2 from2 to2])))
        merge-fences (fn [[b from1 to1] [_ from2 to2]]
                       [b (min from1 from2) (max to1 to2)])]
  (loop [to-process (set (find-fences area))
         full-sides []]
    (if (empty? to-process)
      full-sides
      (let [p1 (first to-process)]
        (if-let [p2 (some (same-side? p1) (rest to-process))]
          (recur (-> to-process (disj p1) (disj p2) (conj (merge-fences p1 p2)))
                 full-sides)
          (recur (-> to-process (disj p1))
                 (-> full-sides (conj p1)))))))))

(defn part2
  [grid]
  (loop [to-process (set (keys grid))
         total-so-far 0]
    (if (empty? to-process)
      total-so-far
      (let [p (first to-process)
            area (find-area grid p)
            sides (find-sides area)]
        (recur (set/difference to-process area)
               (+ total-so-far
                  (* (count area)
                     (count sides))))))))

(lib/check
  [part1 sample] 140
  [part1 sample1] 772
  [part1 sample2] 1930
  [part1 puzzle] 1522850
  [part2 sample] 80
  [part2 sample1] 436
  [part2 sample2] 1206
  [part2 sample3] 236
  [part2 sample4] 368
  [part2 puzzle] 953738)

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
               (+ total-so-far (long fence-cost)))))))

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
                             (cond (= x0 x) [:hori (/ (+ y0 y) 2) x (inc x)]
                                   (= y0 y) [:vert (/ (+ x0 x) 2) y (inc y)]))))))))

(defn count-sides
  [area]
  (loop [to-process (find-fences area)
         full-sides []]
    (if (empty? to-process)
      (count full-sides)
      (let [[[dir1 dim1 from1 to1 :as p1] & to-process] to-process]
        (if-let [[_ _ from2 to2 :as p2] (->> to-process
                                             (filter (fn [[dir2 dim2 from2 to2]]
                                                       (and (= dir1 dir2)
                                                            (= dim1 dim2)
                                                            (or (= to2 from1)
                                                                (= to1 from2)))))
                                             first)]
          (recur (-> to-process
                     set
                     (disj p2)
                     (conj [dir1 dim1 (min from1 from2) (max to1 to2)]))
                 full-sides)
          (recur to-process
                 (conj full-sides p1)))))))

(defn calculate-discounted-fence-cost
  [grid p]
  (let [area (find-area grid p)]
    [area (* (count area) (count-sides area))]))

(defn part2
  [grid]
  (loop [to-process (set (keys grid))
         total-so-far 0]
    (if (empty? to-process)
      total-so-far
      (let [p (first to-process)
            to-process (set (rest to-process))
            [used fence-cost] (calculate-discounted-fence-cost grid p)]
        (recur (set/difference to-process used)
               (+ total-so-far (long fence-cost)))))))

(lib/check
  [part1 sample] 140
  [part1 sample1] 772
  [part1 sample2] 1930
  [part1 puzzle] 1522850
  [part2 sample] 80
  [part2 sample1] 436
  [part2 sample2] 1206
  [part2 puzzle] 0)

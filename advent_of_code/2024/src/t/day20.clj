(ns ^:test-refresh/focus t.day20
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.day16]
            [t.lib :as lib :refer [->long]]))

(def parse t.day16/parse)

(let [dirs [[1 0] [-1 0] [0 1] [0 -1]]]
  (defn generate-moves
    [valid-pos? [y x] cheated? cost]
    (concat (->> dirs
                 (map (fn [[dy dx]]
                        [(+ y dy) (+ x dx)]))
                 (filter valid-pos?)
                 (map (fn [p] [p cheated? (inc cost)])))
            (when (not cheated?)
              (->> dirs
                   (map (fn [[dy dx]]
                          [(+ y dy) (+ x dx)]))
                   (mapcat (fn [[y x]]
                             (->> dirs
                                  (map (fn [[dy dx]]
                                         [[(+ y dy) (+ x dx)] [y x]])))))
                   (mapcat (fn [[[y x] c1]]
                             (->> dirs
                                  (map (fn [[dy dx]]
                                         [[(+ y dy) (+ x dx)] [c1 [y x]]])))))
                   (filter (fn [[p c]] (valid-pos? p)))
                   (map (fn [[p c]] [p c (+ 3 cost)])))))))

(defn all-paths
  [{:keys [valid-pos? start end]}]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (:cost x) (:cost y))))]
    (.add to-visit [start false 0])
    (loop [min-cost {}]
      (if (.isEmpty to-visit)
        (let [cost-no-cheat (min-cost [end false])]
          (->> min-cost
               (filter (fn [[[pos cheated?] c]] (= pos end)))
               (filter (fn [[[pos cheated?] c]] (<= c cost-no-cheat)))
               (map (fn [[[pos cheated?] c]] [cheated? c]))
               (into {})))
        (let [[pos cheated? cost] (.poll to-visit)]
          (if (> cost (min-cost [pos cheated?] Long/MAX_VALUE))
            (recur min-cost)
            (do (doseq [[pos cheated? cost :as nxt-state] (generate-moves valid-pos? pos cheated? cost)]
                  (when (< cost (min-cost [pos cheated?] Long/MAX_VALUE))
                    (.add to-visit nxt-state)))
                (recur (update min-cost [pos cheated?] (fnil min Long/MAX_VALUE) cost)))))))))

(defn part1
  [input saves-at-least]
  (let [cost-per-cheat (all-paths input)
        cost-with-no-cheat (cost-per-cheat false)]
    (->> cost-per-cheat
         (filter (fn [[cheat c]] (>= (- cost-with-no-cheat c) saves-at-least))))))


(defn part2
  [input]
  input)

(lib/check
  [part1 sample 2] 44
  #_#_[part1 sample 4] 30
  #_#_[part1 sample 6] 16
  #_#_[part1 sample 8] 14
  #_#_[part1 sample 10] 10
  #_#_[part1 sample 12] 8
  #_#_[part1 sample 20] 5
  #_#_[part1 sample 36] 4
  #_#_[part1 sample 38] 3
  #_#_[part1 sample 40] 2
  #_#_[part1 sample 64] 1
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

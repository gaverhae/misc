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
                 (map (fn [p] [p (inc cost) cheated?])))
            (when (not cheated?)
              (->> dirs
                   (map (fn [[dy dx]]
                          [[(+ y dy) (+ x dx)] [y x]]))
                   (mapcat (fn [[[y x] c1]]
                             (->> dirs
                                  (map (fn [[dy dx]]
                                         [[(+ y dy) (+ x dx)] [c1 [y x]]])))))
                   (filter (fn [[p c]] (valid-pos? p)))
                   (map (fn [[p c]] [p (+ 2 cost) c])))))))

(defn cheating-paths
  [{:keys [valid-pos? start end]} max-cost]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (:cost x) (:cost y))))]
    (.add to-visit [start 0 false])
    (loop [min-cost {}]
      (if (.isEmpty to-visit)
        (->> min-cost
             (filter (fn [[[pos cheated?] c]] (= pos end)))
             (map (fn [[[pos cheated?] c]] [cheated? c]))
             (into {}))
        (let [[pos cost cheated?] (.poll to-visit)]
          (if (> cost (min-cost [pos cheated?] max-cost))
            (recur min-cost)
            (do (doseq [[pos cost cheated? :as nxt-state] (generate-moves valid-pos? pos cheated? cost)]
                  (when (<= cost (min-cost [pos cheated?] max-cost))
                    (.add to-visit nxt-state)))
                (recur (update min-cost [pos cheated?] (fnil min Long/MAX_VALUE) cost)))))))))

(defn part1
  [{:keys [valid-pos? start end] :as input} saves-at-least]
  (let [cost-with-no-cheat (lib/dijkstra-search
                             start
                             #(= % end)
                             #(generate-moves valid-pos? (% 0) true (% 1)))
        max-cost (- cost-with-no-cheat saves-at-least)]
    (prn [cost-with-no-cheat saves-at-least max-cost (->> (cheating-paths input max-cost)
                                                          (map (fn [[cheat cost]] cost))
                                                          sort)])
    (count (cheating-paths input max-cost))))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample 2] 44
  [part1 sample 4] 30
  [part1 sample 6] 16
  #_#_[part1 sample 8] 14
  #_#_[part1 sample 10] 10
  #_#_[part1 sample 12] 8
  #_#_[part1 sample 20] 5
  #_#_[part1 sample 36] 4
  #_#_[part1 sample 38] 3
  #_#_[part1 sample 40] 2
  #_#_[part1 sample 64] 1
  #_#_[part1 puzzle 100] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

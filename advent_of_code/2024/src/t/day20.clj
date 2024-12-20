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
    [valid-pos? [y x] cheat-done? cost]
    (concat (->> dirs
                 (map (fn [[dy dx]]
                        [(+ y dy) (+ x dx) cheat-done?]))
                 (filter (fn [[p c]] (valid-pos? p)))
                 (map (fn [s] [s (inc cost)])))
            (when (not cheat-done?)
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
                   (map (fn [[p c]] [[p c] (+ 3 cost)])))))))

(defn all-paths
  [{:keys [valid-pos? start end]}]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (:cost x) (:cost y))))]
    (.add to-visit {:cost 0, :pos start, :cheated? false})
    (loop [min-cost {}]
      (if (.isEmpty to-visit)
        min-cost
        (let [{:keys [cost pos cheated?] :as state} (.poll to-visit)]
          (if (> cost (min-cost cheated? Long/MAX_VALUE))
            (recur min-cost)
            (do (doseq [{:keys [cost pos cheated?] :as nxt-state} (generate-moves valid-pos? pos cheated? cost)]
                  (when (< cost (min-cost cheated? Long/MAX_VALUE))
                    (.add to-visit nxt-state)))
                (recur (-> min-cost (cond-> (= end pos) (assoc cheated? cost)))))))))))

(defn part1
  [input min-cheat])

(defn part2
  [input]
  input)

(lib/check
  [part1 sample 2] 44
  [part1 sample 4] 30
  [part1 sample 6] 16
  [part1 sample 8] 14
  [part1 sample 10] 10
  [part1 sample 12] 8
  [part1 sample 20] 5
  [part1 sample 36] 4
  [part1 sample 38] 3
  [part1 sample 40] 2
  [part1 sample 64] 1
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

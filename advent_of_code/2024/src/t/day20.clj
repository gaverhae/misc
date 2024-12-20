(ns ^:test-refresh/focus t.day20
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.day16]
            [t.lib :as lib :refer [->long]]))

(def parse t.day16/parse)

(def dirs
  [[1 0] [-1 0] [0 1] [0 -1]])

(defn generate-moves
  [path [y x] cheated? cost]
  (let [[next-step] (path [y x])]
    (concat [[next-step (inc cost) cheated?]]
            (when (not cheated?)
              (for [[dy1 dx1] dirs
                    [dy2 dx2] dirs
                    :let [y' (+ y dy1 dy2), x' (+ x dx1 dx2)]
                    :when (not= [y' x'] [y x])
                    :let [[_ cost-to-finish end] (path [y' x'])]
                    :when cost-to-finish]
                [end (+ cost 2 cost-to-finish) [(+ y dy1) (+ x dx1)]])))))

(defn cheating-paths
  [no-cheat-path start end max-cost]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (:cost x) (:cost y))))]
    (.add to-visit [start 0 false])
    (loop [min-cost {}]
      (if (.isEmpty to-visit)
        (->> min-cost
             (filter (fn [[[pos cheated?] c]] (= pos end)))
             (map (fn [[[pos cheated?] c]] [cheated? c]))
             (into {}))
        (let [[pos cost cheated?] (.poll to-visit)]
          (if (or (> cost (min-cost [pos cheated?] max-cost))
                  (> cost (min-cost [pos false] max-cost)))
            (recur min-cost)
            (do (doseq [[pos cost cheated? :as nxt-state] (generate-moves no-cheat-path pos cheated? cost)]
                  (when (and (<= cost (min-cost [pos cheated?] max-cost))
                             (<= cost (min-cost [pos false] max-cost)))
                    (.add to-visit nxt-state)))
                (recur (update min-cost [pos cheated?] (fnil min Long/MAX_VALUE) cost)))))))))

(defn trace-moves
  [valid-pos? [[y x :as state] cost]]
  (let [hist (:history (meta state))]
    (for [[dy dx] dirs
          :let [y (+ y dy), x (+ x dx)]
          :when (valid-pos? [y x])]
      [(with-meta [y x] {:history (cons state hist)})
       (inc cost)])))

(defn trace-no-cheat-path
  [initial end valid-pos?]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (first x) (first y))))]
    (loop [[cost state] [0 initial]
           visited #{}]
      (when (not (visited state))
        (doseq [[nxt-state nxt-cost] (trace-moves valid-pos? [state cost])]
          (when (not (visited nxt-state))
            (.add to-visit [nxt-cost nxt-state]))))
      (if (= end state)
        [cost (->> state meta :history (cons end) (cons [-1 -1]) (partition 2 1)
                   (map-indexed (fn [idx [to from]] [from [to idx end]]))
                   (reduce (fn [acc [k v]] (assoc acc k v)) {}))]
        (recur (.poll to-visit)
               (conj visited state))))))

(defn part1
  [{:keys [valid-pos? start end] :as input} saves-at-least]
  (let [[no-cheat-cost no-cheat-path] (trace-no-cheat-path start end valid-pos?)
        max-cost (- no-cheat-cost saves-at-least)]
    (count (cheating-paths no-cheat-path start end max-cost))))

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
  [part1 puzzle 4000] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

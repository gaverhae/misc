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
  [valid-pos? [y x] cheated? cost]
  (concat (for [[dy dx] dirs
                :let [y (+ y dy), x (+ x dx)]
                :when (valid-pos? [y x])]
            [[y x] (inc cost) cheated?])
          (when (not cheated?)
            (for [[dy1 dx1] dirs
                  [dy2 dx2] dirs
                  :let [y' (+ y dy1 dy2), x' (+ x dx1 dx2)]
                  :when (not= [y' x'] [y x])
                  :when (valid-pos? [y' x'])]
              [[y' x'] (+ cost 2) [(+ y dy1) (+ x dx1)]]))))

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
          (if (or (> cost (min-cost [pos cheated?] max-cost))
                  (> cost (min-cost [pos false] max-cost)))
            (recur min-cost)
            (do (doseq [[pos cost cheated? :as nxt-state] (generate-moves valid-pos? pos cheated? cost)]
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
  [initial final? valid-pos?]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (first x) (first y))))]
    (loop [[cost state] [0 initial]
           visited #{}]
      (when (not (visited state))
        (doseq [[nxt-state nxt-cost] (trace-moves valid-pos? [state cost])]
          (when (not (visited nxt-state))
            (.add to-visit [nxt-cost nxt-state]))))
      (if (final? state)
        [cost (set (:history (meta state)))]
        (recur (.poll to-visit)
               (conj visited state))))))

(defn part1
  [{:keys [valid-pos? start end] :as input} saves-at-least]
  (let [[no-cheat-cost no-cheat-path] (trace-no-cheat-path start #(= % end) valid-pos?)
        max-cost (- no-cheat-cost saves-at-least)]
    (count (cheating-paths (assoc input :valid-pos? (conj no-cheat-path end))
                           max-cost))))

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
  #_#_[part1 puzzle 7000] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

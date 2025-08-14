(ns t.day20
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.day16]
            [t.lib :as lib :refer [->long]]))

(def parse t.day16/parse)

(defn generate-moves
  [max-cheats path [y x] cheated? cost]
  (let [[next-step] (path [y x])]
    (concat [[next-step (inc cost) cheated?]]
            (when (not cheated?)
              (loop [remaining-steps max-cheats
                     positions [[y x 0]]]
                (if (zero? remaining-steps)
                  (->> positions
                       (keep (fn [[y' x' c]]
                               (when-let [[_ cost-to-finish end] (path [y' x'])]
                                 [end (+ cost c cost-to-finish) [[y x] [y' x']]]))))
                  (recur (dec remaining-steps)
                         (->> positions
                              (mapcat (fn [[y x c]]
                                        [[y x c]
                                         [(inc y) x (inc c)]
                                         [(dec y) x (inc c)]
                                         [y (inc x) (inc c)]
                                         [y (dec x) (inc c)]]))
                              (reduce (fn [acc [y x c]]
                                        (update acc [y x] (fnil min Long/MAX_VALUE) c))
                                      {})
                              (map (fn [[[y x] c]] [y x c]))))))))))

(defn cheating-paths
  [max-cheats no-cheat-path start end max-cost]
  (loop [to-visit [[start 0 false]]
         min-cost {}]
    (if (empty? to-visit)
      (->> min-cost
           (filter (fn [[[pos cheated?] c]] (= pos end)))
           (map (fn [[[pos cheated?] c]] [cheated? c]))
           (into {}))
      (let [[[pos cost cheated?] & to-visit] to-visit]
        (if (or (> cost (min-cost [pos cheated?] max-cost))
                (> cost (min-cost [pos false] max-cost)))
          (recur to-visit min-cost)
          (recur (concat (for [[pos cost cheated? :as nxt-state] (generate-moves max-cheats no-cheat-path pos cheated? cost)
                            :when (and (<= cost (min-cost [pos cheated?] max-cost))
                                       (<= cost (min-cost [pos false] max-cost)))]
                           nxt-state)
                         to-visit)
                 (update min-cost [pos cheated?] (fnil min Long/MAX_VALUE) cost)))))))

(defn trace-moves
  [valid-pos? [[y x :as state] cost]]
  (let [hist (:history (meta state))]
    (for [[dy dx] [[1 0] [-1 0] [0 1] [0 -1]]
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
  ([input] (part1 input 100))
  ([{:keys [valid-pos? start end] :as input} saves-at-least]
   (let [[no-cheat-cost no-cheat-path] (trace-no-cheat-path start end valid-pos?)
         max-cost (- no-cheat-cost saves-at-least)]
     (count (cheating-paths 2 no-cheat-path start end max-cost)))))

(defn part2
  ([input] (part2 input 100))
  ([{:keys [valid-pos? start end] :as input} saves-at-least]
   (let [[no-cheat-cost no-cheat-path] (trace-no-cheat-path start end valid-pos?)
         max-cost (- no-cheat-cost saves-at-least)]
     (count (cheating-paths 20 no-cheat-path start end max-cost)))))

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
  [part1 puzzle] 1323
  [part2 sample 76] 3
  [part2 sample 74] 7
  [part2 sample 72] 29
  [part2 sample 70] 41
  [part2 sample 68] 55
  [part2 sample 66] 67
  [part2 sample 64] 86
  [part2 sample 62] 106
  [part2 sample 60] 129
  [part2 sample 58] 154
  [part2 sample 56] 193
  [part2 sample 54] 222
  [part2 sample 52] 253
  [part2 sample 50] 285
  [part2 puzzle] 983905)

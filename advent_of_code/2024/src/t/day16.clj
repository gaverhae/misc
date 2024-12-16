(ns ^:test-refresh/focus t.day16
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (map-indexed vector)
       (mapcat (fn [[y line]]
                 (->> line
                      (keep-indexed (fn [x c]
                                      (when (not= \# c)
                                     [[y x] c]))))))
       (reduce (fn [acc [p c]]
                 (cond-> (update acc :valid-pos? (fnil conj #{}) p)
                   (= c \S) (assoc :start p)
                   (= c \E) (assoc :end p)))
               {})))

(defn generate-moves
  [valid-pos? s]
  (let [turn (fn [s]
               (match (:dir s)
                 [0 _] [[1 0] [-1 0]]
                 [_ 0] [[0 1] [0 -1]]))
        new-pos (fn [{[y x] :pos, [dy dx] :dir}]
                  (valid-pos? [(+ y dy) (+ x dx)]))]
    (concat (map #(-> s (assoc :dir %) (update :cost + 1000))
                 (turn s))
            (when-let [p (new-pos s)]
              [(-> s (assoc :pos p) (update :hist conj p) (update :cost + 1))]))))

(defn solve
  [{:keys [valid-pos? start end]}]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (:cost x) (:cost y))))]
    (.add to-visit {:cost 0, :pos start, :dir [0 1], :hist [start]})
    (loop [min-cost {}
           best-cost nil
           traversed #{}]
      (if (.isEmpty to-visit)
        [best-cost (count traversed)]
        (let [{:keys [cost pos dir hist] :as state} (.poll to-visit)]
          (if (> cost (min-cost [pos dir] Long/MAX_VALUE))
            (recur min-cost best-cost traversed)
            (do (doseq [{:keys [pos dir cost] :as nxt-state} (generate-moves valid-pos? state)]
                  (when (< cost (min-cost [pos dir] Long/MAX_VALUE))
                    (.add to-visit nxt-state)))
                (let [final? (= end pos)
                      best-cost (or best-cost (and final? cost))]
                  (recur (assoc min-cost [pos dir] cost)
                         best-cost
                         (if (and final? (= cost best-cost))
                           (reduce conj traversed hist)
                           traversed))))))))))

(defn part1
  [input]
  (first (solve input)))

(defn part2
  [input]
  (second (solve input)))

(lib/check
  [part1 sample] 7036
  [part1 sample1] 11048
  [part1 puzzle] 103512
  [part2 sample] 45
  [part2 sample1] 64
  [part2 puzzle] 554)

(ns ^:test-refresh/focus t.day16
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  {:grid (->> lines (mapv vec))
   :start (->> lines
               (keep-indexed (fn [y line]
                               (->> line
                                    (keep-indexed (fn [x c]
                                                    (when (= c \S)
                                                      [y x])))
                                    first)))
               first)
   :end (->> lines
             (keep-indexed (fn [y line]
                             (->> line
                                  (keep-indexed (fn [x c]
                                                  (when (= c \E)
                                                    [y x])))
                                  first)))
             first)})

(def clockwise
  (->> [[0 1] [1 0] [0 -1] [-1 0]]
       cycle
       (partition 2 1)
       (take 4)
       (reduce #(apply assoc %1 %2) {})))

(def counter-clockwise
  (->> clockwise
       (map (fn [[k v]] [v k]))
       (into {})))

(defn part1
  [{:keys [grid start end]}]
  (lib/dijkstra-search
    [start [0 1]]
    (fn [[p _]] (= p end))
    (fn [[[[y x] [dy dx]] cost :as arg]]
      (->> [[[[y x] (clockwise [dy dx])] (+ 1000 cost)]
            [[[y x] (counter-clockwise [dy dx])] (+ 1000 cost)]
            [[[(+ dy y) (+ dx x)] [dy dx]] (+ 1 cost)]]
           (filter (fn [[[p d] c]]
                     (contains? #{\S \E \.} (get-in grid p))))))))

(defn dijkstra-all
  [initial final? generate-moves]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (first x) (first y))))]
    (.add to-visit [0 initial])
    (loop [visited #{}
           final-cost nil
           good-paths []]
      (if (.isEmpty to-visit)
        good-paths
        (let [[cost state] (.poll to-visit)]
          (cond
            (and (final? state) (or (nil? final-cost) (= cost final-cost)))
            (recur (conj visited state) cost (conj good-paths state))
            (and final-cost (> cost final-cost))
            (recur (conj visited state) final-cost good-paths)
            :else
            (do (when (not (visited state))
                  (doseq [[nxt-state nxt-cost] (generate-moves [state cost])]
                    (when (not (visited nxt-state))
                      (.add to-visit [nxt-cost nxt-state]))))
                (recur (conj visited state) final-cost good-paths))))))))

(defn part2
  [{:keys [grid start end]}]
  (->> (dijkstra-all
         [[start] [0 1]]
         (fn [[[p] _]] (= p end))
         (fn [[[[[y x] :as hist] [dy dx]] cost :as arg]]
           (->> [[[hist (clockwise [dy dx])] (+ 1000 cost)]
                 [[hist (counter-clockwise [dy dx])] (+ 1000 cost)]
                 [[(cons [(+ dy y) (+ dx x)] hist) [dy dx]] (+ 1 cost)]]
                (filter (fn [[[[p] d] c]]
                          (contains? #{\S \E \.} (get-in grid p)))))))
       (mapcat (fn [[hist final-dir]] hist))
       set
       count))

(lib/check
  [part1 sample] 7036
  [part1 sample1] 11048
  [part1 puzzle] 103512
  [part2 sample] 45
  [part2 sample1] 64
  [part2 puzzle] 0)

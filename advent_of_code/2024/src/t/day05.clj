(ns t.day05
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (let [rules (->> lines
                   (take-while #(not= "" %))
                   (map (fn [r] (string/split r #"\|")))
                   (map (fn [r] (map ->long r)))
                   (reduce (fn [acc [bef af]]
                             (update acc bef (fnil conj #{}) af))
                           {}))
        updates (->> lines
                     (drop-while #(not= "" %))
                     rest
                     (map (fn [line]
                            (->> (string/split line #",")
                                 (mapv ->long)))))]
    [rules updates]))

(defn is-safe?
  [rules]
  (fn [u]
    (->> (range (count u))
         (map (fn [c] [(take c u) (get u c)]))
         (every? (fn [[befs p]]
                   (empty? (set/intersection (set befs)
                                             (rules p #{}))))))))

(defn part1
  [[rules updates]]
  (->> updates
       (filter (is-safe? rules))
       (map (fn [u] (get u (quot (count u) 2))))
       (reduce + 0)))

(defn reorder
  [rules]
  (fn [u]
    (let [safe? (is-safe? rules)
          len (count u)]
      (loop [safe-u [(first u)]
             to-place (rest u)
             index-to-try 0]
        (if (= (count safe-u) len)
          safe-u
          (let [new-u (vec (concat (take index-to-try safe-u)
                                   [(first to-place)]
                                   (drop index-to-try safe-u)))]
            (if (safe? new-u)
              (recur new-u (rest to-place) 0)
              (recur safe-u to-place (inc index-to-try)))))))))

(defn part2
  [[rules updates]]
  (->> updates
       (remove (is-safe? rules))
       (map (reorder rules))
       (map (fn [u] (get u (quot (count u) 2))))
       (reduce + 0)))

(lib/check
  [part1 sample] 143
  [part1 puzzle] 5108
  [part2 sample] 123
  [part2 puzzle] 7380)

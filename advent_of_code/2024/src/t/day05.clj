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
                   (reduce (fn [acc [bef af]]
                             (update acc bef (fnil conj #{}) af))
                           {}))
        updates (->> lines
                     (drop-while #(not= "" %))
                     rest
                     (map (fn [line]
                            (vec (string/split line #",")))))]

    [rules updates]))

(defn is-safe?
  [rules]
  (fn [u]
    (->> (range (count u))
         (map (fn [c] [(take c u) (get u c)]))
         (every? (fn [[befs p]]
                   (empty? (set/intersection (set befs)
                                             (rules p))))))))

(defn part1
  [[rules updates]]
  (->> updates
       (filter (is-safe? rules))
       (map (fn [u] (get u (quot (count u) 2))))
       (map ->long)
       (reduce + 0)))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 143
  [part1 puzzle] 5108
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

(ns ^:test-refresh/focus t.day22
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines (map parse-long)))

(defn mix-prune
  [a op]
  (mod (bit-xor a (op a)) 16777216))

(defn next-random
  [n]
  (-> n
      (mix-prune #(* 64 %))
      (mix-prune #(quot % 32))
      (mix-prune #(* 2048 %))))

(defn part1
  [input]
  (->> input
       (map (fn [x] (nth (iterate next-random x)
                         2000)))
       (reduce + 0)))

(defn part2
  [input]
  (->> input
       (map (fn [x]
              (->> (iterate next-random x)
                   (take 2001)
                   (map (fn [r] (mod r 10)))
                   (partition 2 1)
                   (map (fn [[prev cur]] [cur (- cur prev)]))
                   (partition 4 1)
                   (map vec)
                   (reduce (fn [acc p]
                             (let [s [(get (get p 0) 1)
                                      (get (get p 1) 1)
                                      (get (get p 2) 1)
                                      (get (get p 3) 1)]
                                   v (get (get p 3) 0)]
                               (if (contains? acc s)
                                 acc
                                 (assoc acc s v))))
                           {}))))
       (reduce (fn [acc el]
                 (merge-with + acc el)))
       (sort-by (fn [[k v]] (- v)))
       first
       second))

(lib/check
  [part1 sample] 37327623
  [part1 puzzle] 19150344884
  [part2 sample1] 23
  [part2 puzzle] 2121)

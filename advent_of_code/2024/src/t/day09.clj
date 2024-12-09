(ns t.day09
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       first
       (reduce (fn [[blank? d idx] c]
                 (let [n (->long (str c))]
                   [(not blank?)
                    (concat d (repeat n (if blank? nil idx)))
                    (or (and blank? idx)
                        (inc idx))]))
               [false [] 0])
       second))

(defn trim
  [v]
  (let [v (vec v)
        idx (dec (count v))]
    (loop [idx idx]
      (if (v idx)
        (take (inc idx) v)
        (recur (dec idx))))))

(defn compact
  [disk]
  (loop [to-process (trim disk)
         processed []]
    (cond (empty? to-process)
          processed
          (first to-process)
          (recur (rest to-process)
                 (conj processed (first to-process)))
          :else
          (recur (->> to-process rest butlast trim)
                 (conj processed (last to-process))))))

(defn part1
  [input]
  (->> (compact input)
       (map-indexed *)
       (reduce + 0)))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 1928
  [part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

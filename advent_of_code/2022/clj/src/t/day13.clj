(ns t.day13
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [clojure.set :as set]
            [clojure.core.match :refer [match]]
            [instaparse.core :as insta]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (partition-by #{""})
       (remove #{[""]})
       (map (fn [pair] (mapv edn/read-string pair)))))

(defn smaller-than
  [left right]
  (loop [i 0]
    (cond (== i (count left) (count right)) :unknown
          (== i (count left)) true
          (== i (count right)) false
          (and (integer? (get left i))
               (integer? (get right i)))
          (cond (< (get left i) (get right i))
                true
                (> (get left i) (get right i))
                false
                :equals (recur (inc i)))
          (integer? (get left i))
          (let [rec (smaller-than [(get left i)] (get right i))]
            (if (= :unknown rec)
              (recur (inc i))
              rec))
          (integer? (get right i))
          (let [rec (smaller-than (get left i) [(get right i)])]
            (if (= :unknown rec)
              (recur (inc i))
              rec))
          :both-lists
          (let [r1 (smaller-than (get left i) (get right i))]
            (if (= :unknown r1)
              (recur (inc i))
              r1)))))

(defn part1
  [input]
  (->> input
       (keep-indexed
         (fn [idx [left right]]
           (when (smaller-than left right) idx)))
       (map inc)
       (reduce + 0)))

(defn part2
  [input]
  (let [sorted (->> input
                    (concat [[[[2]] [[6]]]])
                    (mapcat identity)
                    (sort-by identity (comparator smaller-than)))]
    (* (inc (.indexOf ^java.util.List sorted [[2]]))
       (inc (.indexOf ^java.util.List sorted [[6]])))))


(lib/check
  [part1 sample] 13
  [part1 puzzle] 5588
  [part2 sample] 140
  [part2 puzzle] 23958)

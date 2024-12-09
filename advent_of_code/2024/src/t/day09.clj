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
                    (conj d [(if blank? nil idx) n])
                    (or (and blank? idx)
                        (inc idx))]))
               [false [] 0])
       second))

(defn trim
  [v]
  (cond (empty? v) []
        (every? nil? v) []
        :else
        (let [v (vec v)
              idx (dec (count v))]
          (loop [idx idx]
            (if (v idx)
              (take (inc idx) v)
              (recur (dec idx)))))))

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
  (->> input
       (mapcat (fn [[f s]] (repeat s f)))
       compact
       (map-indexed *)
       (reduce + 0)))

(defn compact-whole-files
  [disk]
  (loop [to-process disk
         processed-begin []
         processed-end ()]
    (cond (empty? to-process) (concat processed-begin processed-end)
          (-> to-process peek first nil?) (recur (pop to-process)
                                                  processed-begin
                                                  processed-end)
          (ffirst to-process) (recur (vec (rest to-process))
                                     (conj processed-begin (first to-process))
                                     processed-end)
          :else
          (let [[_ free-size] (first to-process)
                [idx size] (peek to-process)
                size-diff (- free-size size)]
            (cond (neg? size-diff) (recur (pop to-process)
                                          processed-begin
                                          (conj processed-end (peek to-process)))
                  (zero? size-diff) (recur (vec (rest (pop to-process)))
                                           (conj processed-begin (peek to-process))
                                           processed-end)
                  (pos? size-diff) (recur (vec (cons [nil size-diff] (rest (pop to-process))))
                                          (conj processed-begin (peek to-process))
                                          processed-end))))))

(defn part2
  [input]
  (->> input
       compact-whole-files))

(lib/check
  [part1 sample] 1928
  #_#_[part1 puzzle] 6323641412437
  [part2 sample] 2858
  #_#_[part2 puzzle] 0)

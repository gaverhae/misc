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

(defn merge-nils
  [disk]
  disk)

(defn compact-whole-files
  [disk]
  (loop [to-process (reverse disk)
         processed disk]
    (cond (empty? to-process) processed
          (-> to-process first first nil?) (recur (rest to-process) processed)
          :else
          (let [[[f-id req-sz] & to-process] to-process
                old-idx (.indexOf ^java.util.List processed [f-id req-sz])
                available? (->> processed
                                (map-indexed vector)
                                (some (fn [[idx [file? size]]]
                                        (when (and (< idx old-idx)
                                                   (not file?)
                                                   (>= size req-sz))
                                          [idx (- size req-sz)]))))]
            (recur to-process
                   (match available?
                     nil processed
                     [idx 0] (-> processed
                                 (assoc idx [f-id req-sz])
                                 (assoc old-idx [nil req-sz]))
                     [idx n] (-> (vec (concat (take idx processed)
                                              [[f-id req-sz] [nil n]]
                                              (drop (inc idx) processed)))
                                 (assoc (inc old-idx) [nil req-sz]))))))))

(defn part2
  [input]
  (->> input
       compact-whole-files
       (mapcat (fn [[f s]] (repeat s f)))
       (map-indexed vector)
       (keep (fn [[m v]] (when v (* m v))))
       (reduce + 0)))

(lib/check
  [part1 sample] 1928
  #_#_[part1 puzzle] 6323641412437
  [part2 sample] 2858
  [part2 puzzle] 6351801932670)

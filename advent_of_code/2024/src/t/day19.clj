(ns ^:test-refresh/focus t.day19
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (let [[towels _ & patterns] lines]
    {:towels (->> (string/split towels #", ")
                  (reduce (fn [acc el]
                            (update acc (first el) (fnil conj []) el))
                          {}))
     :patterns patterns}))

(defn is-possible?
  [towels]
  (let [f (fn [rec p]
            (if (empty? p)
              true
              (->> (towels (first p))
                   (some (fn [t]
                           (and (>= (count p) (count t))
                                (= t (subs p 0 (count t)))
                                (rec rec (subs p (count t)))))))))
        memo-f (memoize f)]
    (fn [p]
      (memo-f memo-f p))))

(defn part1
  [{:keys [towels patterns]}]
  (->> patterns
       (map-indexed (fn [i x] (prn i) x))
       (filter (is-possible? towels))
       count))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 6
  [part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

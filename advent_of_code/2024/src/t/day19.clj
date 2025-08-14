(ns t.day19
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

(defn count-possible
  [towels]
  (let [f (fn [rec p]
            (if (empty? p)
              1
              (->> (towels (first p))
                   (keep (fn [t]
                           (when (and (>= (count p) (count t))
                                      (= t (subs p 0 (count t))))
                             (rec rec (subs p (count t))))))
                   (reduce + 0))))
        memo-f (memoize f)]
    (fn [p]
      (memo-f memo-f p))))

(defn part1
  [{:keys [towels patterns]}]
  (->> patterns
       (map (count-possible towels))
       (filter pos?)
       count))


(defn part2
  [{:keys [towels patterns]}]
  (->> patterns
       (map (count-possible towels))
       (reduce + 0)))

(lib/check
  [part1 sample] 6
  [part1 puzzle] 302
  [part2 sample] 16
  [part2 puzzle] 771745460576799)

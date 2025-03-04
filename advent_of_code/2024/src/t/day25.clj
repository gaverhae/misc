(ns ^:test-refresh/focus t.day25
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (partition-by #{""})
       (remove #{[""]})
       (map (fn [kol]
              [(if (= (ffirst kol) \#) :lock :key)
               (->> kol lib/transpose (map (fn [col] (->> col (filter #{\#}) count dec))))]))
       (group-by first)
       (map (fn [[k vs]] [k (->> vs (map second) (map vec))]))
       (into {})))

(defn part1
  [{:keys [lock key]}]
  (->> (for [l lock
             k key
             :when (->> (map + l k)
                        (every? (fn [x] (< x 6))))]
         1)
       count))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 3
  [part1 puzzle] 2854)

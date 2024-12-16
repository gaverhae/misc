(ns ^:test-refresh/focus t.day16
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  {:grid (->> lines (mapv vec))
   :start (->> lines
               (keep-indexed (fn [y line]
                               (->> line
                                    (keep-indexed (fn [x c]
                                                    (when (= c \S)
                                                      [y x])))
                                    first)))
               first)
   :end (->> lines
             (keep-indexed (fn [y line]
                             (->> line
                                  (keep-indexed (fn [x c]
                                                  (when (= c \E)
                                                    [y x])))
                                  first)))
             first)})

(defn part1
  [input]
  input)

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 7036
  [part1 sample1] 11048
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

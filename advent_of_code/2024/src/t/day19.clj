(ns ^:test-refresh/focus t.day19
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (let [[towels _ & patterns] lines]
    {:towels (string/split towels #", ")
     :patterns patterns}))

(defn part1
  [input]
  input)

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 6
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

(ns ^:test-refresh/focus t.day20
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  lines)

(defn part1
  [input min-cheat]
  input)

(defn part2
  [input]
  input)

(lib/check
  [part1 sample 2] 44
  [part1 sample 4] 30
  [part1 sample 6] 16
  [part1 sample 8] 14
  [part1 sample 10] 10
  [part1 sample 12] 8
  [part1 sample 20] 5
  [part1 sample 36] 4
  [part1 sample 38] 3
  [part1 sample 40] 2
  [part1 sample 64] 1
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

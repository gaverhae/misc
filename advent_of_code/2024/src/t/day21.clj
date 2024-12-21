(ns ^:test-refresh/focus t.day21
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  lines)

(defn numeric-part
  [code]
  0)

(defn shortest-length
  [c]
  0)

(defn part1
  [codes]
  (->> codes
       (map (fn [c] (* (numeric-part c)
                       (shortest-length c))))
       (reduce + 0)))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 126384
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

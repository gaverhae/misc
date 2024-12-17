(ns ^:test-refresh/focus t.day17
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (let [[a b c _ p] lines
        _ (prn a b c p)
        reg (fn [s] (let [[_ v] (re-matches #"Register .: (\d+)" s)]
                      (parse-long v)))
        prog (fn [s] (let [d (re-seq #"\d" s)]
                       (map parse-long d)))]
    {:a (reg a)
     :b (reg b)
     :c (reg c)
     :p (prog p)}))

(defn part1
  [input]
  input)

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 0
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

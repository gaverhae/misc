(ns ^:test-refresh/focus t.day22
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn mix-prune
  [a b]
  (mod (bit-xor a b) 16777216))

(defn next-random
  [n]
  (-> n
      (mix-prune (* 64 n))
      (mix-prune (quot n 32))
      (mix-prune (* 2048 n))))

(clojure.test/deftest secret-numbers
  (clojure.test/is (= [123
                       15887950
                       16495136
                       527345
                       704524
                       1553684
                       12683156
                       11100544
                       12249484
                       7753432
                       5908254]
                      (take 10 (iterate next-random 123)))))

(defn parse
  [lines]
  (->> lines (map parse-long)))

(defn part1
  [input]
  (->> lines
       (map (fn [x] (nth (iterate next-random x)
                         2000)))
       (reduce + 0)))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 37327623
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

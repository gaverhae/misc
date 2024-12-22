(ns ^:test-refresh/focus t.day22
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn mix-prune
  [a op]
  (mod (bit-xor a (op a)) 16777216))

(defn next-random
  [n]
  (-> n
      (mix-prune #(* 64 %))
      (mix-prune #(quot % 32))
      (mix-prune #(* 2048 %))))

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
                      (take 11 (iterate next-random 123)))))

(defn parse
  [lines]
  (->> lines (map parse-long)))

(defn part1
  [input]
  (->> input
       (map (fn [x] (nth (iterate next-random x)
                         2000)))
       (reduce + 0)))

(defn part2
  [input]
  (let [prices (->> input
                    (map (fn [x]
                           (->> (iterate next-random x)
                                (take 2001)
                                (map (fn [r] (mod r 10)))
                                (partition 2 1)
                                (map (fn [[prev cur]] [cur (- cur prev)]))
                                (partition 4 1)
                                (map (fn [[[_ p1] [_ p2] [_ p3] [cur p4]]]
                                       [cur [p1 p2 p3 p4]]))))))]
    (loop [to-try (->> prices (mapcat (fn [vendor] (map (fn [[cur s]] s) vendor))) set)
           best-so-far 0]
      (if (empty? to-try)
        best-so-far
        (let [[s & to-try] to-try
              result (->> prices
                          (keep (fn [vendor]
                                  (->> vendor
                                       (some (fn [[cur s']] (when (= s s') cur))))))
                          (reduce + 0)
                          long)]
          (when (> result best-so-far)
            (prn [best-so-far '-> result]))
          (recur to-try
                 (max best-so-far result)))))))

(lib/check
  [part1 sample] 37327623
  [part1 puzzle] 19150344884
  [part2 sample1] 23
  [part2 puzzle] 0)

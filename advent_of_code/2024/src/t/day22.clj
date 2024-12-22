(ns ^:test-refresh/focus t.day22
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines (map parse-long)))

(defn mix-prune
  [a op]
  (mod (bit-xor a (op a)) 16777216))

(defn next-random
  [n]
  (-> n
      (mix-prune #(* 64 %))
      (mix-prune #(quot % 32))
      (mix-prune #(* 2048 %))))

(defn part1
  [input]
  (->> input
       (map (fn [x] (nth (iterate next-random x)
                         2000)))
       (reduce + 0)))

(def mtq clojure.lang.PersistentQueue/EMPTY)

(defn part2
  [input]
  (->> input
       (map (fn [x]
              (loop [n 0
                     p nil
                     prev nil
                     secret x
                     last-4 mtq
                     m {}]
                (if (= 2000 n)
                  m
                  (let [n (inc n)
                        prev p
                        p (mod secret 10)
                        secret (next-random secret)]
                    (if (nil? prev)
                      (recur n p prev secret last-4 m)
                      (let [d (- p prev)
                            last-4 (conj last-4 d)
                            last-4 (cond-> last-4
                                     (= 5 (count last-4)) pop)
                            m (cond-> m
                                (nil? (m last-4)) (assoc last-4 p))]
                        (recur n p prev secret last-4 m))))))))
       (reduce (fn [acc el]
                 (merge-with + acc el)))
       (sort-by (fn [[k v]] (- v)))
       first
       second))

(lib/check
  [part1 sample] 37327623
  [part1 puzzle] 19150344884
  [part2 sample1] 23
  [part2 puzzle] 2121)

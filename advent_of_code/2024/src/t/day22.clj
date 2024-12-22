(ns ^:test-refresh/focus t.day22
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]])
  (:import (java.util BitSet)))

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

(comment

(->> [-3 6 -1 -1 0 2 -2 0 -2]
     (partition 4 1)
     (map (fn [s]
            (reduce (fn [acc el]
                      (+ (* acc 20) (+ 10 el)))
                    0
                    s)))
     (map (fn [d]
            (->> [(mod d 20)
                  (mod d (* 20 20))
                  (mod d (* 20 20 20))
                  (mod d (* 20 20 20 20))]
                 (map #(- % 10))))))
(= (->> (for [d1 (range -9 10)
              d2 (range -9 10)
              d3 (range -9 10)
              d4 (range -9 10)]
          [d1 d2 d3 d4])
        count)
   (->> (for [d1 (range -9 10)
              d2 (range -9 10)
              d3 (range -9 10)
              d4 (range -9 10)]
          [d1 d2 d3 d4])
        set
        count)
   (->> (for [d1 (range -9 10)
              d2 (range -9 10)
              d3 (range -9 10)
              d4 (range -9 10)]
          (reduce (fn [acc el]
                    (+ (* acc 20) 10 el))
                  0
                  [d1 d2 d3 d4]))
        count)
   (->> (for [d1 (range -9 10)
              d2 (range -9 10)
              d3 (range -9 10)
              d4 (range -9 10)]
          (reduce (fn [acc el]
                    (+ (* acc 20) 10 el))
                  0
                  [d1 d2 d3 d4]))
        set
        count))
true

(to-single-num [1 2 3 4])
93074
(to-single-num [2 3 4 5])
101495
(add-num 93074 5)
101495

(->> (for [d1 (range -9 10)
           d2 (range -9 10)
           d3 (range -9 10)
           d4 (range -9 10)
           d5 (range -9 10)
           :let [s1 (to-single-num [d1 d2 d3 d4])
                 s2 (to-single-num [d2 d3 d4 d5])]
           :when (not= s2 (add-num s1 d5))]
       [d1 d2 d3 d4 d5]))
()

)

(def mtq clojure.lang.PersistentQueue/EMPTY)

(defn to-single-num
  [[d1 d2 d3 d4]]
  (reduce (fn [acc el]
            (+ (* acc 20) 10 el))
          0
          [d1 d2 d3 d4]))

(defn add-num
  [s d]
  (mod (+ (* 20 s) 10 d) (* 20 20 20 20)))

(defn part2
  [input]
  (->> (let [div (* 20 20 20 20)
             tot (long-array div)]
         (loop [vs input]
           (if (empty? vs)
             (loop [idx 0
                    v 0]
               (if (= idx div)
                 v
                 (recur (inc idx) (max v (aget tot idx)))))
             (let [vendor (first vs)
                   vs (rest vs)
                   n 5
                   secret (nth (iterate next-random vendor)
                               (- n 2))
                   prev (mod secret 10)
                   secret (next-random secret)
                   p (mod secret 10)
                   secret (next-random secret)
                   last-4 (->> (iterate next-random vendor)
                               (take n)
                               (partition 2 1)
                               (map (fn [[a b]] (- (mod b 10) (mod a 10))))
                               to-single-num)
                   bs (BitSet. div)]
               (aset tot last-4 (long (+ (aget tot last-4) p)))
               (.set bs last-4)
               (loop [n n
                      p p
                      prev prev
                      secret secret
                      last-4 last-4]
                 (when (<= n 2000)
                   (let [n (inc n)
                         prev p
                         p (mod secret 10)
                         secret (next-random secret)
                         d (- p prev)
                         last-4 (add-num last-4 d)]
                     (when (not (.get bs last-4))
                       (aset tot last-4 (long (+ (aget tot last-4) p))))
                     (.set bs last-4)
                     (recur n p prev secret last-4))))
               (recur vs)))))))

(lib/check
  [part1 sample] 37327623
  [part1 puzzle] 19150344884
  [part2 sample1] 23
  [part2 puzzle] 2121)

(comment

  (lib/timed (part2 @puzzle))
4265

  (lib/bench #(part2 @puzzle))
"20241222.2342.d21717cf:  0,21 ±  0,00 [ 0,21  0,21]"
"20241222.2314.af0eca60:  0,26 ±  0,00 [ 0,26  0,26]"
"20241222.2215.d111a7a5:  1,08 ±  0,00 [ 1,07  1,10]"
"20241222.2151.3e622e31:  1,99 ±  0,00 [ 1,95  2,03]"
"20241222.2137.2bb65680:  2,04 ±  0,00 [ 2,02  2,06]"
"20241222.2121.96b25434:  2,72 ±  0,00 [ 2,70  2,75]"
"20241222.2044.c2b7da14:  5,10 ±  0,00 [ 5,07  5,28]"
"20241222.1622.1b5abc0c:  4,27 ±  0,00 [ 4,24  4,30]"

  )

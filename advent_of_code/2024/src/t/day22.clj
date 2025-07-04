(ns t.day22
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]])
  (:import (java.util BitSet)))

(set! *unchecked-math* :warn-on-boxed)

(defn parse
  [lines]
  (->> lines (map parse-long)))

(defmacro mix-prune
  [a f b]
  `(rem (bit-xor ~a (~f ~a ~b)) 16777216))

(defn next-random
  ^long [^long n]
  (-> n
      (mix-prune * 64)
      (mix-prune quot 32)
      (mix-prune * 2048)))

(defn part1
  [input]
  (->> input
       (map (fn [x] (nth (iterate next-random x)
                         2000)))
       (reduce + 0)))

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
                   secret (long (nth (iterate next-random vendor)
                                     (- n 2)))
                   prev (rem secret 10)
                   secret (next-random secret)
                   p (rem secret 10)
                   secret (next-random secret)
                   last-4 (->> (iterate next-random vendor)
                               (take n)
                               (partition 2 1)
                               (map (fn [[^long a ^long b]] (- (rem b 10) (rem a 10))))
                               (reduce (fn [^long acc ^long el]
                                         (+ (* acc 20) 10 el))
                                       0))
                   bs (BitSet. div)]
               (aset tot last-4 (+ (aget tot last-4) p))
               (.set bs last-4)
               (loop [n n
                      p p
                      prev prev
                      secret secret
                      last-4 ^long last-4]
                 (when (<= n 2000)
                   (let [n (inc n)
                         prev p
                         p (rem secret 10)
                         secret (next-random secret)
                         d (- p prev)
                         last-4 (rem (+ (* 20 last-4) 10 d)
                                     div)]
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
"202506121426.2245.8122587a:  0.08 ±  0.00 [ 0.07  0.08]"
"202501121859.2196.43444551:  0.05 ±  0.00 [ 0.05  0.05]"
"202505282235.2220.ea75fe02:  0.40 ±  0.00 [ 0.40  0.41]"
"202501121834.2194.956ceb1d:  0.42 ±  0.00 [ 0.42  0.43]"
"202501121756.2192.b8be9379:  0.23 ±  0.00 [ 0.23  0.24]"
"20250111.1718.5a669073:  0.22 ±  0.00 [ 0.22  0.22]"

"202505131226.2213.a2fe89bb:  0.20 ±  0.00 [ 0.20  0.20]"
"20250513.1220.2cf5641b:  0.15 ±  0.00 [ 0.15  0.15]"

"20250421.2017.7d0f0dac:  0.14 ±  0.00 [ 0.14  0.14]"
"20250421.1959.8360ffe8:  0.15 ±  0.00 [ 0.15  0.15]"

"20241222.2354.0efbf1ff:  0,15 ±  0,00 [ 0,15  0,15]"
"20241222.2342.d21717cf:  0,21 ±  0,00 [ 0,21  0,21]"
"20241222.2314.af0eca60:  0,26 ±  0,00 [ 0,26  0,26]"
"20241222.2215.d111a7a5:  1,08 ±  0,00 [ 1,07  1,10]"
"20241222.2151.3e622e31:  1,99 ±  0,00 [ 1,95  2,03]"
"20241222.2137.2bb65680:  2,04 ±  0,00 [ 2,02  2,06]"
"20241222.2121.96b25434:  2,72 ±  0,00 [ 2,70  2,75]"
"20241222.2044.c2b7da14:  5,10 ±  0,00 [ 5,07  5,28]"
"20241222.1622.1b5abc0c:  4,27 ±  0,00 [ 4,24  4,30]"

  )

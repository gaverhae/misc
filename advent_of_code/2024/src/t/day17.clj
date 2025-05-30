(ns t.day17
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (let [[a b c _ p] lines
        reg (fn [s] (let [[_ v] (re-matches #"Register .: (\d+)" s)]
                      (parse-long v)))
        prog (fn [s] (let [d (re-seq #"\d" s)]
                       (mapv parse-long d)))]
    {:a (reg a)
     :b (reg b)
     :c (reg c)
     :p (prog p)
     :i 0
     :out []}))

(defn combo
  [arg a b c]
  (cond (<= 0 arg 3) arg
        (= 4 arg) a
        (= 5 arg) b
        (= 6 arg) c))

(defn step
  [{:keys [a b c p i out] :as m}]
  (let [op (get p i)
        arg (get p (inc i))
        m (update m :i + 2)]
    (when (and op arg)
      (case (int op)
        0 (assoc m :a (quot a (long (Math/pow 2 (combo arg a b c)))))
        1 (assoc m :b (bit-xor b arg))
        2 (assoc m :b (mod (combo arg a b c) 8))
        3 (if (zero? a) m (assoc m :i arg))
        4 (assoc m :b (bit-xor b c))
        5 (update m :out conj (mod (combo arg a b c) 8))
        6 (assoc m :b (quot a (long (Math/pow 2 (combo arg a b c)))))
        7 (assoc m :c (quot a (long (Math/pow 2 (combo arg a b c)))))))))

(defn run
  [machine]
  (loop [m machine]
    (if-let [m (step m)]
      (recur m)
      m)))

(defn part1
  [input]
  (->>(run input)
       :out
       (interpose ",")
       (apply str)))

(defn part2
  [input]
  (let [machine (update input :p (fn [p] (vec (take (- (count p) 2) p))))]
    (loop [to-output (->> (:p input) reverse)
           possible-as [0]]
      (if (empty? to-output)
        (apply min possible-as)
        (let [[o & to-output] to-output]
          (recur to-output
                 (->> possible-as
                      (mapcat (fn [a] (->> (range 8)
                                           (map (fn [b] (+ (* 8 a) b))))))
                      (filter (fn [a] (= [o] (:out (run (-> machine (assoc :a a))))))))))))))

(lib/check
  [part1 sample] "4,6,3,5,6,3,5,2,1,0"
  [part1 puzzle] "1,5,0,3,7,3,0,3,1"
  [part2 sample1] 117440
  [part2 puzzle] 105981155568026)

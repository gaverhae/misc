(ns ^:test-refresh/focus t.day17
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

(defn genetic-search
  [make-sol fitness mutate crossover]
  (let [carousel (fn [p] (let [maxi (reduce max (map first p))
                               inverted (map (fn [[f i]] [(- maxi f) f i]) p)
                               total (reduce + (map first inverted))
                               roll (rand total)]
                           (loop [r roll
                                  [[f' f s] & p] inverted]
                             (if (<= r f')
                               [f s]
                               (recur (- r f') p)))))]
    (fn !
      ([]
       (! (->> (repeatedly 100 make-sol)
               (map (fn [i] [(fitness i) i]))
               sort)))
      ([init-pop]
       (loop [population (sort init-pop)
              step 0]
         (if (== step 1000)
           population
           (recur (let [survivors (concat (take 10 population)
                                          (->> (repeatedly 7 make-sol)
                                               (map (fn [i] [(fitness i) i])))
                                          (take 3 (reverse population)))
                        children (repeatedly
                                   80
                                   #(let [[_ parent1] (carousel population)
                                          [_ parent2] (carousel population)
                                          child (mutate (crossover parent1 parent2))]
                                      [(fitness child) child]))]
                    (sort (concat survivors children)))
                  (inc step))))))))

(defn part2
  [{:keys [p] :as input}]
  (let [input-size 21
        to-int (fn [v]
                 (reduce (fn [acc el] (+ (* 8 acc) el)) v))
        make-sol (fn [] (vec (repeatedly input-size #(rand-int 8))))
        fitness (fn [i]
                  (let [o (:out (run (assoc input :a (to-int i))))]
                    (->> (map = o p)
                         (filter identity)
                         count
                         (- (count p)))))
        mutate (fn [i] (assoc i (rand-int input-size) (rand-int 8)))
        crossover (fn [i1 i2]
                    (mapv (fn [x1 x2] (if (> 0.5 (rand)) x1 x2)) i1 i2))
        genetic (genetic-search make-sol fitness mutate crossover)]
    (loop [[[fit v] :as current-gen] (genetic)]
      (prn [fit v])
      (if (zero? fit)
        (to-int v)
        (recur (genetic current-gen))))))

(lib/check
  [part1 sample] "4,6,3,5,6,3,5,2,1,0"
  [part1 puzzle] "1,5,0,3,7,3,0,3,1"
  [part2 sample1] 117440
  [part2 puzzle] 105981155568026)

(ns ^:test-refresh/focus t.day24
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (let [[init [_ & gates]] (split-with #(not= "" %) lines)
        init (->> init
                  (map (fn [s] (re-matches #"(...): (\d)" s)))
                  (map (fn [[_ k v]] [k [:lit (parse-long v)]]))
                  (into {}))
        gates (->> gates
                   (map (fn [s] (re-matches #"(...) ([^ ]*) (...) -> (...)" s)))
                   (map (fn [[_ in1 op in2 out]] [out [op in1 in2]]))
                   (into {}))]
    {:wires (merge init gates)
     :output (->> (merge init gates)
                  keys
                  (filter (fn [k] (= \z (first k))))
                  sort)}))

(defn bits-to-num
  [bits]
  (->> bits
       reverse
       (reduce (fn [acc el] (+ (* 2 acc) el)) 0)))

(defn num-to-bits
  [n]
  (loop [n n
         bits ()]
    (if (zero? n)
      bits
      (recur (quot n 2)
             (cons (rem n 2) bits)))))

(defn part1
  [{:keys [output wires]}]
  (->> output
       (map (fn ! [w]
              (match (get wires w)
                [:lit n] n
                ["XOR" in1 in2] (bit-xor (! in1) (! in2))
                ["OR" in1 in2] (bit-or (! in1) (! in2))
                ["AND" in1 in2] (bit-and (! in1) (! in2)))))
       bits-to-num))

(defn part2
  [{:keys [wires output] :as input} f]
  (let [x (->> wires
               (filter (fn [[k v]] (= \x (first k))))
               sort
               (map (fn [[k [_ v]]] v))
               bits-to-num)
        y (->> wires
               (filter (fn [[k v]] (= \y (first k))))
               sort
               (map (fn [[k [_ v]]] v))
               bits-to-num)
        expected-z (->> (num-to-bits (f x y))
                        reverse
                        (map-indexed (fn [idx b]
                                       [(format "z%02d" idx) b]))
                        (into {}))
        actual-z (->> (part1 input)
                      num-to-bits
                      reverse
                      (map-indexed (fn [idx b]
                                     [(format "z%02d" idx) b]))
                      (into {}))
        wrong-z (->> actual-z
                     (remove (fn [[k v]] (= v (get expected-z k))))
                     (into {}))]

    [x y expected-z actual-z wrong-z]))

(lib/check
  [part1 sample] 4
  [part1 sample1] 2024
  [part1 puzzle] 57344080719736
  [part2 sample2 bit-and] "z00,z01,z02,z05"
  #_#_[part2 puzzle +] 0)

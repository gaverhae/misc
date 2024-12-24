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

(defn run-with-swaps
  [swaps {:keys [wires output]}]
  (->> output
       (map (fn ! [w]
              (match (get wires (swaps w w))
                [:lit n] n
                ["XOR" in1 in2] (bit-xor (! in1) (! in2))
                ["OR" in1 in2] (bit-or (! in1) (! in2))
                ["AND" in1 in2] (bit-and (! in1) (! in2)))))
       bits-to-num))

(defn find-swaps
  [swaps {:keys [wires output]} actual expected max-swaps]
  ;; let's assume this all works out for the best
  (let [wires (->> wires (map (fn [[k _]] k)) (remove (fn [k] (#{\x \y} (first k)))))
        wrong-bits (->> (map vector (num-to-bits actual) (num-to-bits expected))
                        reverse
                        (map-indexed vector)
                        (filter (fn [[idx actual expected]] (not= actual expected)))
                        (map (fn [[idx]] (format "z%02d" idx))))
        _ (prn [:wrong wrong-bits])
        wires-on-wrong-path (->> wrong-bits
                                 (mapcat (fn ! [w]
                                           (match (get wires (swaps w w))
                                             [:lit n] []
                                             [_ in1 in2] (concat [in1 in2] (! in1) (! in2)))))
                                 set)]

    (prn wires-on-wrong-path)))

(defn part2
  [{:keys [wires output] :as input} expected-f req-swaps]
  (let [len (fn [f] (->> wires (filter (fn [[k v]] (= \x (first k)))) count))
        to-wires (fn [k n]
                   (->> (num-to-bits n)
                        reverse
                        (map-indexed (fn [idx b] [(format (str k "%02d") idx) [:lit b]]))
                        (into {})))
        max-x (long (Math/pow 2 (len \x)))
        max-y (long (Math/pow 2 (len \y)))]
    (loop [x 0
           y 0
           swaps {}]
      (if (= (* 2 req-swaps) (count swaps))
        (->> swaps keys sort (interpose ",") (apply str))
        (let [expected-z (expected-f x y)
              actual-z (run-with-swaps swaps
                                       {:output output
                                        :wires (merge wires (to-wires "x" x) (to-wires "y" y))})]
          (recur (long (rand max-x)) (long (rand max-y))
                 (if (= expected-z actual-z)
                   swaps
                   (find-swaps swaps
                               {:output output
                                :wires (merge wires (to-wires "x" x) (to-wires "y" y))}
                               actual-z
                               expected-z
                               req-swaps))))))))

(lib/check
  [part1 sample] 4
  [part1 sample1] 2024
  [part1 puzzle] 57344080719736
  [part2 sample2 bit-and 2] "z00,z01,z02,z05"
  [part2 puzzle + 4] 0)

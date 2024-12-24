(ns ^:test-refresh/focus t.day24
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn gate-name
  [s]
  (let [k (first s)
        d (subs s 1)]
    (case k
      \x [:x (parse-long d)]
      \y [:y (parse-long d)]
      \z [:z (parse-long d)]
      [:inner s])))

(defn parse
  [lines]
  (let [[init [_ & gates]] (split-with #(not= "" %) lines)
        init (->> init
                  (map (fn [s] (re-matches #"(...): (.)" s)))
                  (map (fn [[_ k v]] [(gate-name k) [:lit (parse-long v)]]))
                  (into {}))
        gates (->> gates
                   (map (fn [s] (re-matches #"(...) ([^ ]*) (...) -> (...)" s)))
                   (map (fn [[_ in1 op in2 out]]
                          [(gate-name out)
                           [(case op
                              "AND" :and
                              "OR" :or
                              "XOR" :xor)
                            (gate-name in1)
                            (gate-name in2)]]))
                   (into {}))]
    {:wires (merge init gates)
     :output (->> (merge init gates)
                  keys
                  (filter (fn [k] (= :z (first k))))
                  sort
                  reverse)}))

(defn bits-to-num
  [bits]
  (->> bits
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
                [:xor in1 in2] (bit-xor (! in1) (! in2))
                [:or in1 in2] (bit-or (! in1) (! in2))
                [:and in1 in2] (bit-and (! in1) (! in2)))))
       bits-to-num))

;; bit-add is
;; z = (bit-xor (bit-xor x y) carry)
;;
;; He's probably trolling us by having more complicated expressions, but the
;; important part, I think, is that once we have a wron wiring for a given
;; output bit, we'll probably taint any number of subsequent bits with the
;; carry. This suggests an approach where I can check my output bits one at a
;; time starting from the lowest one.
;;
;; We should double-check that no output bit depends on "higher" input bits,
;; but that's probably too obvious.

(defn part2
  [{:keys [wires output] :as input}]
  (let [input-size (->> wires keys (filter (comp #{:x :y :z} first)) (map second) (apply max))
        max-input (long (Math/pow 2 (inc input-size)))
        rand-input (fn [] [(long (rand max-input)) (long (rand max-input))])
        exprs (->> output
                   (map (fn ! [w]
                          (match w
                            [:z z] [z (! (wires w))]
                            [:x x] [:x x]
                            [:y y] [:y y]
                            [:inner i] (! (wires w))
                            [op in1 in2] [op (! in1) (! in2)]))))
        _ (assert (->> exprs
                       (every? (fn ! [[n e]]
                                 (match e
                                   [:x x] (<= x n)
                                   [:y y] (<= y n)
                                   [_ a b] (and (! [n a]) (! [n b])))))))
        eval-expr (fn [e x y] false)
        valid-expr? (fn [[n expr]]
                      (->> (repeatedly 1000 rand-input)
                           (every? (fn [[x y]] (eval-expr expr x y)))))]
    (->> exprs
         (remove valid-expr?)
         (map first))))

(lib/check
  [part1 sample] 4
  [part1 sample1] 2024
  [part1 puzzle] 57344080719736
  [part2 puzzle] 0)

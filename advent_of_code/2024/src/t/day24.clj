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

(defn eval-expr
  [e x y]
  (let [bit-x (vec (reverse (num-to-bits x)))
        bit-y (vec (reverse (num-to-bits y)))
        ev (fn ! [e]
             (match e
               [:x x'] (get bit-x x' 0)
               [:y y'] (get bit-y y' 0)
               [:and e1 e2] (bit-and (! e1) (! e2))
               [:or e1 e2] (bit-or (! e1) (! e2))
               [:xor e1 e2] (bit-xor (! e1) (! e2))))]
    (ev e)))

(defn make-genetic
  [make-sol fitness crossover mutate]
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
      ([] (! (->> (repeatedly 1000 make-sol)
                  (map (fn [i] [(fitness i) i]))
                  sort)))
      ([init-pop]
       (loop [population (sort init-pop)
              step 0]
         (prn [(java.util.Date.) :step step :best (ffirst population)
               (->> (first population)
                    second
                    first
                    (map (fn [[a b]] (if (= a :inner) b (format "%s%02d" (name a) b))))
                    sort
                    (interpose ",")
                    (apply str))])
         (if false #_(== step 1000)
           population
           (recur (let [survivors (concat (take 10 population)
                                          (take 3 (reverse population)))
                        spontaneous (->> (repeatedly 7 make-sol)
                                         (map (fn [s] [(fitness s) s])))
                        children (->> (range 980)
                                      (pmap (fn [_]
                                              (let [[_ parent1] (carousel population)
                                                    [_ parent2] (carousel population)
                                                    child (mutate (crossover parent1
                                                                             parent2))]
                                                [(fitness child) child]))))]
                    (sort (concat survivors children)))
                  (inc step))))))))

(defn has-cycle?
  [wires outputs]
  (let [cycle? (fn !
                 ([w] (! #{} w))
                 ([seen? w]
                  (if (seen? w)
                    true
                    (let [s (conj seen? w)]
                      (match w
                        [:lit _] false
                        [_ _] (! s (wires w))
                        [op arg1 arg2] (or (! s arg1)
                                           (! s arg2)))))))]
    (->> outputs
         (reduce (fn [acc el]
                   (or acc (cycle? el)))
                 false))))

(defn run
  [wires outputs x-bits y-bits]
  (let [f (fn [rec w]
            (match w
              [:lit n] n
              [:x x] (get x-bits x 0)
              [:y y] (get y-bits y 0)
              [_ _] (rec rec (wires w))
              [op a1 a2] ((case op :or bit-or :xor bit-xor :and bit-and)
                          (rec rec a1)
                          (rec rec a2))))
        memo-f (memoize f)]
    (->> outputs
         (mapv (fn [o]
                 (memo-f memo-f o))))))

(defn to-formula
  [wires n]
  (let [f (fn ! [w]
            (match w
              [:lit n] n
              [:x x] [:x x]
              [:y y] [:y y]
              [:z z] [:z z]
              [:inner l] (! (wires w))
              [op a b] [op (! a) (! b)]))]
    (f (get wires [:z n]))))

(defn expected
  [^long n]
  (case n
    0 [:xor [:x 0] [:y 0]]
    1 [:xor
       [:and [:x 0] [:y 0]]
       [:xor [:x 1] [:y 1]]]
    [:xor
     [:xor [:x n] [:y n]]
     [:or
      (assoc (expected (dec n)) 0 :and)
      [:and [:x (dec n)] [:y (dec n)]]]]))

(defn equiv
  [f1 f2]
  (or (= f1 f2)
      (let [[op1 a1 b1] f1
            [op2 a2 b2] f2]
        (and (= (count f1) (count f2) 3)
             (= op1 op2)
             (or (and (equiv a1 a2) (equiv b1 b2))
                 (and (equiv a1 b2) (equiv b1 a2)))))))

(expected 5)
[:xor [:xor [:x 5] [:y 5]] [:or [:and [:xor [:x 4] [:y 4]] [:or [:and [:xor [:x 3] [:y 3]] [:or [:and [:xor [:x 2] [:y 2]] [:or [:and [:and [:x 0] [:y 0]] [:xor [:x 1] [:y 1]]] [:and [:x 1] [:y 1]]]] [:and [:x 2] [:y 2]]]] [:and [:x 3] [:y 3]]]] [:and [:x 4] [:y 4]]]]
[:xor [:or [:and [:x 4] [:y 4]] [:and [:or [:and [:x 3] [:y 3]] [:and [:or [:and [:xor [:y 2] [:x 2]] [:or [:and [:and [:x 0] [:y 0]] [:xor [:x 1] [:y 1]]] [:and [:y 1] [:x 1]]]] [:and [:y 2] [:x 2]]] [:xor [:x 3] [:y 3]]]] [:xor [:x 4] [:y 4]]]] [:and [:y 5] [:x 5]]] [:xor [:xor [:x 5] [:y 5]] [:or [:and [:xor [:x 4] [:y 4]] [:or [:and [:xor [:x 3] [:y 3]] [:or [:and [:xor [:x 2] [:y 2]] [:or [:and [:and [:x 0] [:y 0]] [:xor [:x 1] [:y 1]]] [:and [:x 1] [:y 1]]]] [:and [:x 2] [:y 2]]]] [:and [:x 3] [:y 3]]]] [:and [:x 4] [:y 4]]]]


(defn part2
  [{:keys [wires output] :as input}]
  (->> output
       (map (fn [[_ n]]
              [n (to-formula wires n) (expected n)]))
       (remove (fn [[_ actual expected]] (equiv actual expected)))
       (mapv prn))
  #_(let [input-size (->> wires keys (filter (comp #{:x :y :z} first)) (map second) (apply max))
        swappable (->> wires keys)
        max-input (long (Math/pow 2 (inc input-size)))
        rand-input (fn [] [(long (rand max-input)) (long (rand max-input))])
        test-inputs (->> (repeatedly 100 rand-input)
                         (mapv (fn [[x y]]
                                 [(vec (num-to-bits x))
                                  (vec (num-to-bits y))
                                  (vec (num-to-bits (+ x y)))])))
        max-score (->> test-inputs (map (fn [[x y z]] (count z))) (reduce + 0))
        fitness (fn [[swaps swap-output?]]
                  (if (not= 8 (count (set swaps)))
                    max-score
                    (let [kvs (->> swaps (partition 2) (map vec))
                          vks (->> kvs (map reverse) (map vec))
                          swaps (into {} (concat kvs vks))
                          sw (->> wires
                                  (map (fn [[out v]] [(swaps out out) v]))
                                  (into {}))
                          outs (if swap-output?
                                 (map (fn [o] (swaps o o)) output)
                                 output)]
                      (if (has-cycle? sw outs)
                        max-score
                        (->> (for [[x y z] test-inputs
                                   :let [result (run sw outs x y)]
                                   idx (range (count z))
                                   :when (= (get z idx) (get result idx))]
                               1)
                             (reduce - max-score))))))
        make-sol (fn [] [(->> swappable shuffle (take 8) vec) (> (rand) 0.5)])
        crossover (fn [[i1 o1] [i2 o2]]
                    [(mapv (fn [x1 x2] (if (> 0.5 (rand)) x1 x2)) i1 i2)
                     (if (> (rand) 0.5) o1 o2)])
        mutate (fn [[i o]]
                 (let [i (if (> (rand) 0.9) (vec (shuffle i)) i)
                       o (if (> (rand) 0.9) (not o) o)
                       new-e (->> swappable (remove (set i)) shuffle first)
                       new-p (rand-int 8)]
                   [(assoc i new-p new-e) o]))
        gen (make-genetic make-sol fitness crossover mutate)]
    (ffirst (gen))))

(lib/check
  [part1 sample] 4
  [part1 sample1] 2024
  [part1 puzzle] 57344080719736
  [part2 puzzle] 0)

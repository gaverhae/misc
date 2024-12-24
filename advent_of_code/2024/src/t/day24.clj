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
                                     (map (fn [[a b]] (if (= a :inner) b (format "z%02d" b))))
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
  [wires]
  (let [cycle? (fn !
                 ([w] (! #{} w))
                 ([seen? w]
                  (if (seen? w)
                    true
                    (let [s (conj seen? w)]
                      (match w
                        [_ _] false
                        [op arg1 arg2] (or (! s (wires arg1))
                                           (! s (wires arg2))))))))]
    (->> wires
         (filter (fn [[[out]]] (= out :z)))
         (reduce (fn [acc el]
                   (or acc (cycle? el)))
                 false))))

(defn machine-runner
  [wires]
  (let [cmpl (fn ! [w]
               (match w
                 [:x x] `(get ~'x-bits ~x 0)
                 [:y y] `(get ~'y-bits ~y 0)
                 [_ _] (! (wires w))
                 [op a1 a2] `(~(case op :or bit-or :xor bit-xor :and bit-and)
                                     ~(! a1)
                                     ~(! a2))))
        fns (->> wires
                 keys
                 (filter (fn [[out]] (= out :z)))
                 sort
                 reverse
                 (mapv (fn [o]
                         (eval `(fn [~'x-bits ~'y-bits]
                                  (prn [~'x-bits ~'y-bits ~(cmpl (wires o))])
                                  ~(cmpl (wires o)))))))]
    (fn [x y]
      (let [x-bits (vec (num-to-bits x))
            y-bits (vec (num-to-bits y))]
        (bits-to-num (->> fns
                          (map (fn [f] (f x-bits y-bits)))))))))

(defn part2
  [{:keys [wires output] :as input}]
  (let [input-size (->> wires keys (filter (comp #{:x :y :z} first)) (map second) (apply max))
        swappable (->> wires keys (remove (fn [[t]] (or (= t :x) (= t :y)))))
        max-input (long (Math/pow 2 (inc input-size)))
        rand-input (fn [] [(long (rand max-input)) (long (rand max-input))])
        wires-to-exprs (fn [wires]
                         (->> output
                              (keep (fn [w]
                                      (let [no-loop (fn ! [w seen?]
                                                      (when (seen? w)
                                                        (throw (ex-info "loop" {})))
                                                      (match w
                                                        [:z z] [z (! (wires w) (conj seen? w))]
                                                        [:x x] [:x x]
                                                        [:y y] [:y y]
                                                        [:inner i] (! (wires w) (conj seen? w))
                                                        [op in1 in2] [op (! in1 (conj seen? w)) (! in2 (conj seen? w))]))]
                                        (try (no-loop w #{})
                                          (catch Throwable _ nil)))))
                              (sort-by first)))
        valid-expr? (fn [[n expr]]
                      (->> (repeatedly 100 rand-input)
                           (every? (fn [[x y]] (= (nth (reverse (num-to-bits (+ x y)))
                                                       n 0)
                                                  (eval-expr expr x y))))))
        num-good-out-bits (fn [swaps]
                            (let [kvs (->> swaps (partition 2) (map vec))
                                  vks (->> swaps (map reverse) (map vec))
                                  swaps (into {} (concat kvs vks))
                                  swapped-wires (->> wires
                                                     (map (fn [[out v]] [(swaps out out) v]))
                                                     (into {}))]
                              (if (has-cycle? swapped-wires)
                                0
                                (->> swapped-wires
                                     wires-to-exprs
                                     (filter valid-expr?)
                                     count))))
        make-sol (fn [] (->> swappable shuffle (take 8) vec))
        fitness (fn [swaps] (- 46 (num-good-out-bits swaps)))
        crossover (fn [i1 i2]
                    (mapv (fn [x1 x2] (if (> 0.5 (rand)) x1 x2)) i1 i2))
        mutate (fn [i]
                 (let [i (if (> (rand) 0.9) (vec (shuffle i)) i)
                       new-e (->> swappable (remove (set i)) shuffle first)
                       new-p (rand-int 8)]
                   (assoc i new-p new-e)))
        gen (make-genetic make-sol fitness crossover mutate)]
    (ffirst (gen))))

(lib/check
  [part1 sample] 4
  [part1 sample1] 2024
  [part1 puzzle] 57344080719736
  [part2 puzzle] 0)

(comment

(bits-to-num [1 1 1 1 1 0 0 0 0 0 1 0 0 0 1 1 0 0 0 0 1 1 1 1 0 1 1 1 1 0 1 0 0 1 0 1 0 1 1 0 0 0 0 1 1])
34103683402435
(bits-to-num [1 0 0 1 1 1 0 0 1 1 0 1 0 1 1 0 1 1 1 1 1 0 0 1 0 1 1 0 1 0 0 0 1 0 1 1 1 1 0 0 1 1 0 1 1])
21555890165659
  (part1 @puzzle)
57344080719736

((machine-runner (:wires @puzzle))
34103683402435
21555890165659)
57344080719736

)

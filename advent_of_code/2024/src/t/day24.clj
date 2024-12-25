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
              [:inner l] (let [[op a b] (! (wires w))]
                           [op l a b])
              [op a b] [op (! a) (! b)]))]
    (let [[op a b] (f (get wires [:z n]))]
      [op (format "z%02d" n) a b])))

(defn expected
  [^long n]
  (case n
    0 [:xor "" [:x 0] [:y 0]]
    1 [:xor ""
       [:and "" [:x 0] [:y 0]]
       [:xor "" [:x 1] [:y 1]]]
    45 [:or ""
        (assoc (expected 44) 0 :and)
        [:and "" [:x 44] [:y 44]]]
    [:xor ""
     [:xor "" [:x n] [:y n]]
     [:or ""
      (assoc (expected (dec n)) 0 :and)
      [:and "" [:x (dec n)] [:y (dec n)]]]]))

(defn equiv
  [f1 f2]
  (or (= f1 f2)
      (let [[op1 _ a1 b1] f1
            [op2 _ a2 b2] f2]
        (and (= (count f1) (count f2) 4)
             (= op1 op2)
             (or (and (equiv a1 a2) (equiv b1 b2))
                 (and (equiv a1 b2) (equiv b1 a2)))))))

(defn find-smaller-diff
  [f1 f2]
  (let [[op1 _ a1 b1] f1
        [op2 _ a2 b2] f2]
    (cond (not= op1 op2) [f1 f2]
          (not= 4 (count f1) (count f2)) [f1 f2]
          (equiv a1 a2) (find-smaller-diff b1 b2)
          (equiv a1 b2) (find-smaller-diff b1 a2)
          (equiv b1 a2) (find-smaller-diff a1 b2)
          (equiv b1 b2) (find-smaller-diff a1 a2)
          :else [:failed f1 f2])))

(defn apply-swaps
  [wires swaps]
  (let [swaps (->> swaps
                   (map (fn [[k v]] [v k]))
                   (into {})
                   (merge swaps))]
  (->> wires
       (map (fn [[g ins]] [(swaps g g) ins]))
       (into {}))))

(defn part2
  [{:keys [wires output] :as input}]
  (let [swaps {[:inner "svm"] [:inner "nbc"]
               [:z 15] [:inner "kqk"]
               [:z 39] [:inner "fnr"]
               [:z 23] [:inner "cgq"]}
        wires (apply-swaps wires swaps)
        failed (->> output
                    (map (fn [[_ n]]
                           [n (to-formula wires n) (expected n)]))
                    (remove (fn [[_ actual expected]] (equiv actual expected)))
                    (map (fn [[n actual expected]]
                           [n actual expected (find-smaller-diff actual expected)]))
                    first)]
    (prn failed)
    (assert (nil? failed))
    (->> swaps
         (mapcat identity)
         (map (fn [[k v]] (if (= :z k) (format "z%02d" v) v)))
         sort
         (interpose ",")
         (apply str)))
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
  [part2 puzzle] "cgq,fnr,kqk,nbc,svm,z15,z23,z39")

(comment

[:xor "cgq" [:or "kph" [:and "jcb" [:xor "smn" [:x 22] [:y 22]] [:or "nnw" [:and "brb" [:x 21] [:y 21]] [:and "rjf" [:xor "mkw" [:y 21] [:x 21]] [:or "cqf" [:and "bvw" [:or "shj" [:and "qrs" [:y 19] [:x 19]] [:and "wnw" [:xor "dtn" [:y 19] [:x 19]] [:or "bbc" [:and "djp" [:x 18] [:y 18]] [:and "fwn" [:xor "tcb" [:y 18] [:x 18]] [:or "dbd" [:and "bdv" [:or "gcc" [:and "bbm" [:xor "rbr" [:x 16] [:y 16]] [:or "kqk" [:and "dkk" [:y 15] [:x 15]] [:and "pbd" [:or "cpv" [:and "wfh" [:x 14] [:y 14]] [:and "wth" [:xor "mdg" [:y 14] [:x 14]] [:or "tdb" [:and "dhr" [:x 13] [:y 13]] [:and "phn" [:xor "ttw" [:y 13] [:x 13]] [:or "ckr" [:and "qnv" [:or "kjr" [:and "fpb" [:x 11] [:y 11]] [:and "rpr" [:xor "kng" [:y 11] [:x 11]] [:or "rjg" [:and "rwv" [:x 10] [:y 10]] [:and "wvf" [:or "vws" [:and "pdh" [:y 9] [:x 9]] [:and "tjk" [:or "jfn" [:and "vjc" [:x 8] [:y 8]] [:and "hdf" [:xor "ktj" [:y 8] [:x 8]] [:or "npf" [:and "rhs" [:y 7] [:x 7]] [:and "hdk" [:or "cqp" [:and "kwc" [:x 6] [:y 6]] [:and "fcv" [:or "pdf" [:and "skn" [:xor "svm" [:y 5] [:x 5]] [:or "rgq" [:and "ppw" [:x 4] [:y 4]] [:and "brg" [:or "nfp" [:and "bhr" [:x 3] [:y 3]] [:and "bbq" [:or "prb" [:and "wdv" [:xor "msc" [:y 2] [:x 2]] [:or "vnc" [:and "kwk" [:and "wbd" [:x 0] [:y 0]] [:xor "dqq" [:x 1] [:y 1]]] [:and "mhb" [:y 1] [:x 1]]]] [:and "mcc" [:y 2] [:x 2]]] [:xor "rvw" [:x 3] [:y 3]]]] [:xor "rcw" [:x 4] [:y 4]]]]] [:and "nbc" [:y 5] [:x 5]]] [:xor "wcw" [:x 6] [:y 6]]]] [:xor "rnc" [:y 7] [:x 7]]]]]] [:xor "fmc" [:y 9] [:x 9]]]] [:xor "tbk" [:x 10] [:y 10]]]]]] [:xor "dnq" [:y 12] [:x 12]]] [:and "kkq" [:y 12] [:x 12]]]]]]] [:xor "fwr" [:x 15] [:y 15]]]]] [:and "qbc" [:y 16] [:x 16]]] [:xor "dhd" [:x 17] [:y 17]]] [:and "grk" [:x 17] [:y 17]]]]]]] [:xor "wdd" [:x 20] [:y 20]]] [:and "gdg" [:y 20] [:x 20]]]]]] [:and "dwt" [:x 22] [:y 22]]] [:xor "hpw" [:y 23] [:x 23]]]

[:and "" [:x 23] [:y 23]]



  )

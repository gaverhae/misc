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

(defn inv-step
  [{:keys [a b c p i out t] :as m}]
  (concat (when-let [from (t i)]
            ;; We could have jumped here instead of coming from the previous
            ;; instruction, if A was not zero. Undoing the jump means excluding
            ;; a possible value of 0 for a and no other change except for i.
            [(-> m
                 (assoc :i from)
                 (update :a (fn [[lower upper]] [(max 1 lower) upper])))])
          ;; other cases: for each opcode we return the set of machines we
          ;; could have come from
          (let [op (get p i)
                arg (get p (inc i))
                ;; we know we did not jump here, so i just goes down
                m (update m :i - 2)]
            (case (int op)
              ;; We know the current value of A is the quotient of the
              ;; division, truncated. Unfortunately that doesn't give us much.
              ;; Let's just ignore this case for now, see if that works.
              0 [m]
              ;; Bitwise-xor is its own inverse: a x b = c <=> a x c = b
              ;; This also means there's only one state we could have come from
              ;; (per current state).
              1 [{update m :b (fn [[l u]]
                                (let [l (bit-xor l arg)
                                      u (bit-xor u arg)]
                                  [(min l u 0) (max l u)]))}]
              ;; this operation "loses" everything above the first

              2 :todo
              3 :todo
              4 :todo
              5 :todo
              6 :todo
              7 :todo))))

(defn part2
  [input]
  ;; The only way a program stops is by i running out of p, so to run in
  ;; reverse we can start with pc pointing to the last opcode.
  ;; We're going to represent a set of machines by putting all of the valid
  ;; values in a, b, and c in the form of a range.
  ;; Our inv-step function must return the set of sets of machines that could
  ;; have let to the current state.
  (let [rev-machine {:p (:p input)
                     ;; We know from part1 that running it forward has worked
                     ;; with no long overflow, so we have a cap on the max
                     ;; value here.
                     :a [0 Long/MAX_VALUE]
                     :b [0 Long/MAX_VALUE]
                     :c [0 Long/MAX_VALUE]
                     :out (:p input)
                     :i (count (:p input))
                     ;; jump points
                     :t (->> (:p input)
                             (partition 2 2)
                             (keep-indexed (fn [idx [op arg]] (when (= op 3) [arg idx])))
                             (into {}))}]
  ;; check if we're back at the start of the program
  (if (and (zero? i) (empty? out))
    (inv-step rev-machine)))

(comment

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

)

(lib/check
  [part1 sample] "4,6,3,5,6,3,5,2,1,0"
  [part1 puzzle] "1,5,0,3,7,3,0,3,1"
  [part2 sample1] 117440
  [part2 puzzle] 105981155568026)

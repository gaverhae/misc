(ns t.day10
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [instaparse.core :as insta]))

(defn parse
  [text]
  (let [p (insta/parser
            "<S> = lights <' '> buttons <' '> joltage
             lights = <'['> (on | off) + <']'>
             on = <'#'>
             off = <'.'>
             buttons = button (<' '> button)*
             button = <'('> nums <')'>
             joltage = <'{'> nums <'}'>
             <nums> = num (<','> num)*
             num = #'\\d+'")]
    (->> text
         string/split-lines
         (map p)
         (map (fn [[[_ & lights] [_ & buttons] [_ & joltage]]]
                {:lights (->> lights
                              (mapv first))
                 :buttons (->> buttons
                               (mapv (fn [[_ & nums]]
                                       (->> nums
                                            (map (fn [[_ n]]
                                                   (parse-long n)))
                                            set))))
                 :joltage (->> joltage
                               (mapv (fn [[_ n]]
                                       (parse-long n))))})))))

(defn dijkstra-search
  [initial final? generate-moves]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (first x) (first y))))]
    (loop [[cost state] [0 initial]
           visited #{}]
      (when (not (visited state))
        (doseq [[nxt-state nxt-cost] (generate-moves [state cost])]
          (when (not (visited nxt-state))
            (.add to-visit [nxt-cost nxt-state]))))
      (if (final? state)
        cost
        (recur (.poll to-visit)
               (conj visited state))))))

(defn part1
  [input]
  (->> input
       (map (fn [{:keys [lights buttons]}]
              (dijkstra-search (->> lights (mapv (constantly :off)))
                               (fn [v] (= v lights))
                               (fn [[lights cost-so-far]]
                                 (let [switch {:off :on, :on :off}]
                                   (->> buttons
                                        (map (fn [button]
                                               [(->> lights
                                                     (map-indexed (fn [i l]
                                                                    (if (contains? button i)
                                                                      (switch l)
                                                                      l))))
                                                (inc cost-so-far)]))))))))
       (reduce + 0)))

(defn swap-rows
  [am idx1 idx2]
  (-> am
      (assoc idx1 (get am idx2))
      (assoc idx2 (get am idx1))))

(defn find-pivot
  [am]
  (loop [idx 0]
    (cond (= idx (count am)) nil
          (-> am (get idx) first zero?) (recur (inc idx))
          :else (swap-rows am 0 idx))))

(defn add-rows
  [v1 v2]
  (mapv (fn [a b] (+ a b)) v1 v2))

(defn multiply-row
  [v s]
  (->> v
       (mapv (fn [e] (* e s)))))

(defn eliminate-pivot
  [am]
  (let [p (first am)]
    (->> (cons p
               (->> (rest am)
                    (map (fn [v]
                           (if (zero? (first v))
                             v
                             (add-rows v (multiply-row p (/ (- (first v))
                                                            (first p)))))))))
         vec)))

(defn merge-sub
  [outer inner]
  (-> (first outer)
      (cons (->> (rest outer)
                 (map-indexed (fn [idx outer-row]
                                (-> (first outer-row)
                                    (cons (get inner idx))
                                    vec)))))
      vec))

(defn partial-gaussian
  [am]
  (cond (= 1 (count am)) am
        (= 0 (count (first am))) am
        (zero? (ffirst am)) (if-let [am (find-pivot am)]
                              (partial-gaussian am)
                              (->> (partial-gaussian (->> am (mapv (comp vec rest))))
                                   (mapv (fn [r] (->> r (cons 0) vec)))))
        :else (let [am (eliminate-pivot am)]
                (merge-sub am (partial-gaussian (->> am
                                                     rest
                                                     (mapv (comp vec rest))))))))

(defn extract-constraint
  [task]
  (if (empty? (:constraints task))
    {:ranges (:ranges task)
     :next-step :count}
    (let [[c & s] (:constraints task)]
      (if (every? zero? c)
        (assoc task :constraints s)
        {:constraints s
         :ranges (:ranges task)
         :cst c
         :indices (->> c
                       butlast
                       (keep-indexed (fn [idx v]
                                       (when (not (zero? v)) idx))))
         :next-step :expand-indices}))))

(defn expand-indices
  [task]
  (if (empty? (:indices task))
    [{:constraints (:constraints task)
      :cst (:cst task)
      :ranges (:ranges task)
      :next-step :apply-constraint}]
    (let [[idx & indices] (:indices task)]
      (if (int? (get-in task [:ranges idx]))
        [(assoc task :indices indices)]
        (let [[start end] (get-in task [:ranges idx])]
          (->> (range start (inc end))
               (map (fn [v]
                      (assoc-in task [:ranges idx] v)))))))))

(defn apply-constraint
  [task]
  (let [cst (:cst task)
        coeffs (butlast cst)
        result (last cst)
        ranges (:ranges task)
        sum (->> coeffs
                 (map-indexed (fn [idx v]
                                (if (zero? v)
                                  0
                                  (* v (get ranges idx)))))
                 (reduce + 0))]
    (when (= sum result)
      [{:constraints (:constraints task)
        :ranges (:ranges task)
        :next-step :constraint}])))

(defn part2
  [input]
  (->> input
       (map (fn [{:keys [buttons joltage]}]
              (let [m (->> buttons
                           (map (fn [button]
                                  (->> (range 0 (count joltage))
                                       (map (fn [n]
                                              (if (contains? button n)
                                                1
                                                0))))))
                           (apply mapv vector))
                    am (mapv (fn [a b] (conj a b)) m joltage)
                    s (partial-gaussian am)
                    ranges (->> buttons
                                (mapv (fn [button]
                                        [0 (->> joltage
                                                (map-indexed vector)
                                                (filter (fn [[idx v]]
                                                          (contains? button idx)))
                                                (map second)
                                                (apply min))])))]
                (loop [todo [{:ranges ranges
                              :constraints (reverse s)
                              :next-step :constraint}]
                       min-so-far Long/MAX_VALUE]
                  (if (empty? todo)
                    min-so-far
                    (let [[task & todo] todo]
                      (case (:next-step task)
                        :constraint (recur (cons (extract-constraint task)
                                                 todo)
                                           min-so-far)
                        :expand-indices (recur (concat (expand-indices task)
                                                       todo)
                                               min-so-far)
                        :apply-constraint (recur (concat (apply-constraint task)
                                                         todo)
                                                 min-so-far)
                        :count (recur todo
                                      (min min-so-far
                                           (->> task
                                                :ranges
                                                (reduce + 0)))))))))))
       (reduce + 0)))

(comment

  (-> (io/resource "day10-sample.txt")
      (slurp)
      (parse))
({:lights [:off :on :on :off], :buttons [[3] [1 3] [2] [2 3] [0 2] [0 1]], :joltage [3 5 4 7]}
 {:lights [:off :off :off :on :off], :buttons [[0 2 3 4] [2 3] [0 4] [0 1 2] [1 2 3 4]], :joltage [7 5 12 7 2]}
 {:lights [:off :on :on :on :off :on], :buttons [[0 1 2 3 4] [0 3 4] [0 1 2 4 5] [1 2]], :joltage [10 11 11 5 10 5]})

  (-> (io/resource "day10-sample.txt")
      (slurp)
      (parse)
      (part1))
7

  (-> (io/resource "day10-input.txt")
      (slurp)
      (parse)
      (part1))
428

  (-> (io/resource "day10-sample.txt")
      (slurp)
      (parse)
      part2)
33

  (-> (io/resource "day10-input.txt")
      (slurp)
      (parse)
      part2)
16613

         )

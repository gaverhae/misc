(ns t.day12
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [clojure.core.async :as async]
            [instaparse.core :as insta]))

(defn rotate
  [shape]
  (->> shape
       (map {[0 0] [0 2]
             [0 1] [1 2]
             [0 2] [2 2]
             [1 0] [0 1]
             [1 1] [1 1]
             [1 2] [2 1]
             [2 0] [0 0]
             [2 1] [1 0]
             [2 2] [2 0]})
       set))

(defn flip
  [shape]
  (->> shape
       (map {[0 0] [0 2]
             [0 1] [0 1]
             [0 2] [0 0]
             [1 0] [1 2]
             [1 1] [1 1]
             [1 2] [1 0]
             [2 0] [2 2]
             [2 1] [2 1]
             [2 2] [2 0]})
       set))

(defn all-orientations
  [bs]
  (->> [bs
        (rotate bs)
        (rotate (rotate bs))
        (rotate (rotate (rotate bs)))
        (flip bs)
        (rotate (flip bs))
        (rotate (rotate (flip bs)))
        (rotate (rotate (rotate (flip bs))))
        (flip (rotate bs))
        (rotate (flip (rotate bs)))
        (rotate (rotate (flip (rotate bs))))
        (rotate (rotate (rotate (flip (rotate bs)))))]
       set))

(defn parse
  [text]
  (let [p (insta/parser "S = shapes trees
                         shapes = shape +
                         shape = num <':\\n'> shape-line+ <'\\n'>
                         shape-line = ('.' | '#') + <'\\n'>
                         trees = tree +
                         tree = num <'x'> num <':'> (<' '> num)+ <'\\n'>
                         num = #'\\d+'")
        [_ [_ & shapes] [_ & trees]] (p text)
        shapes (->> shapes
                    (map (fn [[_ [_ n] & lines]]
                           [(parse-long n)
                            (->> lines
                                 (map-indexed (fn [y [_ & cs]]
                                                (->> cs
                                                     (keep-indexed (fn [x s]
                                                                     (when (= s "#")
                                                                       [y x]))))))
                                 (apply concat)
                                 set)]))
                    (map (fn [[i s]]
                           [i {:orts (all-orientations s)
                               :num-cells (count s)}]))
                    (into {}))]
    (->> trees
         (map (fn [[_ [_ w] [_ h] & gifts]]
                {:w (parse-long w)
                 :h (parse-long h)
                 :gifts (->> gifts
                             (keep-indexed (fn [idx [_ s]]
                                             (let [^long n (parse-long s)]
                                               (when (pos? n)
                                                 (assoc (get shapes idx) :to-place n)))))
                             (sort-by (comp count :orts)))})))))

(defn move
  [[^long dy ^long dx]]
  (fn [s]
    (->> s
         (map (fn [[^long y ^long x]] [(+ y dy) (+ x dx)]))
         set)))

(defn works?
  [{:keys [^long w ^long h gifts]}]
  (let [inside (set (for [y (range h)
                          x (range w)]
                      [y x]))
        positions-to-fill (->> inside
                               (sort-by (fn [[^long y ^long x]] (+ y x))))
        ^long num-free-cells (->> gifts
                                  (map (fn [g] (* (long (:num-cells g))
                                                  (long (:to-place g)))))
                               (reduce - (* h w)))]
    (if (< num-free-cells 0)
      false
      (loop [states [{:to-fill positions-to-fill
                      :budget num-free-cells
                      :free? inside
                      :gifts (->> gifts
                                  (map (fn [{:keys [orts to-place]}]
                                         [orts to-place]))
                                  (into {}))}]]
        (if (empty? states)
          false
          (let [[{:keys [to-fill ^long budget free? gifts]} & states] states]
            (cond (empty? gifts) true
                  (empty? to-fill) false
                  (empty? free?) false
                  :else
                  (let [[p & to-fill] to-fill]
                    (recur (->> states
                                (concat (->> gifts
                                             (mapcat (fn [[orts ^long to-place]]
                                                       (when (> to-place 0)
                                                         (->> orts
                                                              (map (move p))
                                                              (filter (fn [s] (set/subset? s free?)))
                                                              (keep (fn [s]
                                                                      (let [free? (set/difference free? s)
                                                                            budget (cond-> budget (free? p) dec)]
                                                                        (when (>= budget 0)
                                                                          {:to-fill to-fill
                                                                           :budget budget
                                                                           :free? free?
                                                                           :gifts (if (= 1 (get gifts orts))
                                                                                    (dissoc gifts orts)
                                                                                    (update gifts orts dec))})))))))))
                                        (when (> budget 0)
                                          [{:to-fill to-fill
                                            :budget (dec budget)
                                            :free? (disj free? p)
                                            :gifts gifts}]))
                                doall))))))))))

(defn part1
  [input]
  #_(->> input
       (mapv works?))
  (let [report (async/chan)
        work-queue (async/chan)
        _ (async/thread
            (doseq [t (->> input
                           (map-indexed vector))]
              (async/>!! work-queue t)))
        start (System/currentTimeMillis)]
    (dotimes [n 5]
      (async/thread
        (loop []
          (when-let [[idx t] (async/<!! work-queue)]
            (let [r (works? t)
                  elapsed (- (System/currentTimeMillis) start)
                  seconds (-> elapsed (quot 1000) (rem 60))
                  minutes  (-> elapsed (quot 1000) (quot 60) (rem 60))
                  hours (-> elapsed (quot 1000) (quot 60) (quot 60))]
              (async/>!! report [(format "%02d:%02d:%02d" hours minutes seconds)
                                 idx
                                 r]))
            (recur)))))
    (loop [pending (count input)
           succ 0
           fail 0]
      (if (zero? pending)
        succ
        (let [[elapsed idx r] (async/<!! report)
              pending (dec pending)
              succ (cond-> succ r inc)
              fail (cond-> fail (not r) inc)]
          (prn [elapsed :pending pending :succ succ :fail fail])
          (recur pending succ fail))))))

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day12-sample.txt")
      (slurp)
      (parse))
({:w 4
  :h 4
  :gifts ({:orts #{#{[2 2] [0 0] [0 2] [2 0] [2 1] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [1 2]}
                   #{[2 2] [0 0] [1 0] [0 2] [2 0] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [0 1]}}
           :num-cells 7
           :to-place 2})}
 {:w 12
  :h 5
  :gifts ({:orts #{#{[1 0] [1 1] [0 2] [2 0] [2 1] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [1 1] [2 1] [1 2] [0 1]}}
           :num-cells 7
           :to-place 1}
          {:orts #{#{[2 2] [0 0] [1 1] [0 2] [2 0] [2 1] [0 1]}
                   #{[2 2] [0 0] [1 0] [1 1] [0 2] [2 0] [1 2]}}
           :num-cells 7
           :to-place 2}
          {:orts #{#{[2 2] [0 0] [0 2] [2 0] [2 1] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [1 2]}
                   #{[2 2] [0 0] [1 0] [0 2] [2 0] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [0 1]}}
           :num-cells 7
           :to-place 2}
          {:orts #{#{[2 2] [1 1] [0 2] [2 0] [2 1] [1 2] [0 1]}
                   #{[0 0] [1 0] [1 1] [0 2] [2 0] [1 2] [0 1]}
                   #{[2 2] [1 0] [1 1] [0 2] [2 0] [2 1] [1 2]}
                   #{[2 2] [0 0] [1 1] [0 2] [2 1] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [1 1] [2 0] [2 1] [1 2]}
                   #{[2 2] [0 0] [1 0] [1 1] [2 0] [2 1] [0 1]}
                   #{[2 2] [0 0] [1 0] [1 1] [0 2] [1 2] [0 1]}
                   #{[0 0] [1 0] [1 1] [0 2] [2 0] [2 1] [0 1]}}
           :num-cells 7
           :to-place 1})}
 {:w 12
  :h 5
  :gifts ({:orts #{#{[1 0] [1 1] [0 2] [2 0] [2 1] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [1 1] [2 1] [1 2] [0 1]}}
           :num-cells 7
           :to-place 1}
          {:orts #{#{[2 2] [0 0] [1 1] [0 2] [2 0] [2 1] [0 1]}
                   #{[2 2] [0 0] [1 0] [1 1] [0 2] [2 0] [1 2]}}
           :num-cells 7
           :to-place 2}
          {:orts #{#{[2 2] [0 0] [0 2] [2 0] [2 1] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [1 2]}
                   #{[2 2] [0 0] [1 0] [0 2] [2 0] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [0 1]}}
           :num-cells 7
           :to-place 3}
          {:orts #{#{[2 2] [1 1] [0 2] [2 0] [2 1] [1 2] [0 1]}
                   #{[0 0] [1 0] [1 1] [0 2] [2 0] [1 2] [0 1]}
                   #{[2 2] [1 0] [1 1] [0 2] [2 0] [2 1] [1 2]}
                   #{[2 2] [0 0] [1 1] [0 2] [2 1] [1 2] [0 1]}
                   #{[2 2] [0 0] [1 0] [1 1] [2 0] [2 1] [1 2]}
                   #{[2 2] [0 0] [1 0] [1 1] [2 0] [2 1] [0 1]}
                   #{[2 2] [0 0] [1 0] [1 1] [0 2] [1 2] [0 1]}
                   #{[0 0] [1 0] [1 1] [0 2] [2 0] [2 1] [0 1]}}
           :num-cells 7
           :to-place 1})})

(defn p []
  (-> (io/resource "day12-sample.txt")
      (slurp)
      (parse)
      (part1))
  )

(defn p2 []
  (-> (io/resource "day12-input.txt")
      (slurp)
      (parse)
      (part1))
  )

  (-> (io/resource "day12-sample2.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day12-input.txt")
      (slurp)
      (parse)
      part2)

         )

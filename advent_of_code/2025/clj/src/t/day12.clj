(ns t.day12
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [instaparse.core :as insta]))

(defn parse
  [text]
  (let [p (insta/parser "S = shapes trees
                         shapes = shape +
                         shape = num <':\\n'> shape-line+ <'\\n'>
                         shape-line = ('.' | '#') + <'\\n'>
                         trees = tree +
                         tree = num <'x'> num <':'> (<' '> num)+ <'\\n'>
                         num = #'\\d+'")
        [_ [_ & shapes] [_ & trees]] (p text)]
    {:shapes (->> shapes
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
                  (into {}))
     :trees (->> trees
                 (map (fn [[_ [_ w] [_ h] & shapes]]
                        {:w (parse-long w)
                         :h (parse-long h)
                         :shapes (->> shapes
                                      (map (fn [[_ n]] (parse-long n)))
                                      (keep-indexed (fn [idx n]
                                                      (when (pos? n) [idx n])))
                                      (into {}))})))}))

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

(defn move
  [[dy dx]]
  (fn [s]
    (->> s
         (map (fn [[y x]] [(+ y dy) (+ x dx)]))
         set)))

(defn all-places
  [options n]
  (let [c (count options)
        v (vec options)]
    (loop [selected (->> (range c) (map vector))]
      (if (= (count (first selected)) n)
        (->> selected
             (map (fn [s]
                    (->> s
                         (map (fn [idx]
                                (get v idx)))))))
        (recur (->> selected
                    (mapcat (fn [s]
                              (->> (range (inc (peek s)) c)
                                   (map (fn [n] (conj s n))))))))))))

(defn works?
  [shapes]
  (let [all-shapes (->> (assoc shapes -1 #{[0 0]})
                        (map (fn [[idx bs]]
                               [idx (all-orientations bs)]))
                        (into {}))]
    (fn [{:keys [w h shapes]}]
      (let [inside (set (for [y (range h)
                              x (range w)]
                          [y x]))
            free-cells (- (* h w)
                          (->> shapes
                               (map (fn [[idx n]]
                                      (* n (count (first (get all-shapes idx))))))
                               (reduce + 0)))]
        (if (neg? free-cells)
          false
          (loop [states [{:free? inside
                          :to-place (-> shapes
                                        sort)}]]
            (if (empty? states)
              false
              (let [[{:keys [free? to-place]} & states] states]
                (if (empty? to-place)
                  true
                  (let [[[idx n] & to-place] to-place]
                    (recur (concat (->> (all-places free? n)
                                        (mapcat (fn [selected-positions]
                                                  (loop [todo [{:free? free?
                                                                :positions selected-positions}]
                                                         done []]
                                                    (if (empty? todo)
                                                      (->> done
                                                           (map (fn [f] {:free? f, :to-place to-place})))
                                                      (let [[{:keys [free? positions]} & todo] todo]
                                                        (if (empty? positions)
                                                          (recur todo (conj done free?))
                                                          (let [[p & positions] positions]
                                                            (recur (concat
                                                                     (->> (get all-shapes idx)
                                                                          (map (move p))
                                                                          (filter (fn [s] (set/subset? s free?)))
                                                                          (map (fn [s]
                                                                                 {:free? (set/difference free? s)
                                                                                  :positions positions})))
                                                                     todo)
                                                                   done)))))))))
                                   states))))))))))))

(defn part1
  [input]
  (let [w? (works? (:shapes input))
        start (System/currentTimeMillis)]
    (->> (:trees input)
         (keep-indexed (fn [idx t]
                         (let [r (w? t)]
                           (prn [:done (let [elapsed (- (System/currentTimeMillis) start)
                                             seconds (-> elapsed (quot 1000) (rem 60))
                                             minutes  (-> elapsed (quot 1000) (quot 60) (rem 60))
                                             hours (-> elapsed (quot 1000) (quot 60) (quot 60))]
                                         (format "%02d:%02d:%02d" hours minutes seconds))
                                 idx r])
                           (when r 1))))
         count)))

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day12-sample.txt")
      (slurp)
      (parse))
  {:shapes {0 #{[0 0] [1 0] [1 1] [0 2] [2 0] [2 1] [0 1]}
            1 #{[2 2] [0 0] [1 0] [1 1] [0 2] [2 1] [0 1]}
            2 #{[1 0] [1 1] [0 2] [2 0] [2 1] [1 2] [0 1]}
            3 #{[0 0] [1 0] [1 1] [2 0] [2 1] [1 2] [0 1]}
            4 #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [0 1]}
            5 #{[2 2] [0 0] [1 1] [0 2] [2 0] [2 1] [0 1]}}
   :trees ({:w 4, :h 4, :shapes {4 2}}
           {:w 12, :h 5, :shapes {0 1, 2 1, 4 2, 5 2}}
           {:w 12, :h 5, :shapes {0 1, 2 1, 4 3, 5 2}})}

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

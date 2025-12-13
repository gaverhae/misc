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
  (let [all-shapes (->> (assoc shapes :cell #{[0 0]})
                        (map (fn [[idx bs]]
                               [idx (all-orientations bs)]))
                        (into {}))]
    (fn [{:keys [w h shapes]}]
      (let [inside (set (for [y (range h)
                              x (range w)]
                          [y x]))]
        (loop [states [[:pick {:free? inside
                               :to-place shapes}]]]
          (if (empty? states)
            false
            (let [[[k m] & states] states]
              (case k
                :pick (let [{:keys [free? to-place]} m]
                        (if (empty? to-place)
                          true
                          (let [[[idx n] & to-place] to-place]
                            (recur (concat (->> (all-places free? n)
                                                (map (fn [ps]
                                                       [:place {:free? free?
                                                                :to-place to-place
                                                                :idx idx
                                                                :gifts ps}])))
                                           states)))))
                :place (let [{:keys [free? to-place gifts idx]} m]
                         (if (empty? gifts)
                           (recur (cons [:pick {:free? free?
                                                :to-place to-place}]
                                        states))
                           (let [[g & gifts] gifts]
                             (recur (concat (->> (get all-shapes idx)
                                                 (map (move g))
                                                 (filter (fn [s] (set/subset? s free?)))
                                                 (map (fn [s]
                                                        [:place {:free? (set/difference free? s)
                                                                 :to-place to-place
                                                                 :idx idx
                                                                 :gifts gifts}])))
                                            states)))))))))))))

(defn part1
  [input]
  (->> (:trees input)
       (take 2)
       (map (works? (:shapes input)))
       #_count))

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

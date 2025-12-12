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
                                      (keep-indexed (fn [id n] (when (pos? n) [id n])))
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
  [s dy dx]
  (->> s
       (map (fn [[y x]] [(+ y dy) (+ x dx)]))
       set))

(defn works?
  [shapes]
  (let [all-shapes (->> shapes
                        (map (fn [[idx bs]]
                               [idx (all-orientations bs)]))
                        (into {}))]
    (fn [{:keys [w h shapes]}]
      (loop [to-try [{:free? (set (for [y (range h)
                                        x (range w)]
                                    [y x]))
                      :placed {}
                      :to-place (->> shapes
                                     (mapcat (fn [[idx n]]
                                               (repeat n idx))))}]]
        (if (empty? to-try)
          false
          (let [[{:keys [free? to-place placed]} & to-try] to-try]
            (if (empty? to-place)
              true
              (let [[t & to-place] to-place]
                (recur (concat (->> (get all-shapes t)
                                    (mapcat (fn [g]
                                              (->> (if-let [p (placed g)]
                                                     (->> free?
                                                          sort
                                                          (drop-while (fn [f] (pos? (compare p f)))))
                                                     (sort free?))
                                                   (map (fn [[y x]] [[y x] (move g y x)]))
                                                   (filter (fn [[_ g]] (set/subset? g free?)))
                                                   (map (fn [[p g]]
                                                          {:free? (set/difference free? g)
                                                           :placed (assoc placed g p)
                                                           :to-place to-place}))))))
                               to-try))))))))))

(defn part1
  [input]
  (->> (:trees input)
       (take 2)
       #_(drop 2)
       (map (works? (:shapes input)))
       #_count))

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day12-sample.txt")
      (slurp)
      (parse))
{:shapes {0 #{[0 0] [1 0] [1 1] [0 2] [2 0] [2 1] [0 1]},
          1 #{[2 2] [0 0] [1 0] [1 1] [0 2] [2 1] [0 1]},
          2 #{[1 0] [1 1] [0 2] [2 0] [2 1] [1 2] [0 1]},
          3 #{[0 0] [1 0] [1 1] [2 0] [2 1] [1 2] [0 1]},
          4 #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [0 1]},
          5 #{[2 2] [0 0] [1 1] [0 2] [2 0] [2 1] [0 1]}},
 :trees ({:w 4, :h 4, :shapes {4 2}}
         {:w 12, :h 5, :shapes {0 1, 2 1, 4 2, 5 2}}
         {:w 12, :h 5, :shapes {0 1, 2 1, 4 3, 5 2}})}
(defn p []
  (-> (io/resource "day12-sample.txt")
      (slurp)
      (parse)
      (part1))
  )

  (-> (io/resource "day12-input.txt")
      (slurp)
      (parse)
      (part1))

  (-> (io/resource "day12-sample2.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day12-input.txt")
      (slurp)
      (parse)
      part2)

         )

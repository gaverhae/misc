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
                 (map (fn [[_ [_ x] [_ y] & shapes]]
                        {:x (parse-long x)
                         :y (parse-long y)
                         :shapes (->> shapes
                                      (map (fn [[_ n]] (parse-long n))))})))}))

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

(defn works?
  [shapes]
  (let [all-shapes (->> shapes
                        (map (fn [[idx bs]]
                               [idx (->> [bs
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
                                         set)]))
                        (into {}))]
    (fn [tree]
      all-shapes)))

(defn part1
  [input]
  (->> (:trees input)
       (take 1)
       (map (works? (:shapes input)))
       #_(filter (works? (:shapes input)))
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
 :trees ({:x 4, :y 4, :shapes (0 0 0 0 2 0)}
         {:x 12, :y 5, :shapes (1 0 1 0 2 2)}
         {:x 12, :y 5, :shapes (1 0 1 0 3 2)})}

  (-> (io/resource "day12-sample.txt")
      (slurp)
      (parse)
      (part1))
({0 #{#{[2 2] [1 1] [0 2] [2 0] [2 1] [1 2] [0 1]}
      #{[0 0] [1 0] [1 1] [0 2] [2 0] [1 2] [0 1]}
      #{[2 2] [1 0] [1 1] [0 2] [2 0] [2 1] [1 2]}
      #{[2 2] [0 0] [1 1] [0 2] [2 1] [1 2] [0 1]}
      #{[2 2] [0 0] [1 0] [1 1] [2 0] [2 1] [1 2]}
      #{[2 2] [0 0] [1 0] [1 1] [2 0] [2 1] [0 1]}
      #{[2 2] [0 0] [1 0] [1 1] [0 2] [1 2] [0 1]}
      #{[0 0] [1 0] [1 1] [0 2] [2 0] [2 1] [0 1]}},
  1 #{#{[2 2] [0 0] [1 0] [1 1] [0 2] [2 1] [0 1]}
      #{[2 2] [0 0] [1 0] [1 1] [0 2] [2 1] [1 2]}
      #{[0 0] [1 0] [1 1] [0 2] [2 0] [2 1] [1 2]}
      #{[2 2] [1 0] [1 1] [0 2] [2 0] [2 1] [0 1]}
      #{[2 2] [0 0] [1 0] [1 1] [2 0] [1 2] [0 1]}
      #{[2 2] [0 0] [1 1] [2 0] [2 1] [1 2] [0 1]}
      #{[2 2] [1 0] [1 1] [0 2] [2 0] [1 2] [0 1]}
      #{[0 0] [1 1] [0 2] [2 0] [2 1] [1 2] [0 1]}},
  2 #{#{[1 0] [1 1] [0 2] [2 0] [2 1] [1 2] [0 1]}
      #{[2 2] [0 0] [1 0] [1 1] [2 1] [1 2] [0 1]}},
  3 #{#{[0 0] [1 0] [1 1] [0 2] [2 1] [1 2] [0 1]}
      #{[2 2] [1 0] [1 1] [2 0] [2 1] [1 2] [0 1]}
      #{[0 0] [1 0] [1 1] [2 0] [2 1] [1 2] [0 1]}
      #{[2 2] [1 0] [1 1] [0 2] [2 1] [1 2] [0 1]}},
  4 #{#{[2 2] [0 0] [0 2] [2 0] [2 1] [1 2] [0 1]}
      #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [1 2]}
      #{[2 2] [0 0] [1 0] [0 2] [2 0] [1 2] [0 1]}
      #{[2 2] [0 0] [1 0] [0 2] [2 0] [2 1] [0 1]}},
  5 #{#{[2 2] [0 0] [1 1] [0 2] [2 0] [2 1] [0 1]}
      #{[2 2] [0 0] [1 0] [1 1] [0 2] [2 0] [1 2]}}})


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

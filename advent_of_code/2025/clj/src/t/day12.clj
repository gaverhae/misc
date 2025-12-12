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
                  (mapv (fn [[_ [_ n] & lines]]
                          {:idx (parse-long n)
                           :lines (->> lines
                                       (mapv (fn [[_ & cs]]
                                               (->> cs
                                                    (mapv {"#" true, "." false})))))})))

     :trees (->> trees
                 (map (fn [[_ [_ x] [_ y] & shapes]]
                        {:x (parse-long x)
                         :y (parse-long y)
                         :shapes (->> shapes
                                      (map (fn [[_ n]] (parse-long n))))})))}))

(defn part1
  [input]
  )

(defn part2
  [input]
  )

(comment

  (-> (io/resource "day12-sample.txt")
      (slurp)
      (parse))
{:shapes [{:idx 0, :lines [[true true true] [true true false] [true true false]]}
          {:idx 1, :lines [[true true true] [true true false] [false true true]]}
          {:idx 2, :lines [[false true true] [true true true] [true true false]]}
          {:idx 3, :lines [[true true false] [true true true] [true true false]]}
          {:idx 4, :lines [[true true true] [true false false] [true true true]]}
          {:idx 5, :lines [[true true true] [false true false] [true true true]]}]
 :trees ({:x 4, :y 4, :shapes (0 0 0 0 2 0)}
         {:x 12, :y 5, :shapes (1 0 1 0 2 2)}
         {:x 12, :y 5, :shapes (1 0 1 0 3 2)})}

  (-> (io/resource "day12-sample.txt")
      (slurp)
      (parse)
      (part1))

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

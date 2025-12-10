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
                                            (mapv (fn [[_ n]]
                                                    (parse-long n)))))))
                 :joltage (->> joltage
                               (mapv (fn [[_ n]]
                                       (parse-long n))))})))))

(defn part1
  [input]
  )

(defn part2
  [input]
  )

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

  (-> (io/resource "day10-input.txt")
      (slurp)
      (parse)
      (part1))

  (-> (io/resource "day10-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day10-input.txt")
      (slurp)
      (parse)
      part2)

         )

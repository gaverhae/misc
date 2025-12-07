(ns t.day07
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  {:start (->> text
               string/split-lines
               first
               (take-while #{\.})
               count)
   :splitters (->> text
                   string/split-lines
                   rest
                   (map (fn [line]
                          (->> line
                               (keep-indexed (fn [idx c]
                                               (when (= \^ c) idx)))
                               set))))})

(defn part1
  [text]
  )

(defn part2
  [text]
  )

(comment

  (-> (io/resource "day07-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day07-sample.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day07-input.txt")
      (slurp)
      (parse)
      part1)

  (-> (io/resource "day07-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day07-input.txt")
      (slurp)
      (parse)
      part2)

         )

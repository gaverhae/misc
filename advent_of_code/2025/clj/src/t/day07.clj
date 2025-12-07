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
  [{:keys [start splitters]}]
  (loop [to-process splitters
         beams #{start}
         splits 0]
    (if (empty? to-process)
      splits
      (let [[line-splits & to-process] to-process
            splitters-hit (set/intersection beams line-splits)]
        (recur to-process
               (set/union (set/difference beams splitters-hit)
                          (->> splitters-hit
                               (mapcat (fn [n] [(dec n) (inc n)]))
                               set))
               (+ splits (count splitters-hit)))))))

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
21

  (-> (io/resource "day07-input.txt")
      (slurp)
      (parse)
      part1)
1499

  (-> (io/resource "day07-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day07-input.txt")
      (slurp)
      (parse)
      part2)

         )

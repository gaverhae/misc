(ns t.day13
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(def grammar
  "<S> = machine (<'\n\n'> machine)*
  machine = button <'\n'> button <'\n'> prize
  <button> = <'Button '> <#'A|B'> <': X+'> n <', Y+'> n
  <prize> = <'Prize: X='> n <', Y='> n
  <n> = #'\\d+'")

(defn parse
  [lines]
  (->> lines
       (interpose "\n")
       (apply str)
       ((parser grammar))
       (map (fn [[_ & coords]]
              (map ->long coords)))))

(defn solve
  [[ax ay bx by px py]]
  (let [b (long (/ (- py (/ (* px ay) ax))
                   (- by (/ (* bx ay) ax))))
        a (long (/ (- px (* b bx))
                   ax))]
    (when (and (= px (+ (* a ax) (* b bx)))
               (= py (+ (* a ay) (* b by))))
      (+ (* 3 a) b))))

(defn part1
  [input]
  (->> input
       (keep solve)
       (reduce + 0)))

(defn part2
  ([input] (part2 input 10000000000000))
  ([input offset]
   (->> input
        (map (fn [[ax ay bx by px py]]
               [ax ay bx by (+ px offset) (+ py offset)]))
        (keep solve)
        (reduce + 0))))

(lib/check
  [part1 sample] 480
  [part1 puzzle] 34393
  [part2 sample 0] 480
  [part2 puzzle 0] 34393
  [part2 puzzle] 83551068361379)

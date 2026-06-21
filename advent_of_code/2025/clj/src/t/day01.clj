(ns t.day01
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            #_[t.lib :as lib]))

(defn parse
  [text]
  (->> text
       (string/split-lines)
       (map (fn [line]
              (* (case (first line)
                   \L -1
                   \R +1)
                 (parse-long (subs line 1)))))))

(defn part1
  [input]
  (reduce (fn [[num-0 cur-pos] steps]
            (let [new-pos (mod (+ cur-pos steps) 100)]
              [(cond-> num-0
                 (zero? new-pos) inc)
               new-pos]))
          [0 50]
          input))

(defn part2
  [input]
  (loop [pos 50
         num-0 0
         to-process input
         cur-move 0]
    (cond (and (empty? to-process) (zero? cur-move))  num-0
          (zero? cur-move) (recur pos num-0 (rest to-process) (first to-process))
          (pos? cur-move) (let [p (mod (inc pos) 100)]
                            (recur p (cond-> num-0 (zero? p) inc) to-process (dec cur-move)))
          (neg? cur-move) (let [p (mod (dec pos) 100)]
                            (recur p (cond-> num-0 (zero? p) inc) to-process (inc cur-move)))
          :else (throw (ex-info "Should not happen." {:cur-move cur-move})))))

(comment
  (-> (io/resource "day01_sample.txt")
      (slurp)
      (parse))
  (-> (io/resource "day01_sample.txt")
      (slurp)
      (parse)
      part1)
[3 32]
  (-> (io/resource "day01_input.txt")
      (slurp)
      (parse)
      part1)
[1048 8]

  (-> (io/resource "day01_sample.txt")
      (slurp)
      (parse)
      part2)
[6 32]

(-> (io/resource "day01_input.txt")
    (slurp)
    (parse)
    part2)
[2450 8]
[2242 8]
[6504 8]


  )



(ns t.day11
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [instaparse.core :as insta]))


(defn parse
  [text]
  (let [p (insta/parser "<S> = from <':'> to
                         from = name
                         to = (<' '> name)*
                         <name> = #'[a-z]{3}'")]
    (->> text
       string/split-lines
       (map (fn [line]
              (let [[[_ from] [_ & to]] (p line)]
                [from to])))
       (into {}))))

(defn all-paths-from-to
  [input from to]
  (loop [to-process {from 1}
         count-so-far 0]
    (if (empty? to-process)
      count-so-far
      (let [next-step (->> to-process
                           (mapcat (fn [[p c]] (->> (get input p)
                                                    (map (fn [p] [p c])))))
                           (reduce (fn [m [p c]]
                                     (update m p (fnil + 0) c))
                                   {}))]
        (recur (dissoc next-step to)
               (+ count-so-far (get next-step to 0)))))))

(defn part1
  [input]
  (all-paths-from-to input "you" "out"))

(defn part2
  [input]
  (let [ps (partial all-paths-from-to input)]
    (+ (* (ps "svr" "dac")
          (ps "dac" "fft")
          (ps "fft" "out"))
       (* (ps "svr" "fft")
          (ps "fft" "dac")
          (ps "dac" "out")))))

(comment

  (-> (io/resource "day11-sample.txt")
      (slurp)
      (parse))
{"eee" ("out"),
 "ggg" ("out"),
 "ccc" ("ddd" "eee" "fff"),
 "you" ("bbb" "ccc"),
 "hhh" ("ccc" "fff" "iii"),
 "bbb" ("ddd" "eee"),
 "ddd" ("ggg"),
 "aaa" ("you" "hhh"),
 "fff" ("out"),
 "iii" ("out")}

  (-> (io/resource "day11-sample.txt")
      (slurp)
      (parse)
      (part1))
5

  (-> (io/resource "day11-input.txt")
      (slurp)
      (parse)
      (part1))
662

  (-> (io/resource "day11-sample2.txt")
      (slurp)
      (parse)
      part2)
2

  (-> (io/resource "day11-input.txt")
      (slurp)
      (parse)
      part2)
429399933071120

         )

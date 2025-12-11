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

(defn part1
  [input]
  (loop [to-process ["you"]
         count-so-far 0]
    (if (empty? to-process)
      count-so-far
      (let [[t & to-process] to-process]
        (if (= t "out")
          (recur to-process (inc count-so-far))
          (recur (concat (get input t) to-process)
                 count-so-far))))))

(defn part2
  [input]
  (loop [to-process [{:pos "svr"
                      :dac? false
                      :fft? false}]
         count-so-far 0]
    (if (empty? to-process)
      count-so-far
      (let [[t & to-process] to-process]
        (if (= t {:pos "out", :dac? true, :fft? true})
          (recur to-process (inc count-so-far))
          (recur (concat (->> (get input (:pos t))
                              (map (fn [new-pos]
                                     {:pos new-pos
                                      :fft? (or (:fft? t)
                                                (= "fft" new-pos))
                                      :dac? (or (:dac? t)
                                                (= "dac" new-pos))})))
                         to-process)
                 count-so-far))))))

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

         )

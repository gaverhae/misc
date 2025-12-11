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
  )

(defn part2
  [input]
  )

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

  (-> (io/resource "day11-input.txt")
      (slurp)
      (parse)
      (part1))

  (-> (io/resource "day11-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day11-input.txt")
      (slurp)
      (parse)
      part2)

         )

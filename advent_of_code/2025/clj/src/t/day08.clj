(ns t.day08
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn parse
  [text]
  (->> text
       string/split-lines
       (mapv (fn [line]
               (let [[x y z] (string/split line #",")]
                 [(parse-long x)
                  (parse-long y)
                  (parse-long z)])))))

(defn sq
  ^long [^long n]
  (* n n))

(defn distance
  [[^long x1 ^long y1 ^long z1] [^long x2 ^long y2 ^long z2]]
  (+ (sq (- x2 x1))
     (sq (- y2 y1))
     (sq (- z2 z1))))

(defn part1
  [input n]
  (loop [connections (->> (for [^long idx1 (range (count input))
                                ^long idx2 (range (inc idx1) (count input))
                                :let [j1 (get input idx1)
                                      j2 (get input idx2)]]
                            [(distance j1 j2) j1 j2])
                          sort
                          (take n)
                          (map (fn [[_ j1 j2]] [j1 j2])))
         circuits []]
    (if (empty? connections)
      (->> circuits
           (map count)
           (sort-by -)
           (take 3)
           (reduce * 1))
      (let [[[j1 j2] & connections] connections
            [contains-j1] (->> circuits (filter (fn [s] (contains? s j1))))
            [contains-j2] (->> circuits (filter (fn [s] (contains? s j2))))]
        (recur connections
               (cond (and (nil? contains-j1) (nil? contains-j2)) (conj circuits #{j1 j2})
                     (nil? contains-j1) (->> circuits
                                             (map (fn [c] (if (contains? c j2)
                                                            (conj c j1)
                                                            c)))
                                             set)
                     (nil? contains-j2) (->> circuits
                                             (map (fn [c] (if (contains? c j1)
                                                            (conj c j2)
                                                            c)))
                                             set)
                     :else (-> circuits
                               (disj contains-j1)
                               (disj contains-j2)
                               (conj (set/union contains-j1 contains-j2)))))))))




(defn part2
  [input]
  )

(comment

  (-> (io/resource "day08-sample.txt")
      (slurp)
      (parse))

  (-> (io/resource "day08-sample.txt")
      (slurp)
      (parse)
      (part1 10))
40

  (-> (io/resource "day08-input.txt")
      (slurp)
      (parse)
      (part1 1000))
97384

  (-> (io/resource "day08-sample.txt")
      (slurp)
      (parse)
      part2)

  (-> (io/resource "day08-input.txt")
      (slurp)
      (parse)
      part2)

         )

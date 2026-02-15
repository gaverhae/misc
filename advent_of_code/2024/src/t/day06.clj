(ns t.day06
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as string]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  {:map (->> lines
             (mapv (fn [line]
                    (mapv (fn [c] (case c
                                    \. true
                                    \^ true
                                    \# false))
                          line))))
   :guard [(->> (for [y (range (count lines))
                      :let [line (nth lines y)]
                      x (range (count line))
                      :let [c (nth line x)]
                      :when (= c \^)]
                  [y x])
                first)
           [-1 0]]})

(def turn
  {[-1 0] [0 1]
   [0 1] [1 0]
   [1 0] [0 -1]
   [0 -1] [-1 0]})

(defn find-path
  [{m :map, [p d] :guard}]
  (loop [[y0 x0] p
         [dy dx] d
         visited? #{[p d]}]
    (let [[y x] [(+ y0 dy) (+ x0 dx)]
          nxt (get-in m [y x] :out)]
      (cond
        (visited? [[y x] [dy dx]]) [:loop]
        (= false nxt) (recur [y0 x0] (turn [dy dx]) visited?)
        (= true nxt) (recur [y x] [dy dx] (conj visited? [[y x] [dy dx]]))
        (= :out nxt) [:exit (->> visited? (map first) set)]))))

(defn part1
  [input]
  (match (find-path input)
    [:loop] :not-expected
    [:exit cells] (count cells)))

(defn part2
  [{m :map, [p d] :guard :as input}]
  (->> (for [obs-y (range (count m))
             obs-x (range (count (m obs-y)))
             :when (not= [obs-y obs-x] p)]
         [obs-y obs-x])
       (filter (fn [obs]
                 (match (find-path (update input :map assoc-in obs false))
                   [:loop] true
                   [:exit _] false)))
       count))

(lib/check
  [part1 sample] 41
  [part1 puzzle] 5067
  [part2 sample] 6
  [part2 puzzle] 1793)

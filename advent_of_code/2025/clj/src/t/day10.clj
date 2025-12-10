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
                                            (map (fn [[_ n]]
                                                   (parse-long n)))
                                            set))))
                 :joltage (->> joltage
                               (mapv (fn [[_ n]]
                                       (parse-long n))))})))))

(defn dijkstra-search
  [initial final? generate-moves]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (first x) (first y))))]
    (loop [[cost state] [0 initial]
           visited #{}]
      (when (not (visited state))
        (doseq [[nxt-state nxt-cost] (generate-moves [state cost])]
          (when (not (visited nxt-state))
            (.add to-visit [nxt-cost nxt-state]))))
      (if (final? state)
        cost
        (recur (.poll to-visit)
               (conj visited state))))))

(defn part1
  [input]
  (->> input
       (map (fn [{:keys [lights buttons]}]
              (dijkstra-search (->> lights (mapv (constantly :off)))
                               (fn [v] (= v lights))
                               (fn [[lights cost-so-far]]
                                 (let [switch {:off :on, :on :off}]
                                   (->> buttons
                                        (map (fn [button]
                                               [(->> lights
                                                     (map-indexed (fn [i l]
                                                                    (if (contains? button i)
                                                                      (switch l)
                                                                      l))))
                                                (inc cost-so-far)]))))))))
       (reduce + 0)))

(defn part2
  [input]
  (->> input
       (map (fn [{:keys [joltage buttons]}]
              (dijkstra-search (->> joltage (mapv (constantly 0)))
                               (fn [v] (= v joltage))
                               (fn [[joltage cost-so-far]]
                                 (->> buttons
                                      (map (fn [button]
                                             [(->> joltage
                                                   (map-indexed (fn [i j]
                                                                  (if (contains? button i)
                                                                    (inc j)
                                                                    j))))
                                              (inc cost-so-far)])))))))
       (reduce + 0)))

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
7

  (-> (io/resource "day10-input.txt")
      (slurp)
      (parse)
      (part1))
428

  (-> (io/resource "day10-sample.txt")
      (slurp)
      (parse)
      part2)
33

  (-> (io/resource "day10-input.txt")
      (slurp)
      (parse)
      part2)

         )

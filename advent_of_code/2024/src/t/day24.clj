(ns ^:test-refresh/focus t.day24
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (let [[init [_ & gates]] (split-with #(not= "" %) lines)
        init (->> init
                  (map (fn [s] (re-matches #"(...): (\d)" s)))
                  (map (fn [[_ k v]] [k [:lit (parse-long v)]]))
                  (into {}))
        gates (->> gates
                   (map (fn [s] (re-matches #"(...) ([^ ]*) (...) -> (...)" s)))
                   (map (fn [[_ in1 op in2 out]] [out [op in1 in2]]))
                   (into {}))]
    (assert (= #{} (set/intersection (set (keys init))
                                     (set (keys gates)))))
    {:wires (merge init gates)
     :output (->> (merge init gates)
                  keys
                  (filter (fn [k] (= \z (first k))))
                  sort
                  reverse)}))

(defn part1
  [{:keys [output wires]}]
  (->> output
       (map (fn ! [w]
              (match (get wires w)
                [:lit n] n
                ["XOR" in1 in2] (bit-xor (! in1) (! in2))
                ["OR" in1 in2] (bit-or (! in1) (! in2))
                ["AND" in1 in2] (bit-and (! in1) (! in2)))))
       (reduce (fn [acc el] (+ (* 2 acc) el)) 0)))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 4
  [part1 sample1] 2024
  [part1 puzzle] 57344080719736
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

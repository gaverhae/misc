(ns ^:test-refresh/focus t.day23
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  (->> lines
       (map (fn [s] (string/split s #"-")))
       (mapcat (fn [[c1 c2]] [[c1 c2] [c2 c1]]))
       (reduce (fn [acc [k v]]
                 (update acc k (fnil conj #{}) v))
               {})))

(defn part1
  [input]
  (->> (for [c1 (->> input
                       (filter (fn [[k v]] (= \t (first k))))
                       (filter (fn [[k v]] (>= (count v) 2)))
                       (map first))
               c2 (get input c1)
               c3 (get input c1)
               :when (and (neg? (compare c2 c3))
                          (contains? (get input c2) c3))]
           (sort [c1 c2 c3]))
         set
         count))

(defn part2
  [input]
  (loop [cs (keys input)
         connected #{}]
    (if (empty? cs)
      (->> connected (sort-by count) last sort (interpose ",") (apply str))
      (let [[c & cs] cs]
        (recur cs
               (->> (cons #{c}
                          (->> connected
                               (map (fn [s]
                                      (if (= s (set/intersection s (get input c)))
                                        (conj s c)
                                        s)))))
                    set))))))

(lib/check
  [part1 sample] 7
  [part1 puzzle] 1599
  [part2 sample] "co,de,ka,ta"
  [part2 puzzle] "av,ax,dg,di,dw,fa,ge,kh,ki,ot,qw,vz,yw")

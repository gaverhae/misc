(ns ^:test-refresh/focus t.day13
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

(defn part1
  [input]
  (->> input
       (keep (fn [[ax ay bx by px py]]
               (->> (for [a (range 101)
                          b (range 101)
                          :when (and (= px (+ (* a ax) (* b bx)))
                                     (= py (+ (* a ay) (* b by))))]
                      (+ (* 3 a) b))
                    sort
                    first)))
       (reduce + 0)))

(defn part2
  [input offset]
  (->> input
       (keep (fn [[ax ay bx by px py]]
               ;; start with "all a", one coord <= and one coord >
               (loop [a (max (quot px ax) (quot py ay))
                      b 0]
                 (let [cx (+ (* a ax) (* b bx))
                       cy (+ (* a ay) (* b by))]
                   #_(prn [[ax ay bx by px py] a b cx cy])
                   (cond (or (zero? a)
                             (and (> cx px) (> cy py))) nil ;; impossible
                         (and (= px cx) (= py cy)) (+ (* 3 a) b)
                         (and (<= cx px) (<= cy py)) (recur a (inc b))
                         (or (> cx px) (> cy py)) (recur (dec a) b)
                         :else (throw (ex-info "unhandled case" [ax ay bx by px py a b cx cy])))))))
       (reduce + 0)))

(lib/check
  [part1 sample] 480
  [part1 puzzle] 34393
  [part2 sample 0] 480
  [part2 puzzle 0] 34393
  #_#_[part2 puzzle] 0)

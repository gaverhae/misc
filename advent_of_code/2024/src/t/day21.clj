(ns ^:test-refresh/focus t.day21
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(defn parse
  [lines]
  lines)

(defn numeric-part
  [code]
  (->> code
       (filter (fn [c] (<= (int \0) (int c) (int \9))))
       (apply str)
       parse-long))

(defn to-combinations
  [s]
  (case (count (set s))
    0 #{s}
    1 #{s}
    2 (loop [to-add s
             possible-strings #{""}]
        (if (empty? to-add)
          possible-strings
          (let [[nxt & to-add] to-add]
            (recur to-add
                   (->> possible-strings
                        (mapcat (fn [s]
                                  (->> (range (inc (count s)))
                                       (map (fn [i]
                                              (str (subs s 0 i) nxt (subs s i (count s))))))))
                        set)))))))

(def from-to-num
  (->> {[9 9] "" [9 8] "<" [9 7] "<<" [9 6] "v" [9 5] "<v" [9 4] "<<v"
        [9 3] "vv" [9 2] "vv<" [9 1] "<<vv" [9 0] "vvv<" [9 "A"] "vvv"
        [8 8] "" [8 7] "<" [8 6] "v>" [8 5] "v" [8 4] "v<" [8 3] "vv>"
        [8 2] "vv" [8 1] "vv<" [8 0] "vvv" [8 "A"] "vvv>"
        [7 7] "" [7 6] "v>>" [7 5] "v>" [7 4] "v" [7 3] "vv>>" [7 2] "vv>"
        [7 1] "vv" [7 0] "vv>v" [7 "A"] "vvv>>"
        [6 6] "" [6 5] "<" [6 4] "<<" [6 3] "v" [6 2] "v<" [6 1] "v<<"
        [6 0] "vv<" [6 "A"] "vv"
        [5 5] "" [5 4] "<" [5 3] ">v" [5 2] "v" [5 1] "v<" [5 0] "vv"
        [5 "A"] "vv>"
        [4 4] "" [4 3] "v>>" [4 2] "v>" [4 1] "v" [4 0] "vv>" [4 "A"] "vv>>"
        [3 3] "" [3 2] "<" [3 1] "<<" [3 0] "<v" [3 "A"] "v"
        [2 2] "" [2 1] "<" [2 0] "v" [2 "A"] "v>"
        [1 1] "" [1 0] "v>" [1 "A"] "v>>"
        [0 0] "" [0 "A"] ">"
        ["A" "A"] ""}
       (mapcat (fn [[[from to] one-path]]
                 [[[(str from) (str to)] (to-combinations one-path)]
                  [[(str to) (str from)] (to-combinations (->> one-path
                                                               (map {\< \>, \> \<, \v \^, \^ \v})
                                                               (apply str)))]]))
       (into {})))

(defn move-numeric
  [current-position direction]
  (-> {"9" {"<" "8" "v" "6"}
       "8" {"<" "7" "v" "5" ">" "9"}
       "7" {">" "8" "v" "4"}
       "6" {"^" "9" "<" "5" "v" "3"}
       "5" {"<" "4" "^" "8" ">" "6" "v" "2"}
       "4" {"^" "7" ">" "5" "v" "1"}
       "3" {"^" "6" "<" "2" "v" "A"}
       "2" {"^" "5" "<" "1" ">" "3" "v" "0"}
       "1" {"^" "4" ">" "2"}
       "0" {"^" "2" ">" "A"}
       "A" {"^" "3" "<" "0"}}
      (get-in [current-position direction])))

(defn move-directional
  [current-position direction]
  (-> {"^" {">" "A" "v" "v"}
       "A" {"<" "^" "v" ">"}
       "<" {">" "v"}
       "v" {"<" "<" "^" "^" ">" ">"}
       ">" {"^" "A" "<" "v"}}
      (get-in [current-position direction])))

(defn make-from-to
  [t]
  (for [from (keys t)
        to (keys t)
       (map (fn [k]

    f))

(defn numeric-keypad
  [desired-output]
  [])

(defn directional-keypad
  [moves]
  [])

(defn shortest-length
  [c]
  (->> c
       (map numeric-keypad)
       (mapcat directional-keypad)
       (mapcat directional-keypad)
       (map count)
       sort
       first))

(defn part1
  [codes]
  (->> codes first numeric-keypad
       #_(mapcat (fn [c] (numeric-keypad c move-directional))))
  #_(->> codes
       (map (fn [c] (* (numeric-part c)
                       (shortest-length c))))
       (reduce + 0)))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] #_126384 #{"<A^A>^^AvvvA" "<A^A^>^AvvvA" "<A^A^^>AvvvA"}
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

(ns t.day21
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

(defn is-bad-sequence-num?
  "We are not allowed to have the head in front of the empty cell."
  [moves]
  (loop [[y x] [3 2]
         moves moves]
    (cond (= [y x] [3 0]) true
          (empty? moves) false
          :else (let [[m & moves] moves
                      [dy dx] (get {"^" [-1 0], "<" [0 -1], "v" [1 0], ">" [0 1], "A" [0 0]} (str m))
                      [y x] [(+ dy y) (+ dx x)]]
                  (recur [y x] moves)))))

(defn numeric-keypad
  [desired-output]
  (->> (str "A" desired-output)
       (partition 2 1)
       (map (fn [[from to]] (get from-to-num [(str from) (str to)])))
       (interpose ["A"])
       (reduce (fn [acc el]
                 (->> acc
                      (mapcat (fn [p1] (->> el (map (fn [p2] (str p1 p2))))))))
               [""])
       (map (fn [s] (str s "A")))
       (remove is-bad-sequence-num?)
       set))

(def from-to-dir
  {["<" "<"] #{""} ["<" ">"] #{">>"} ["<" "A"] #{">^>" ">>^"} ["<" "^"] #{">^"} ["<" "v"] #{">"}
   [">" "<"] #{"<<"} [">" ">"] #{""} [">" "A"] #{"^"} [">" "^"] #{"<^" "^<"} [">" "v"] #{"<"}
   ["A" "<"] #{"v<<" "<v<"} ["A" ">"] #{"v"} ["A" "A"] #{""} ["A" "^"] #{"<"} ["A" "v"] #{"<v" "v<"}
   ["^" "<"] #{"v<"} ["^" ">"] #{">v" "v>"} ["^" "A"] #{">"} ["^" "^"] #{""} ["^" "v"] #{"v"}
   ["v" "<"] #{"<"} ["v" ">"] #{">"} ["v" "A"] #{">^" "^>"} ["v" "^"] #{"^"} ["v" "v"] #{""}})

(defn directional-keypad
  [out n]
  (let [f (fn [rec out n]
            (if (zero? n)
              (count out)
              (->> (str "A" out)
                   (partition 2 1)
                   (map (fn [[from to]] (get from-to-dir [(str from) (str to)])))
                   (map (fn [set-of-pos]
                          (->> set-of-pos
                               (map (fn [p] (rec rec (str p "A") (dec n))))
                               (apply min))))
                   (reduce + 0))))
        memo-f (memoize f)]
    (memo-f memo-f out n)))

(defn shortest-length
  [c iters]
  (->> (numeric-keypad c)
       (map (fn [o] (directional-keypad o iters)))
       (apply min)))

(defn solve
  [codes iters]
  (->> codes
       (map (fn [c]
              (* (numeric-part c)
                 (shortest-length c iters))))
       (reduce + 0)))

(defn part1
  [input]
  (solve input 2))

(defn part2
  [input]
  (solve input 25))

(lib/check
  [part1 sample] 126384
  [part1 puzzle] 219366
  [part2 puzzle] 271631192020464)

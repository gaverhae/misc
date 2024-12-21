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
              #_out
              (->> (str "A" out)
                   (partition 2 1)
                   (map (fn [[from to]] (get from-to-dir [(str from) (str to)])))
                   (map (fn [set-of-pos]
                          (->> set-of-pos
                               (map (fn [p] (rec rec (str p "A") (dec n))))
                               (apply min)
                               #_(sort-by count)
                               #_first)))
                   (reduce + 0)
                   #_(apply str))))
;              (let [parts (string/split out #"A")]
;                #_(prn [:string out])
;                #_(prn [:parts parts])
;                (->> parts
;                     (map (fn [part]
;                            (->> (str "A" part "A")
;                                 (partition 2 1)
;                                 (map (fn [[from to]] (get from-to-dir [(str from) (str to)])))
;                                 (interpose ["A"])
;                                 (reduce (fn [acc el]
;                                           (->> acc
;                                                (mapcat (fn [p1] (->> el (map (fn [p2] (str p1 p2))))))))
;                                         [""])
;                                 (map (fn [s] (str s "A")))
;                                 set
;                                 (map (fn [possible] (rec rec possible (dec n))))
;                                 #_(apply min)
;                                 (sort-by (juxt count identity))
;                                 first)))
;                     (apply str)
;                     #_(reduce + 0)))))
        memo-f (memoize f)]
    (memo-f memo-f out n)))

(defn shortest-length
  [c iters]
  (->> (numeric-keypad c)
       (map (fn [o] (directional-keypad o iters)))
       #_(sort-by (juxt count identity))
       #_first
       (apply min)))

(string/split "<<^AA>vA" #"A")
["<<^" "" ">v"]

(defn f-d
  [moves]
  (loop [moves moves
         [p out] ["A" ""]]
    (if (empty? moves)
      out
      (let [[m & moves] moves]
        (recur moves
               (match [p m]
                 [_ \A] [p (str out p)]
                 ["A" \<] ["^" out]
                 ["A" \v] [">" out]
                 ["^" \v] ["v" out]
                 ["^" \>] ["A" out]
                 ["<" \>] ["v" out]
                 ["v" \<] ["<" out]
                 ["v" \^] ["^" out]
                 ["v" \>] [">" out]
                 [">" \<] ["v" out]
                 [">" \^] ["A" out]))))))

(f-d "<v<A>A>^A<v<A>>^AAvAA<^A>A<vA>^AA<Av<A>>^AvA^A<vA<AA>>^AvAA<^A>A<vA>^A<A>A<v<A>A>^A<A>vA^A<vA<AA>>^AvA<^A>AvA^A<vA>^A<A>A<vA<AA>>^AvA^A<A>vA^A<v<A>>^A<vA>A^A<A>A")
(f-d "<vA<AA>>^AvAA^<A>Av<<A>>^AvA^A<vA^>Av<<A>^A>AvA^Av<<A>A^>A<Av>A^A")
(f-d "v<<A>>^A<A>AvA<^A>A<vA^>A")
"<A^A>^AvA"
" 0 2  6 3"


(f-d "<vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<v<A>>^AA<vA>A^A<A>A<v<A>A>^AAA<A>vA^A")
(f-d "v<<A>>^A<A>A<AAv>A^A<vAAA^>A")
"<A^A^^>AvvvA"
" 0 2   9   A"

(defn f-n
  [moves]
  (loop [moves moves
         out ""
         p "A"]
    (if (empty? moves)
      out
      (let [[m & moves] moves
            out (if (= \A m) (str out p) out)]
        (recur moves
               out
               (if (= \A m)
                 p
                 (case p
                   "A" (case m \< "0", \^ "3")
                   "0" (case m \^ "2", \> "A")
                   "1" (case m \^ "4", \> "2")
                   "2" (case m \v "0", \> "3", \< "1", \^ "5")
                   "3" (case m \< "2", \v "A", \^ "6")
                   "4" (case m \^ "7", \> "5", \v "1")
                   "5" (case m \< "4", \^ "8", \v "2", \> "6")
                   "6" (case m \^ "9", \< "5", \v "3")
                   "7" (case m \v "4", \> "8")
                   "8" (case m \< "7", \v "5", \> "9")
                   "9" (case m \< "8", \v "6"))))))))


(comment
(clojure.test/deftest rev
  (let [code "0"]
  (clojure.test/is (= code
                      (-> code
                          (shortest-length 1)
                          #_f-d
                          #_f-d
                          #_f-d
                          #_f-d
                          #_f-n
                          )))))
)

(comment

(shortest-length "0" 0)
"<A"
(f-n (shortest-length "0" 0))
"0"

(shortest-length "0" 1)
"v<<A>^>A"
(-> (shortest-length "0" 1) f-d f-n)
"0"

(shortest-length "0" 2)
"<vA<AA>^>AvAA<^A>A"

(-> (shortest-length "0" 2) f-d f-d f-n)
"0"

(shortest-length "0" 3)
"v<<A>A>^Av<<A>^>AAvAA<^A>A<vA>^AAv<<A>^A>AvA^A"
(->> (shortest-length "0" 3) f-d f-d f-d f-n)

(shortest-length "0" 5)
"v<<A>A>^Av<<A>^>AAvAA<^A>A<vA>^A<A>A<vA>^Av<<A>^A>AvA^Av<<A>A>^Av<<A>^>AAvAA<^A>A<vA>^AAv<<A>^A>AvA^AAv<<A>A>^AvA<^A>AA<vA<AA>^>AvA<^A>AvA^A<vA>^A<A>A<vA<AA>^>AvA^AvA<^A>A<vA>^Av<<A>^A>AvA^AAv<<A>A>^Av<<A>^>AAvAA<^A>A<vA>^Av<<A>^A>AvA^A<vA>^A<A>Av<<A>A>^AvA<^A>Av<<A>^>AvA^A"
(->> (shortest-length "0" 5) f-d f-d f-d f-d f-d f-n)
"0"

(->> (shortest-length "029A" 8) f-d f-d f-d f-d f-d f-d f-d f-d f-n)
"029A"


"  v << A>^>A"
"<vA<AAAvAA<^AA"

)

(defn part1
  [codes]
  (->> codes
       #_(drop 1)
       #_(take 1)
       (map (fn [c]
#_(shortest-length c 3)
              (* (numeric-part c)
                       (shortest-length c 2))))
       (reduce + 0)))

(defn part2
  [codes iters]
  (->> codes
       #_(take 1)
       (map (fn [c]
              #_(shortest-length c iters)
              (* (numeric-part c)
                  (shortest-length c iters))))
       (reduce + 0)))

(lib/check
  [part1 sample] 126384
  [part1 puzzle] 219366
  [part2 puzzle 25] 0)

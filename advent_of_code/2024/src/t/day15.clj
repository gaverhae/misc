(ns ^:test-refresh/focus t.day15
  (:require [clojure.core.match :refer [match]]
            [clojure.set :as set]
            [clojure.string :as string]
            [instaparse.core :refer [parser]]
            [t.lib :as lib :refer [->long]]))

(def p
  (parser
    "S = grid <'\n'> moves
    grid = (grid-line <'\n'>)+
    grid-line = (wall | empty | box | robot)+
    moves = (up | down | left | right | <'\n'>)+
    wall = '#'
    empty = '.'
    box = 'O'
    robot = '@'
    left = '<'
    right = '>'
    up = '^'
    down = 'v'"))

(defn parse
  [lines]
  (let [[_ [_ & grid] [_ & moves]] (->> lines
                                        (interpose "\n")
                                        (apply str)
                                        p)]
    {:grid (->> grid
                (map-indexed vector)
                (mapcat (fn [[y [_ & line]]]
                          (->> line
                               (map-indexed (fn [x [c]] [[y x] c])))))
                (into {}))
     :robot (->> grid
                 (map-indexed vector)
                 (mapcat (fn [[y [_ & line]]]
                           (->> line
                                (keep-indexed (fn [x [c]]
                                                (when (= :robot c)
                                                  [y x]))))))
                 first)
     :moves (->> moves (map first) (map {:left [0 -1], :right [0 1], :down [1 0], :up [-1 0]}))}))

(defn move
  [bor [y0 x0 :as pos] grid [dy dx :as dir]]
  (let [new-pos [(+ dy y0)(+ dx x0)]]
    (case (get grid new-pos)
      :empty [new-pos (-> grid (assoc pos :empty) (assoc new-pos bor))]
      :wall [pos grid]
      :box (let [[new-box-pos grid] (move :box new-pos grid dir)]
             (if (= new-box-pos new-pos)
               [pos grid]
               [new-pos (-> grid (assoc pos :empty) (assoc new-pos bor))])))))

(defn part1
  [{:keys [grid robot moves]}]
  (loop [[robot grid] [robot grid]
         moves moves]
    (if (empty? moves)
      (->> grid
           (filter (fn [[pos x]] (= x :box)))
           (map (fn [[[y x] _]] (+ (* 100 y) x)))
           (reduce + 0))
      (let [[m & moves] moves]
        (recur (move :robot robot grid m) moves)))))

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 10092
  [part1 sample1] 2028
  [part1 puzzle] 1526018
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

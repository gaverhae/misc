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

(defn move-box
  [[y0 x0 :as left] grid [dy dx :as dir]]
  (let [right [y0 (inc x0)]
        new-left [(+ y0 dy) (+ x0 dx)]
        new-right [(+ y0 dy) (+ x0 dx 1)]
        move [true (-> grid
                       (assoc left :empty)
                       (assoc right :empty)
                       (assoc new-left :left)
                       (assoc new-right :right))]
        no-move [false grid]]
    (match [dir (get grid new-left) (get grid new-right)]
      [_ :wall _] no-move
      [_ _ :wall] no-move
      [_ :empty :empty] move
      [[0 -1] :empty _] move
      [[0 -1] :right _] (let [[moved? grid] (move-box [y0 (- x0 2)] grid [0 -1])]
                          (if moved? move no-move))
      [[0 1] _ :empty] move
      [[0 1] _ :left] (let [[moved? grid] (move-box [y0 (+ x0 2)] grid [0 1])]
                        (if moved? move no-move))
      [[_ 0] :left :right] (let [[moved? grid] (move-box new-left grid dir)]
                             (if moved? move no-move))
      [[_ 0] :empty :left] (let [[moved? grid] (move-box new-right grid dir)]
                             (if moved? move no-move))
      [[_ 0] :right :empty] (let [[moved? grid] (move-box [(+ y0 dy) (dec x0)] grid dir)]
                              (if moved? move no-move))
      [[_ 0] :right :left] (let [[moved-right? grid] (move-box new-right grid dir)
                                 [moved-left? grid] (move-box [(+ y0 dy) (dec x0)] grid dir)]
                             (if (and moved-left? moved-right?) move no-move)))))

(defn move-robot
  [[y0 x0 :as pos] grid [dy dx :as dir]]
  (let [new-pos [(+ dy y0) (+ dx x0)]
        move [new-pos (-> grid (assoc pos :empty) (assoc new-pos :robot))]
        no-move [pos grid]]
    (case (get grid new-pos)
      :empty [new-pos (-> grid (assoc pos :empty) (assoc new-pos :robot))]
      :wall [pos grid]
      :left (let [[moved? grid] (move-box new-pos grid dir)]
              (if moved?  move no-move))
      :right (let [[moved? grid] (move-box [(+ y0 dy) (+ x0 dx -1)] grid dir)]
               (if moved? move no-move)))))

(defn part2
  [{:keys [grid moves]}]
  (let [grid (->> grid
                  (mapcat (fn [[[y x] c]] (case c
                                            :wall [[[y (* 2 x)] :wall] [[y (inc (* 2 x))] :wall]]
                                            :empty [[[y (* 2 x)] :empty] [[y (inc (* 2 x))] :empty]]
                                            :robot [[[y (* 2 x)] :robot] [[y (inc (* 2 x))] :empty]]
                                            :box [[[y (* 2 x)] :left] [[y (inc (* 2 x))] :right]])))
                  (into {}))
        robot (->> grid
                   (filter (fn [[[y x] c]] (= c :robot)))
                   (ffirst))]
    (loop [[robot grid] [robot grid]
           moves moves]
      (if (empty? moves)
        (->> grid
             (filter (fn [[pos x]] (= x :left)))
             (map (fn [[[y x] _]] (+ (* 100 y) x)))
             (reduce + 0))
        (let [[m & moves] moves]
          (recur (move-robot robot grid m) moves))))))

(lib/check
  [part1 sample] 10092
  [part1 sample1] 2028
  [part1 puzzle] 1526018
  [part2 sample] 9021
  #_#_[part2 puzzle] 0)

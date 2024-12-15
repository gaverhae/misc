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
                               (map-indexed (fn [x [c]] [[y x] (if (= :robot c) :empty c)])))))
                (into {}))
     :robot (->> grid
                 (map-indexed vector)
                 (mapcat (fn [[y [_ & line]]]
                           (->> line
                                (keep-indexed (fn [x [c]]
                                                (when (= :robot c)
                                                  [y x]))))))
                 first)
     :moves (->> moves (map first))}))

(defn part1
  [input]
  input)

(defn part2
  [input]
  input)

(lib/check
  [part1 sample] 10092
  [part1 sample1] 2028
  #_#_[part1 puzzle] 0
  #_#_[part2 sample] 0
  #_#_[part2 puzzle] 0)

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

(defn generate-moves
  [{:keys [inner outer output]} desired-output inner-keypad]
  (let [target (str (get desired-output (count output)))]
  (if (= inner target)
    [{:inner inner
      :outer (str outer "A")
      :output (str output inner)}]
    (->> ["<" ">" "^" "v"]
         (keep (fn [d]
                 (when-let [next-key (inner-keypad inner d)]
                   {:inner next-key
                    :output output
                    :outer (str outer d)})))))))

(defn numeric-keypad
  [desired-output inner-keypad]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (first x) (first y))))]
    (loop [[cost state] [0 {:inner "A", :output "", :outer ""}]
           min-cost nil
           visited #{}
           good-paths #{}]
      (if (and min-cost (> cost min-cost))
        good-paths
        (do (when (not (visited state))
              (doseq [nxt-state (generate-moves state desired-output inner-keypad)]
                (when (not (visited nxt-state))
                  (.add to-visit [(inc cost) nxt-state]))))
            (recur (.poll to-visit)
                   (if (and (nil? min-cost) (= (:output state) desired-output))
                     cost
                     min-cost)
                   (conj visited state)
                   (cond-> good-paths
                     (= (:output state) desired-output) (conj (:outer state)))))))))

(defn directional-keypad
  [moves]
  [""])

(defn shortest-length
  [c]
  (->> c
       (map (fn [c] (numeric-keypad c move-numeric)))
       (mapcat directional-keypad)
       (mapcat directional-keypad)
       (map count)
       sort
       first))

(defn part1
  [codes]
  (->> codes first ((fn [c] (numeric-keypad c move-numeric))))
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

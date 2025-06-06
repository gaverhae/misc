(ns t.lib
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as string]
            [clojure.test :refer [deftest are is testing]]
            [criterium.core :as crit]
            [hato.client :as hc]))

(defn ->long
  [s]
  (Long/parseLong s))

(defn transpose
  [s]
  (apply mapv vector s))

(defn dijkstra-search
  [initial final? generate-moves]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (first x) (first y))))]
    (loop [[cost state] [0 initial]
           visited #{}]
      (when (not (visited state))
        (doseq [[nxt-state nxt-cost] (generate-moves [state cost])]
          (when (not (visited nxt-state))
            (.add to-visit [nxt-cost nxt-state]))))
      (if (final? state)
        cost
        (recur (.poll to-visit)
               (conj visited state))))))

(defn a-star-search
  [initial final? generate-moves heuristic]
  (let [to-visit (java.util.PriorityQueue. 100 (fn [x y] (compare (first x) (first y))))]
    (loop [[guess cost state] [(heuristic initial) 0 initial]
           visited #{}]
      (when (not (visited state))
        (doseq [[nxt-state nxt-cost] (generate-moves [state cost])]
          (when (not (visited nxt-state))
            (.add to-visit [(+ nxt-cost (heuristic nxt-state))
                            nxt-cost nxt-state]))))
      (if (final? state)
        cost
        (recur (.poll to-visit)
               (conj visited state))))))

(defn manhattan
  [[^long x1 ^long y1] [^long x2 ^long y2]]
  (+ (Math/abs (- x1 x2))
     (Math/abs (- y1 y2))))

(defn gcd
  ([a b]
   (cond (neg? a) (recur (- a) b)
         (neg? b) (recur a (- b))
         (= a b) a
         (> a b) (recur (- a b) b)
         (< a b) (recur a (- b a))))
  ([a b & cs]
   (reduce gcd a (cons b cs))))

(defn lcm
  [a b]
  (quot (* a b) (gcd a b)))

(defn clear-screen
  []
  (print (str (char 27) "[2J")) ; clear screen
  (print (str (char 27) "[;H"))); move cursor to the top left corner of the screen

(let [color->code (->> (map vector [:grey :red :green :yellow :blue :magenta :cyan :white]
                                   (range 30 38))
                       (into {}))]
  (defn- color-text
    [col s offset]
    (if-let [code (color->code col)]
      (str "\033[" (+ offset code) "m" s "\033[0m")
      (throw (IllegalArgumentException. (str "Unknown color: '" (pr-str col) "'."))))))

(defn color
  [col s]
  (color-text col s 0))

(defn bg-color
  [col s]
  (color-text col s 10))

(defn get-aoc
  [path]
  (-> (str "https://adventofcode.com/2024" path)
      (hc/get {:headers
               {"cookie" (format "session=%s"
                                 (System/getenv "AOC_SESSION"))}})
      :body))

(defmacro check
  [& specs]
  `(do
     (def ~'day (-> (ns-name ~*ns*)
                    (string/replace "t.day" "")
                    (Long/parseLong)))
     (def ~'sample (delay (-> (format "data/sample/day%02d" ~'day)
                              slurp
                              string/split-lines
                              ~'parse)))
     (def ~'sample1 (delay (-> (format "data/sample/day%02d-1" ~'day)
                               slurp
                               string/split-lines
                               ~'parse)))
     (def ~'sample2 (delay (-> (format "data/sample/day%02d-2" ~'day)
                               slurp
                               string/split-lines
                               ~'parse)))
     (def ~'sample3 (delay (-> (format "data/sample/day%02d-3" ~'day)
                               slurp
                               string/split-lines
                               ~'parse)))
     (def ~'sample4 (delay (-> (format "data/sample/day%02d-4" ~'day)
                               slurp
                               string/split-lines
                               ~'parse)))
     (def ~'puzzle (delay
                     (let [file# (format "data/puzzle/day%02d" ~'day)]
                       (when (not (.exists (io/file file#)))
                         (spit file#
                               (-> (format "https://adventofcode.com/2024/day/%d/input"
                                           ~'day)
                                   (hc/get {:headers
                                            {"cookie" (format "session=%s"
                                                              (System/getenv "AOC_SESSION"))}})
                                   :body)))
                       (-> file#
                           slurp
                           string/split-lines
                           ~'parse))))
     (deftest ~'check
       ~@(->> specs
              (partition 2)
              (map (fn [[[part input & args] expected]]
                     `(is (= ~(concat [part `(force ~input)] args) ~expected))))))))

(deftest tests
  (testing "transpose"
    (are [expected actual] (= expected actual)
         [[1 4] [2 5] [3 6]] (transpose [[1 2 3] [4 5 6]]))))

(defmacro timed
  [& exprs]
  `(let [start# (System/currentTimeMillis)
         _# (do ~@exprs)]
     (- (System/currentTimeMillis) start#)))

(defn now
  []
  (subs (str (java.time.LocalDateTime/now)) 0 19))

(defn now-millis
  []
  (System/currentTimeMillis))

(defn duration-since
  [start]
  (let [d (- (now-millis) start)]
    (format "%4d:%02d:%02d"
            (-> d (quot 1000) (quot 60) (quot 60))
            (-> d (quot 1000) (quot 60) (mod 60))
            (-> d (quot 1000) (mod 60)))))

(defn bench-data
  [f]
  (let [res (crit/benchmark (f) {})
        cur-ver (-> (sh "git" "show" "-q" "--format=%cd.%%s.%h" "--date=format:%Y%m%d%H%M" "--abbrev=8")
                    :out
                    string/trim)
        num-commits (-> (sh "git" "rev-list" "--count" "HEAD")
                        :out
                        string/trim)]
    {:version (format cur-ver num-commits)
     :mean (-> res :mean first)
     :variance (-> res :variance first)
     :lower-q (-> res :lower-q first)
     :upper-q (-> res :upper-q first)}))

(defn bench
  [f]
  (let [res (bench-data f)]
    (format "%s: %5.2f ± %5.2f [%5.2f %5.2f]"
            (res :version)
            (res :mean)
            (res :variance)
            (res :lower-q)
            (res :upper-q))))

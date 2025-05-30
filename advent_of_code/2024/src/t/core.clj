(ns t.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [t.lib :as lib])
  (:import (java.time format.DateTimeFormatter
                      temporal.ChronoUnit
                      Instant
                      ZonedDateTime
                      ZoneId))
  (:gen-class))

;; for the first time, my JVM seems aware of my locale, which I really don't
;; like, so I reset it:
(java.util.Locale/setDefault java.util.Locale/ROOT)

(let [formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH'Z'")]
  (defn current-hour
    []
    (.format (ZonedDateTime/now (ZoneId/of "Z")) formatter)))

(defn get-leaderboard-data
  [board-id]
  (let [data-file (str ".lead/" (current-hour) ".json")]
    (when-not (.exists (io/file data-file))
      (spit data-file
            (lib/get-aoc (format "/leaderboard/private/view/%s.json" board-id))))
    (json/read (io/reader data-file))))

(defn ts->zdt
  [ts]
  (ZonedDateTime/ofInstant (Instant/ofEpochSecond ts) (ZoneId/of "Z")))

(defn format-hour
  [d ts]
  (if ts
    (let [zdt (ZonedDateTime/ofInstant (Instant/ofEpochSecond ts) (ZoneId/of "Z"))]
      (format "%1s%02d:%02d"
              (if (not= [2024 12 d]
                        [(.getYear zdt) (.getMonthValue zdt) (.getDayOfMonth zdt)])
                "*" " ")
              (.getHour zdt)
              (.getMinute zdt)))
    (format "%6s" "")))

(defn format-times
  [data]
  (println (get data "name"))
  (doseq [part ["1" "2"]]
    (println (->> (for [d (range 1 (->> (get data "completion_day_level")
                                        keys
                                        (map parse-long)
                                        sort
                                        last
                                        inc))]
                    (format-hour d (get-in data ["completion_day_level" (str d) part "get_star_ts"])))
                  (interpose " ")
                  (apply str))))
  (println (->> (for [d (range 1 (->> (get data "completion_day_level")
                                      keys
                                      (map parse-long)
                                      sort
                                      last
                                      inc))]
                  (let [t1 (get-in data ["completion_day_level" (str d) "1" "get_star_ts"])
                        t2 (get-in data ["completion_day_level" (str d) "2" "get_star_ts"])]
                    (if (and t1 t2)
                      (let [t1 (ts->zdt t1)
                            t2 (ts->zdt t2)]
                        (format "%6d" (.between ChronoUnit/MINUTES t1 t2)))
                      (format "%6s" ""))))
                (interpose " ")
                (apply str))))

(defn get-env
  [e]
  (let [v (System/getenv e)]
    (when (nil? v)
      (println (format "Please set $%s." e))
      (System/exit 1))
    v))

(defn leaderboard
  []
  (let [board-id (get-env "LEADERBOARD_ID")
        user-ids (-> (get-env "USER_IDS")
                     (string/split #" "))
        data (get-leaderboard-data board-id)]
    (doseq [u user-ids]
      (format-times (get-in data ["members" (str u)])))))

(defn times
  []
  (doseq [d (map inc (range 25))
          :when (not= d 14)
          :when (not= d 24)]
    (let [input (slurp (format "data/puzzle/day%02d" d))
          day-ns (format "t.day%02d" d)
          _ (require [(symbol day-ns)])
          parse (resolve (symbol day-ns "parse"))
          part1 (resolve (symbol day-ns "part1"))
          part2 (resolve (symbol day-ns "part2"))
          part1 (fn []
                  (part1 (-> input string/split-lines parse)))
          part2 (fn []
                  (part2 (-> input string/split-lines parse)))]

      (println (format "Day %2d: %7.3f  %7.3f"
                       d
                       (:mean (lib/bench-data part1))
                       (:mean (lib/bench-data part2)))))))

(defn -main
  [topic & args]
  (case topic
    "lb" (leaderboard)
    "ts" (times)))

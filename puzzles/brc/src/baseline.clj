;; translated as directly as lazily feasible from
;; https://github.com/gunnarmorling/1brc/blob/db064194be375edc02d6dbcd21268ad40f7e2869/src/main/java/dev/morling/onebrc/CalculateAverage_baseline.java

(ns baseline
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn round
  [v]
  (/ (Math/round (* 10.0 v)) 10.0))

(defn -main
  [path]
  (with-open [r (io/reader path)]
    (->> (line-seq r)
         (map (fn [s] (let [[id t] (string/split s #";")]
                        [id (parse-double t)])))
         (reduce (fn [m [id t]]
                   (update m id (fn [old]
                                  (-> old
                                      (update :min (fnil min Double/MAX_VALUE) t)
                                      (update :max (fnil max Double/MIN_VALUE) t)
                                      (update :sum (fnil + 0) t)
                                      (update :count (fnil inc 0))))))

                 (sorted-map))
         (map (fn [[id stats]] [id (str (round (:min stats)) "/" (round (/ (:sum stats) (:count stats))) "/" (:max stats))]))
         println)))

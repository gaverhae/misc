(ns t.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (java.io File)
           (java.nio.file Files
                          LinkOption
                          Path
                          Paths)
           (java.util.stream Stream))
  (:gen-class))

(let [a (make-array LinkOption 0)]
  (defn all-files-under
    "Returns a lazy list of all the files under the given path. Only reports
    regular files; does not traverse symlinks."
    [path]
    (cond (string? path) (all-files-under (Paths/get path (make-array String 0)))
          ;; do not traverse symbolic links at all
          (Files/isSymbolicLink path) []
          ;; silently ignore files we can't read
          (not (Files/isReadable path)) []
          (Files/isDirectory path a) (let [children (with-open [stream (Files/list path)]
                                                      (-> stream Stream/.iterator iterator-seq vec))]
                                       (lazy-seq (mapcat all-files-under children)))
          :else [(-> path Path/.toAbsolutePath str)])))

(defn count-files
  [root]
  (->> (all-files-under root)
       count))

(defn -main
  [& args]
  (let [env_roots (System/getenv "FILE_ROOTS")]
    (if (nil? env_roots)
      (do (println "You need to set FILE_ROOTS. The expected value is a space-separated")
          (println "list of paths to folders to analyze and track."))
      (->> (string/split env_roots #" ")
           (map count-files)
           (reduce + 0)
           prn))))

(comment

  (require '[clojure.set :as set])
  (import '(java.nio.file CopyOption
                          attribute.FileAttribute))

  (defn merge-dirs
    [d1 d2 dest]
    (let [p (fn [d s] (Paths/get (str d s) (make-array String 0)))
          files-under-d1 (->> (all-files-under d1) (map (fn [s] (subs s (count d1)))))
          files-under-d2 (->> (all-files-under d2) (map (fn [s] (subs s (count d2)))))
          same-files (->> (set/intersection (set files-under-d1)
                                            (set files-under-d2))
                          (filter (fn [f]
                                    (= -1 (Files/mismatch (p d1 f)
                                                          (p d2 f))))))]
      (doseq [f same-files]
        (Files/createDirectories (Path/.getParent (p dest f)) (make-array FileAttribute 0))
        (Files/move (p d1 f) (p dest f) (make-array CopyOption 0))
        (Files/delete (p d2 f)))))

  (merge-dirs "/Volumes/to_sort/mbp/Library1" "/Volumes/to_sort/mbp/Library3" "/Volumes/to_sort/mbp/Library")


)

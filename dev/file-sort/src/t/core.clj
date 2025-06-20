(ns t.core
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as string])
  (:import (java.io File)
           (java.nio.file attribute.FileAttribute
                          CopyOption
                          Files
                          LinkOption
                          Path
                          Paths)
           (java.util Locale)
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

(let [no-link-opt (make-array LinkOption 0)
      no-string (make-array String 0)]
  (defn all-dirs-under
    "Returns a lazy list of all the dirs under the given path. Only reports
    regular dirs; does not traverse symlinks."
    [path]
    (cond (string? path) (all-dirs-under (Paths/get path no-string))
          ;; do not traverse symbolic links at all
          (Files/isSymbolicLink path) []
          ;; silently ignore files we can't read
          (not (Files/isReadable path)) []
          (Files/isDirectory path no-link-opt)
          (let [children (with-open [stream (Files/list path)]
                           (-> stream Stream/.iterator iterator-seq vec))]
            (cons (-> path Path/.toAbsolutePath str)
                  (lazy-seq (mapcat all-dirs-under children))))
          :else [])))

(defn file-stats
  [root]
  (let [files (all-files-under root)]
    {:count (count files)
     :total-size (->> files
                      (map (fn [s] (Files/size (Paths/get s (make-array String 0)))))
                      (reduce + 0))}))

(defn now
  []
  (let [now (str (java.time.Instant/now))]
    (-> (subs now 0 19)
        (str "Z")
        (string/replace "T" "-")
        (string/replace ":" "-"))))

(defn count-files
  [env-roots save-result]
  (if (nil? env-roots)
    (do (println "You need to set FILE_ROOTS. The expected value is a space-separated")
        (println "list of paths to folders to analyze and track."))
    (let [cs {:version 1
              :data (->> (string/split env-roots #" ")
                         (map (fn [s] [s (file-stats s)]))
                         (into {}))}
          n (now)]
      (when (= "true" save-result)
        (spit (str "_data/" n ".edn") (pr-str cs)))
      (prn cs)
      (println (format "Total files: %d" (->> cs :data vals (map :count) (reduce + 0))))
      (println (format "Total size (GB): %.2f" (-> cs :data vals
                                                   (->> (map :total-size)
                                                        (reduce + 0))
                                                   long
                                                   (/ 1.0 1000 1000 1000)))))))

(let [no-copy-opt (make-array CopyOption 0)
      no-file-attr (make-array FileAttribute 0)
      no-string (make-array String 0)
      p (fn [d s] (Paths/get (str d s) no-string))
      ds? (fn [s] (and (>= (count s) 9)
                       (= ".DS_Store" (subs s (- (count s) 9) (count s)))))
      under (fn [f d]
              (->> (f d) (map (fn [s] (subs s (count d)))) (remove ds?) set))
      same-files? (fn [p1 p2] (= -1 (Files/mismatch p1 p2)))
      copy (fn [from to] (Files/copy from to no-copy-opt))
      create-path (fn [path] (Files/createDirectories path no-file-attr))]
  (defn merge-dirs
    [d1 d2 dest]
    (let [files-under-d1 (under all-files-under d1)
          files-under-d2 (under all-files-under d2)
          all-dirs (set/union (under all-dirs-under d1) (under all-dirs-under d2))
          all-files (set/union files-under-d1 files-under-d2)
          common-paths (set/intersection files-under-d1 files-under-d2)
          move (fn [from to] (Files/move from to no-copy-opt))
          delete (fn [path] (Files/delete path))]
      (doseq [d all-dirs]
        (create-path (p dest d)))
      (doseq [f (sort all-files)]
        (cond (contains? common-paths f) (do (move (p d1 f) (p dest f))
                                             (if (same-files? (p dest f) (p d2 f))
                                               (delete (p d2 f))
                                               (move (p d2 f) (p dest (str f "__" (random-uuid))))))
              (contains? files-under-d1 f) (move (p d1 f) (p dest f))
              (contains? files-under-d2 f) (move (p d2 f) (p dest f)))))))

(defn -main
  [& args]
  (when-let [lang (System/getenv "LANG")]
    (Locale/setDefault (Locale. lang)))
  (let [env-roots (System/getenv "FILE_ROOTS")
        save-result (System/getenv "SAVE_RESULT")]
    (cond (empty? args)
          (count-files env-roots save-result)

          (and (= "merge" (first args))
               (= 3 (count (rest args))))
          (let [[_ d1 d2 dest] args]
            (merge-dirs d1 d2 dest))

          :else
          (println "Unknown command: " (pr-str args)))))

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
  (defn no-sym-file-seq
    "Returns a lazy list of all the files under the given path. Unlike file-seq,
    will not traverse symbolic links."
    [path]
    (tree-seq
      (fn [^Path p] (and (not (Files/isSymbolicLink p))
                         (Files/isReadable p)
                         (Files/isDirectory p a)))
      (fn [^Path p] (with-open [stream (Files/list p)]
                      (-> stream Stream/.iterator iterator-seq vec)))
      path)))

(defn count-files
  [root]
  (->> (Paths/get root (make-array String 0))
       no-sym-file-seq
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

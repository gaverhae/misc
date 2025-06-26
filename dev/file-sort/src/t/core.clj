(ns t.core
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.set :as set]
            [clojure.string :as string])
  (:import (java.io File)
           (java.nio.file attribute.BasicFileAttributes
                          attribute.FileAttribute
                          CopyOption
                          FileVisitor
                          FileVisitResult
                          Files
                          LinkOption
                          Path
                          Paths)
           (java.security MessageDigest)
           (java.util Locale)
           (java.util.stream Stream))
  (:gen-class))

(def no-follow-symlinks (into-array [LinkOption/NOFOLLOW_LINKS]))
(def no-string (make-array String 0))

(defn ->path
  [^String s]
  (Path/.toAbsolutePath (Paths/get s no-string)))

(defn all-paths-under
  "List all the paths under the given path. Returns a map with these keys:
   :dirs, :files, :symlinks, :error."
  [path]
  (cond ;; do not traverse symbolic links at all
        (Files/isSymbolicLink path) {:symlinks [path]}
        ;; silently ignore files we can't read
        (not (Files/isReadable path)) {:error [path]}
        ;; recur on directories
        (Files/isDirectory path no-follow-symlinks)
        (let [children (with-open [stream (Files/list path)]
                         (-> stream Stream/.iterator iterator-seq vec))]
          (->> (map all-paths-under children)
               (reduce (fn [acc el]
                         (merge-with #(apply conj %1 %2) acc el))
                       {:dirs [path]})))
        :else {:files [path]}))

(defn file-stats
  [root]
  (let [paths (:files (all-paths-under (->path root)))]
    {:count (count paths)
     :total-size (->> paths
                      (map (fn [path] (Files/size path)))
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
              :data (->> env-roots
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

(let [no-copy-opt ^"[Ljava.nio.file.CopyOption;" (make-array CopyOption 0)
      no-file-attr (make-array FileAttribute 0)
      p (fn [d s] (->path (str d s)))
      ds? (fn [s] (and (>= (count s) 9)
                       (= ".DS_Store" (subs s (- (count s) 9) (count s)))))
      under (fn [d]
              (->> (all-paths-under (->path d))
                   (map (fn [[typ ps]]
                          [typ
                           (cond->> ps
                             true (map (fn [p] (subs (str p) (count (str (->path d))))))
                             (= :files typ) (remove ds?)
                             true set)]))
                   (into {})))
      exists? (fn [p] (Files/exists p no-follow-symlinks))
      same-files? (fn [p1 p2]
                    (or (and (Files/isSymbolicLink p1)
                             (Files/isSymbolicLink p2)
                             (= (Files/readSymbolicLink p1)
                                (Files/readSymbolicLink p2)))
                        (and (Files/isRegularFile p1 no-follow-symlinks)
                             (Files/isRegularFile p2 no-follow-symlinks)
                             (= -1 (Files/mismatch p1 p2)))))
      create-path (fn [path] (Files/createDirectories path no-file-attr))
      move (fn [from to] (Files/move from to no-copy-opt))
      delete (fn [path] (Files/delete path))
      remove-dir-tree (fn [d]
                        (Files/walkFileTree
                          (->path d)
                          (reify FileVisitor
                            (visitFile [_ path attrs]
                              (if (and (BasicFileAttributes/.isRegularFile attrs)
                                       (Path/.endsWith path ".DS_Store"))
                                (do (delete path)
                                    FileVisitResult/CONTINUE)
                                (throw (ex-info "Unexpected path." {:path path}))))
                            (preVisitDirectory [_ _ _]
                              FileVisitResult/CONTINUE)
                            (postVisitDirectory [_ path e]
                              (when e
                                (throw (ex-info "Error." {:exception e})))
                              (delete path)
                              FileVisitResult/CONTINUE))))]
  (defn merge-dirs
    [dest & ds]
    (reduce (fn [d1 d2]
              (let [under-d2 (under d2)]
                (doseq [d (:dirs under-d2)]
                  (create-path (p d1 d)))
                (doseq [f (sort (set/union (:files under-d2)
                                           (:symlinks under-d2)))]
                  (if (exists? (p d1 f))
                    (if (same-files? (p d1 f) (p d2 f))
                      (delete (p d2 f))
                      (move (p d2 f) (p d1 (str f "__" (random-uuid)))))
                    (move (p d2 f) (p d1 f))))
                (remove-dir-tree d2)
                d1))
            dest
            ds)))

(defn bytes-to-hex
  [^bytes bs]
  (->> bs
       (map (fn [b]
              (format "%02x" b)))
       (apply str)))

(defn hashes
  [f]
  (let [buffer-size (* 64 1024)
        buffer (byte-array buffer-size)
        md5 (MessageDigest/getInstance "md5")
        sha1 (MessageDigest/getInstance "sha1")]
    (with-open [is (io/input-stream f)]
      (loop []
        (let [n (.read is buffer)]
          (when (pos? n)
            (.update md5 buffer 0 n)
            (.update sha1 buffer 0 n))
          (if (= n buffer-size)
            (recur)
            {:md5 (bytes-to-hex (.digest md5))
             :sha1 (bytes-to-hex (.digest sha1))}))))))

(defn find-dups
  [env-roots]
  (->> env-roots
       (map ->path)
       (map all-paths-under)
       (mapcat :files)
       (map (fn [p]
              {:path p
               :size (Files/size p)}))
       (reduce (fn [acc m]
                 (update acc (:size m) (fnil conj []) m))
               {})
       (filter (fn [[s ms]]
                 (>= (count ms) 2)))
       (sort-by key)
       reverse
       (take 100)
       (mapcat val)
       (map (fn [m] (merge m (hashes (Path/.toFile (:path m))))))
       (group-by (juxt :md5 :sha1 :size))
       (filter (fn [[[md5 sha1 size] ms]]
                 (>= (count ms) 2)))
       (sort-by (fn [[[md5 sha1 size] ms]]
                  size))
       reverse
       (take 10)
       reverse
       (map (fn [[[md5 sha1 size] ms]]
              (println)
              (println (format "%8.2f GB / %s / %s"
                               (/ (long size) (* 1.0 1024 1024 1024))
                               md5 sha1))
              (->> ms (map (comp str :path)) sort (map println) doall)
              (println)))
       doall))

(defn -main
  [& args]
  (when-let [lang (System/getenv "LANG")]
    (Locale/setDefault (Locale. lang)))
  (let [env-roots (-> (System/getenv "FILE_ROOTS")
                      (string/split #" "))
        save-result (System/getenv "SAVE_RESULT")]
    (cond (empty? args)
          (count-files env-roots save-result)

          (and (= "merge" (first args))
               (>= (count (rest args)) 2))
          (apply merge-dirs (rest args))

          (= ["dups"] args)
          (find-dups env-roots)

          :else
          (println "Unknown command: " (pr-str args)))))

(ns t.core
  (:require [clojure.core.async :as async]
            [clojure.core.match :refer [match]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.process :as p]
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

(defn is-dir?
  [path]
  (Files/isDirectory path no-follow-symlinks))

(defn children
  [path]
  (when (is-dir? path)
    (with-open [stream (Files/list path)]
      (-> stream Stream/.iterator iterator-seq vec))))

(defn delete
  "Deletes the given path. If it is a non-empty directory, throws."
  [path]
  (Files/delete path))

(defn delete-rec
  "Deletes a given path and all its content."
  [path]
  (when (is-dir? path)
    (doseq [child (children path)]
      (delete-rec child)))
  (delete path))

(defn make-hard-link
  [from at]
  (Files/createLink at from))

(defn all-paths-under
  "List all the paths under the given path. Returns a map with these keys:
  :dirs, :files, :symlinks, :error."
  [path]
  (cond ;; do not traverse symbolic links at all
        (Files/isSymbolicLink path) {:symlinks [path]}
        ;; silently ignore files we can't read
        (not (Files/isReadable path)) {:error [path]}
        ;; recur on directories
        (is-dir? path)
        (->> (children path)
             (map all-paths-under)
             (reduce (fn [acc el]
                       (merge-with #(apply conj %1 %2) acc el))
                     {:dirs [path]}))
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

(defn show-size
  [^long n]
  (let [suf ["B" "kB" "MB" "GB"]]
    (loop [n (* 1.0 n) e 0]
      (if (and (>= n 1024)
               (> (count suf) (inc e)))
        (recur (/ n 1024.0) (inc e))
        (format "%.2f%s" n (suf e))))))


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
      (println (format "Total files, size: %d, %s"
                       (->> cs :data vals (map :count) (reduce + 0))
                       (-> cs :data vals
                           (->> (map :total-size)
                                (reduce + 0))
                           show-size))))))

(defn bytes-to-hex
  [^bytes bs]
  (->> bs
       (map (fn [b]
              (format "%02x" b)))
       (apply str)))

(defn hashes
  [f max-iter]
  (let [buffer-size (* 64 1024)
        buffer (byte-array buffer-size)
        md5 (MessageDigest/getInstance "md5")
        sha1 (MessageDigest/getInstance "sha1")]
    (with-open [is (io/input-stream f)]
      (loop [i 0]
        (let [n (.read is buffer)]
          (when (pos? n)
            (.update md5 buffer 0 n)
            (.update sha1 buffer 0 n))
          (if (and (or (= -1 max-iter)
                       (< i max-iter))
                   (= n buffer-size))
            (recur (inc i))
            {:md5 (bytes-to-hex (.digest md5))
             :sha1 (bytes-to-hex (.digest sha1))}))))))

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
                             (= :files typ) (remove ds?))]))
                   (into {})))
      exists? (fn [p] (Files/exists p no-follow-symlinks))
      same-files? (fn [p1 p2]
                    (or (= (Files/getAttribute p1 "unix:ino" no-follow-symlinks)
                           (Files/getAttribute p2 "unix:ino" no-follow-symlinks))
                        (and (Files/isSymbolicLink p1)
                             (Files/isSymbolicLink p2)
                             (= (Files/readSymbolicLink p1)
                                (Files/readSymbolicLink p2)))
                        (and (Files/isRegularFile p1 no-follow-symlinks)
                             (Files/isRegularFile p2 no-follow-symlinks)
                             (= -1 (Files/mismatch p1 p2)))))
      create-path (fn [path] (Files/createDirectories path no-file-attr))
      move (fn [from to] (Files/move from to no-copy-opt))
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
                (doseq [f (sort (concat (:files under-d2)
                                        (:symlinks under-d2)))]
                  (if (exists? (p d1 f))
                    (if (same-files? (p d1 f) (p d2 f))
                      (delete (p d2 f))
                      (let [h (hashes (Path/.toFile (p d2 f)) -1)
                            ext (string/replace (Path/.getFileName (p d2 f)) #".*\." "")
                            target (str f "__FS_DUPS__" (:sha1 h) "__" (:md5 h) (when ext ".") ext)]
                        (if (exists? (p d1 target))
                          (delete (p d2 f))
                          (do (println " C  " (subs f 1))
                              (move (p d2 f) (p d1 target))))))
                    (do (println "A   " (subs f 1))
                        (move (p d2 f) (p d1 f)))))
                (remove-dir-tree d2)
                d1))
            dest
            ds)))

(defn find-dups
  [env-roots]
  (let [remove-same-files (fn [files]
                            (->> files
                                 (map (fn [f]
                                        [(Files/getAttribute (:path f) "unix:ino" no-follow-symlinks)
                                         f]))
                                 (into {})
                                 (map val)))
        signature (fn [n]
                    (fn [file]
                      (let [{:keys [md5 sha1]} (hashes (Path/.toFile (:path file))
                                                       n)]
                        (format "%s / %s / %s" (show-size (:size file)) md5 sha1))))
        complete-signature (fn [file]
                             (prn [:h (:path file)])
                             ((signature -1) file))
        re-chunk (fn [f chunks]
                   (->> chunks
                        (mapcat (fn [[chunk-id chunk-elems]]
                                  (group-by f chunk-elems)))
                        (filter (fn [[chunk-id chunk-elems]]
                                  (>= (count chunk-elems) 2)))))
        nil-if-one (fn [files]
                     (when (>= (count files) 2)
                       files))
        make-chunks (fn ! [files]
                      (if (empty? files)
                        nil
                        (let [size (:size (first files))
                              [this-chunk files] (split-with #(= size (:size %)) files)
                              chunks (->> [[nil (->> this-chunk
                                                     (remove-same-files)
                                                     (nil-if-one))]]
                                          (re-chunk (signature 1))
                                          (re-chunk (signature 100))
                                          (re-chunk (signature -1)))]
                          (concat chunks
                                  (lazy-seq (! files))))))]
    (->> env-roots
         (map ->path)
         (map all-paths-under)
         (mapcat :files)
         (map (fn [p]
                {:path p
                 :size (Files/size p)}))
         (sort-by (comp - :size))
         make-chunks)))

(defn show-dups
  [env-roots n]
  (->> (find-dups env-roots)
       (take n)
       reverse
       (map (fn [[sig ms]]
              (println sig)
              (->> ms (map (comp str :path)) sort (map println) doall)
              (println)))
       doall))

(defmacro with-raw-terminal
  [& body]
  `(try
     (p/exec "bash" "-c" "stty raw -F /dev/tty")
     (with-open [r# (io/reader System/in)]
       ;; This is dirty. Is it _too_ dirty? :thinking:
       (let [~'get-char (fn [] (str (char (.read r#))))
             ;; Normally `(println x)` is equivalent to `(print x "\n")` but in
             ;; raw mode we need the carriage return.
             ~'println (fn [& args#] (apply print (concat args# ["\n\r"])))]
         ~@body))
     (finally
       (p/exec {:err :discard} "bash" "-c" "stty cooked -F /dev/tty || true")
       (println))))

(defn rem-dups
  [env-roots]
  (let [ch (async/chan)
        stop (async/chan)
        q1 (async/thread
             (loop [to-handle (find-dups env-roots)]
               (if (empty? to-handle)
                 (async/close! ch)
                 (let [[t & to-handle] to-handle]
                   (async/alt!!
                     [stop] (async/close! ch)
                     [[ch t]] (recur to-handle))))))
        q2 (async/thread
             (with-raw-terminal
               (loop []
                 (when-let [[sig paths] (async/<!! ch)]
                   (println) (println)
                   (println sig)
                   (case (loop [original (first paths)
                                duplicates (rest paths)]
                           (if (empty? duplicates)
                             :done
                             (let [[dup & duplicates] duplicates]
                               (println)
                               (println (format "Original:  \"%s\"." (:path original)))
                               (println (format "Duplicate: \"%s\"." (:path dup)))
                               (println "[s]kip, [d]elete duplicate, [D]elete original, [q]uit, [h]ard link?")
                               (print "> ") (flush)
                               (case (get-char)
                                 "s" (recur original duplicates)
                                 "d" (do (delete (:path dup))
                                         (recur original duplicates))
                                 "D" (do (delete (:path original))
                                         (recur dup duplicates))
                                 "q" :quit
                                 nil :quit
                                 "h" (do (delete (:path dup))
                                         (make-hard-link (:path original) (:path dup))
                                         (recur original duplicates))
                                 (do (println "Please press s, d, D, q, or h.")
                                     (recur original (cons dup duplicates)))))))
                     :done (recur)
                     :quit (do (async/put! stop :done)
                               (println "Bye!")))))))]
    (async/<!! q1)
    (async/<!! q2)))

(defn delete-pattern
  "Deletes all paths that match pattern under given root."
  [root pattern]
  (let [ps (all-paths-under (->path root))
        compile-pattern (fn [pat]
                          (match pat
                            [:empty-dir]
                            (fn [[typ path]]
                              (and (= :dirs typ)
                                   (= () (children path))))
                            [:file file-name]
                            (fn [[typ path]]
                              (and (= :files typ)
                                   (= file-name
                                      (str (Path/.getFileName path)))))
                            [:under partial-path :file file-name]
                            (fn [[typ path]]
                              (and (= :files typ)
                                   (= file-name
                                      (str (Path/.getFileName path)))
                                   (Path/.endsWith path (str partial-path "/" file-name))))
                            [:under partial-path :dir dir-name]
                            (fn [[typ path]]
                              (and (= :dirs typ)
                                   (= dir-name
                                      (str (Path/.getFileName path)))
                                   (Path/.endsWith path (str partial-path "/" dir-name))))
                            [:under partial-path :dir :any :containing files]
                            (fn [[typ path]]
                              (and (= :dirs typ)
                                   (Path/.endsWith path (str partial-path "/"
                                                             (str (Path/.getFileName path))))
                                   (= (set files)
                                      (->> (children path)
                                           (map (fn [child]
                                                  [(cond (Files/isSymbolicLink child) :symlink
                                                         (Files/isRegularFile child no-follow-symlinks) :file
                                                         (is-dir? child) :dir
                                                         :else :unknown)
                                                   (str (Path/.getFileName child))]))
                                           set))))
                            [:under partial-path :dir dir-name :containing files]
                            (fn [[typ path]]
                              (and (= :dirs typ)
                                   (= dir-name
                                      (str (Path/.getFileName path)))
                                   (Path/.endsWith path (str partial-path "/" dir-name))
                                   (= (set files)
                                      (->> (children path)
                                           (map (fn [child]
                                                  [(cond (Files/isSymbolicLink child) :symlink
                                                         (Files/isRegularFile child no-follow-symlinks) :file
                                                         (is-dir? child) :dir
                                                         :else :unknown)
                                                   (str (Path/.getFileName child))]))
                                           set))))))
        match? (compile-pattern pattern)
        to-delete (->> ps
                       (mapcat (fn [[k vs]]
                                 (map (fn [v] [k v]) vs)))
                       (filter match?)
                       (map second))]
    (doseq [f to-delete]
      (println (str f))
      (delete-rec f))))

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

          (and (= ["dups" "show"] (take 2 args))
               (parse-long (nth args 2)))
          (show-dups env-roots (parse-long (nth args 2)))

          (= ["dups"] args)
          (rem-dups env-roots)

          (and (= "delete" (first args))
               (= 3 (count args)))
          (let [[_ root pat] args]
            (delete-pattern root (edn/read-string pat)))

          :else
          (println "Unknown command: " (pr-str args)))))

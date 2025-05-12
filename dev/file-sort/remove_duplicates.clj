#!/usr/bin/env lein-exec

(defn rm [abs-path]
  (clojure.java.shell/sh "rm" "-f" abs-path))

(defn md5 [abs-path]
  (-> (clojure.java.shell/sh "md5sum" abs-path)
      :out
      clojure.string/trim
      (clojure.string/split #" ")
      first))

(defn sha512 [abs-path]
  (-> (clojure.java.shell/sh "sha512sum" abs-path)
      :out
      clojure.string/trim
      (clojure.string/split #" ")
      first))

(defn size-in-bytes [abs-path]
  (-> (clojure.java.shell/sh "du" "-b" abs-path)
      :out
      clojure.string/trim
      (clojure.string/split #"\t")
      first))

(defn token [abs-path]
  {:size (Long/parseLong (size-in-bytes abs-path))
   :md5 (md5 abs-path)
   :sha512 (sha512 abs-path)})

(defn fs [paths]
  (->> (mapcat #(file-seq (clojure.java.io/file %)) paths)
       (filter #(.isFile %))
       (filter #(not= ".DS_Store" (.getName %)))
       (map (fn [x]
              (let [p (.getAbsolutePath x)]
                [p (token p)])))))

(defn collect-duplicates
  [[seen-before to-delete] [abs-path token]]
  (if-let [existing (seen-before token)]
    [seen-before (update-in to-delete [token] (fnil conj []) abs-path)]
    [(assoc seen-before token abs-path) to-delete]))

(defn size
  [n]
  (let [suf ["B" "kB" "MB" "GB" "TB"]]
    (loop [n (* 1.0 n) e 0]
      (if (>= n 1024)
        (recur (/ n 1024.0) (inc e))
        (format "%.2f%s" n (suf e))))))

(defn get-args
  []
  (let [args (rest *command-line-args*)]
    (if (seq args)
      args
      ["."])))

(let [[seen-before to-delete-map] (reduce collect-duplicates [{} {}] (fs (get-args)))
      to-delete (->> to-delete-map
                     (mapcat (fn [[token paths]] (map (fn [p] [token p]) paths)))
                     (sort-by (comp - :size first)))]
  (loop [[seen-before to-delete] [seen-before to-delete]]
    (cond
      (empty? to-delete) (println "Bye!")
      (empty? seen-before) (println "Bye!")
      :else (let [[token abs-path] (first to-delete)
                  existing (get seen-before token)]
              (println (format "Files left: %d." (count to-delete)))
              (println (format "Total size to remove: %s." (->> to-delete
                                                                (map first)
                                                                (map :size)
                                                                (reduce +)
                                                                size)))
              (println (format "Next item: \"%s\" (%s)" abs-path (size (:size token))))
              (println (format "Original:  \"%s\"" existing))
              (println "[S]witch, [s]kip, [d]elete, [q]uit?")
              (print "> ") (flush)
              (case (read-line)
                "S" (recur [(assoc seen-before token abs-path)
                            (cons [token existing] (rest to-delete))])
                "s" (recur [seen-before (rest to-delete)])
                "d" (do (rm abs-path)
                        (recur [seen-before (rest to-delete)]))
                "q" (println "Bye!")
                (do (println "Please enter S, s, d or q.")
                    (recur [seen-before to-delete])))))))

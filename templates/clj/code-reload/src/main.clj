(ns main)

(defn other
  []
  (println "ploup"))

(defn after-reload
  []
  (println "This file has been reloaded."))

(defn start-loop
  []
  (let [keep-going? (atom true)]
    (.start (Thread. (fn []
                       (loop []
                         (when @keep-going?
                           (Thread/sleep 1000)
                           (other)
                           (recur))))))
    (fn [] (reset! keep-going? false))))

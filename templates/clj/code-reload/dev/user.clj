(ns user
  (:require [clojure.tools.namespace.dir :as dir]
            [clojure.tools.namespace.reload :as reload]
            [clojure.tools.namespace.track :as track]
            [nextjournal.beholder :as beholder]
            [main :as main]))

(defonce state (atom {:tracker (track/tracker)}))

(defn refresh-code
  [old-tracker]
  (let [new-tracker (-> (dir/scan old-tracker "src")
                        (assoc ::track/unload [])
                        reload/track-reload)]
    (if-let [e (::reload/error new-tracker)]
      (prn [:refresh/error (-> e Throwable->map :cause)])
      (prn [:refresh/ok]))
    new-tracker))

(defn go
  []
  (swap! state
         (fn [{:keys [watcher stop-loop]}]
           (when watcher (beholder/stop watcher))
           (when stop-loop (stop-loop))
           {:watcher (beholder/watch (fn [_]
                                       (swap! state update :tracker refresh-code)
                                       (main/after-reload))
                                     "src")
            :stop-loop (main/start-loop)})))

(defn stop
  []
  (swap! state
         (fn [{:keys [watcher stop-loop]}]
           (when watcher (beholder/stop watcher))
           (when stop-loop (stop-loop))
           {})))

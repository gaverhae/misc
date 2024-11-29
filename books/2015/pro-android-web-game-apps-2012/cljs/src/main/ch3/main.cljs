(ns ch3.main
  (:require [ch3.model :as m]
            [ch3.render :as r]
            [clojure.core.async :as async]))

(defn init
  []
  (let [event-bus (async/chan)]
    (r/start-render-loop! event-bus "mainCanvas")
    (async/go
      (loop [model (m/init 6 7)
             moves [2 2 3 6 6 5 4 5 4 5 4 5]]
        (async/>! event-bus [:model model])
        (async/<! (async/timeout 1000))
        (when (seq moves)
          (recur (m/move model (first moves))
                 (rest moves)))))))

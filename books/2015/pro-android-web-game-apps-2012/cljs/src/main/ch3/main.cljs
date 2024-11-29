(ns ch3.main
  (:require [ch3.browser-input :as i]
            [ch3.model :as m]
            [ch3.render :as r]
            [clojure.core.async :as async]))

(defn init
  []
  (let [>bus (async/chan)
        <bus (async/pub >bus first)]
    (r/start-render-loop! <bus "mainCanvas")
    (i/start-input-loop! >bus "mainCanvas")
    (async/go (loop [] (prn [:bus (async/<! >bus)])
                 (recur)))
    (async/go
      (loop [model (m/init 6 7)
             moves [2 2 3 6 6 5 4 5 4 5 4 5]]
        (async/>! >bus [:model model])
        (async/>! >bus [:ignored {:data 1}])
        (async/<! (async/timeout 1000))
        (when (seq moves)
          (recur (m/move model (first moves))
                 (rest moves)))))))

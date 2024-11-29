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
    (m/start-model-loop! <bus >bus)
    (async/go
      (loop [moves [2 2 3 6 6 5 4 5 4 5 4 5]]
        (async/<! (async/timeout 1000))
        (when (seq moves)
          (async/>! >bus [:move (first moves)])
          (recur (rest moves)))))))

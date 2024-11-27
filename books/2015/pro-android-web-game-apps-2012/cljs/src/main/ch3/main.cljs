(ns ch3.main
  (:require [ch3.browser-input :as i]
            [ch3.model :as m]
            [ch3.render :as r]
            [clojure.core.async :as async]))

(defn init
  []
  (let [>bus (async/chan)
        <bus (async/pub >bus first)]
    (r/start-render-loop! <bus >bus "mainCanvas")
    (i/start-input-loop! >bus "mainCanvas")
    (m/start-model-loop! <bus >bus)))

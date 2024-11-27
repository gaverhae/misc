(ns ch3.main
  (:require [ch3.render :as r]
            [clojure.core.async :as async]))

(defn init
  []
  (let [canvas (js/document.getElementById "mainCanvas")
        ctx (.getContext canvas "2d")
        dims (r/dimensions (.-width canvas) (.-height canvas) 6 7)
        model {:rows 6
               :cols 7
               :tokens [[] [] [0 1] [1] [] [] [0 1]]
               :player-color ["red" "green"]}]
    (r/render ctx dims model)))

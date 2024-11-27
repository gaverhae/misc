(ns ch3.main
  (:require [ch3.model :as m]
            [ch3.render :as r]
            [clojure.core.async :as async]))

(defn init
  []
  (let [canvas (js/document.getElementById "mainCanvas")
        ctx (.getContext canvas "2d")
        dims (r/dimensions (.-width canvas) (.-height canvas) 6 7)
        model (reduce m/move (m/init 6 7) [2 2 3 6 6 5 4 5 4 5 4 5])]
    (assert (= [:win 1] (:status model)))
    (r/render ctx dims model)))

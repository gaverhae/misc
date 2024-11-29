(ns ch3.browser-input
  (:require [clojure.core.async :as async]))

(defn start-input-loop!
  [>bus canvas-id]
  (js/window.addEventListener
    "resize"
    (fn [_]
      (prn [:resize])
      (async/put! >bus [:resize])))
  (.addEventListener
    (js/document.getElementById canvas-id)
    "click"
    (fn [e]
      (prn [:click])
      (async/put! >bus [:click (.-x e) (.-y e)])
      (.stopPropagation e)
      (.preventDefault e))))

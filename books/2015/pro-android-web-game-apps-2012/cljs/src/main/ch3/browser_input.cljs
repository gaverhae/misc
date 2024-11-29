(ns ch3.browser-input
  (:require [clojure.core.async :as async]))

(defn send-resize-event
  [>bus]
  (async/put! >bus [:resize js/document.body.clientWidth js/document.body.clientHeight]))

(defn start-input-loop!
  [>bus canvas-id]
  (js/window.addEventListener "resize" #(send-resize-event >bus))
  (send-resize-event >bus)
  (.addEventListener
    (js/document.getElementById canvas-id)
    "click"
    (fn [e]
      (prn [:click])
      (async/put! >bus [:click (.-x e) (.-y e)])
      (.stopPropagation e)
      (.preventDefault e))))

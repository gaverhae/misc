(ns ch3.main
  (:require [clojure.core.async :as async]))

(defn translated
  [ctx x y cb]
  (doto ctx
    (.save)
    (.translate x y)
    (cb)
    (.restore)))

(defn dimensions
  [width height rows cols]
  (let [cs (js/Math.floor (min (/ width cols) (/ height rows)))
        bw (* cs cols)
        bh (* cs rows)]
    {:left-offset (js/Math.floor (/ (- width bw) 2))
     :top-offset (js/Math.floor (/ (- height bh) 2))
     :board-width bw
     :board-height bh
     :cell-size cs
     :rows rows
     :cols cols}))

(defn draw-background
  [ctx dims]
  (let [gradient (.createLinearGradient ctx 0 0 0 (:board-height dims))
        w (:board-width dims)
        h (:board-height dims)]
    (doto gradient
      (.addColorStop 0 "#fffbb3")
      (.addColorStop 1 "#f6f6b2"))
    (set! (.-fillStyle ctx) gradient)
    (.fillRect ctx 0 0 w h)

    (set! (.-strokeStyle ctx) "#dad7ac")
    (set! (.-fillStyle ctx) "#f6f6b2")

    (doto ctx
      (.beginPath)
      (.moveTo (* (/ 1 6) w) h)
      (.bezierCurveTo (* (/  9 6) w) (* (/ -1 6) h)
                      (* (/ -3 6) w) (* (/ -1 6) h)
                      (* (/  5 6) w) h)
      (.fill)
      (.beginPath)
      (.moveTo (* (/ 1 6) w) 0)
      (.bezierCurveTo (* (/  9 6) w) (* (/  7 6) h)
                      (* (/ -3 6) w) (* (/  7 6) h)
                      (* (/  5 6) w) 0)
      (.fill))))

(defn draw-grid
  [ctx dims]
  (let [top 0.5
        left 0.5
        bottom (+ (* (:rows dims) (:cell-size dims)) 0.5)
        right (+ (* (:cols dims) (:cell-size dims)) 0.5)]
    (.beginPath ctx)
    (doseq [i (range (inc (:cols dims)))
            :let [x (+ (* i (:cell-size dims)) 0.5)]]
      (doto ctx
        (.moveTo x top)
        (.lineTo x bottom)))
    (doseq [i (range (inc (:rows dims)))
            :let [y (+ (* i (:cell-size dims)) 0.5)]]
      (doto ctx
        (.moveTo left  y)
        (.lineTo right y)))
    (set! (.-lineWidth ctx) 1)
    (set! (.-strokeStyle ctx) "#989681")
    (.stroke ctx)))

(defn draw-token
  [ctx size color]
  (let [radius (* size 0.4)
        gx (* size 0.1)
        gy (* size -0.1)
        gradient (.createRadialGradient ctx gx gy (* 0.1 size) gx gy (* 1.2 radius))]
    (doto gradient
      (.addColorStop 0, "yellow")
      (.addColorStop 1 color))
    (set! (.-fillStyle ctx) gradient)
    (doto ctx
      (.beginPath)
      (.arc 0 0 radius 0 (* 2 js/Math.PI) true)
      (.fill))))

(defn draw-tokens
  [ctx dims tokens get-color]
  (doseq [col (range (count tokens))
          :let [column (get tokens col)
                x (* (:cell-size dims) (+ col 0.5))]
          line (range (count column))
          :let [color (get-color (get column line))
                y (* (:cell-size dims) (- (:rows dims) line 0.5))]]
    (translated ctx x y
      (fn [ctx] (draw-token ctx (:cell-size dims) color)))))

(defn render
  [ctx dims data]
  (translated ctx (:left-offset dims) (:top-offset dims)
    (fn [ctx]
      (draw-background ctx dims)
      (draw-grid ctx dims)
      (draw-tokens ctx dims (:tokens data) (:player-color data)))))

(defn init
  []
  (let [canvas (js/document.getElementById "mainCanvas")
        ctx (.getContext canvas "2d")
        dims (dimensions (.-width canvas) (.-height canvas) 6 7)
        model {:rows 6
               :cols 7
               :tokens [[] [] [0 1] [1] [] [] [0 1]]
               :player-color ["red" "green"]}]
    (render ctx dims model)))

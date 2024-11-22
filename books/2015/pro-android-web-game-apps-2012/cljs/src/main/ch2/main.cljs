(ns ch2.main)

(defn with
  [ctx f & args]
  (.save ctx)
  (apply f ctx args)
  (.restore ctx))

(defn draw-background
  [ctx w h]
  (let [g (.createLinearGradient ctx 0 0 0 h)]
    (.addColorStop g 0 "#fffbb3")
    (.addColorStop g 1 "#f6f6b2")
    (set! (.-fillStyle ctx) g)
    (.fillRect ctx 0 0 w h)))

(defn draw-decoration
  [ctx w h]
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
    (.bezierCurveTo (* (/  9 6) w)  (* (/ 7 6) h)
                    (* (/ -3 6) w)  (* (/ 7 6) h)
                    (* (/  5 6) w)  0)
    (.fill)))

(defn draw-border
  [ctx w h]
  (set! (.-strokeStyle ctx) "#989681")
  (set! (.-lineWidth ctx) 2)
  (.strokeRect ctx 1 1 (- w 2) (- h 2)))

(defn center-in
  [ctx w h]
  (doto ctx
    (.translate (js/Math.floor (/ w 2))
                (js/Math.floor (/ h 2)))))

(defn draw-circle
  [ctx r]
  (let [tau (* 2 js/Math.PI)]
    (doto ctx
      (.beginPath)
      (.arc 0 0 r 0 tau true)
      (.fill ))))

(defn draw-grid-lines
  [ctx cell-size data]
  (let [num-cells-vert (count data)
        num-cells-horiz (count (first data))
        top 0
        right (* num-cells-horiz cell-size)
        bottom (* num-cells-vert cell-size)
        left 0]
    (center-in ctx (- right) (- bottom))
    (.beginPath ctx)
    (doseq [i (range (inc num-cells-horiz))
            :let [h-pos (+ (* i cell-size) 0.5)]]
      (doto ctx
        (.moveTo h-pos top)
        (.lineTo h-pos bottom)))
    (doseq [i (range (inc num-cells-vert))
            :let [v-pos (+ (* i cell-size) 0.5)]]
      (doto ctx
        (.moveTo left  v-pos)
        (.lineTo right v-pos)))
    (set! (.-lineWidth ctx) 1)
    (set! (.-strokeStyle ctx) "#989681")
    (.stroke ctx)))

(defn draw-one-piece
  [ctx cell-size color]
  (let [r (* cell-size 0.4)
        gx (* cell-size 0.1)
        gy (- (* cell-size 0.1))
        gradient (.createRadialGradient ctx
                                        gx gy (* cell-size 0.1)
                                        gx gy (* r 1.2))]
    (doto gradient
      (.addColorStop 0 "yellow")
      (.addColorStop 1 color))
    (set! (.-fillStyle ctx) gradient)
    (set! (.-strokeStyle ctx) "#000")
    (set! (.-lineWidth ctx) 3)
    (draw-circle ctx r)))

(defn draw-pieces
  [ctx cell-size data]
  (let [colors [nil "red" "green"]]
    (doseq [j (range (count data))
            :let [y (* (+ j 0.5) cell-size)
                  line (get data j)]
            i (range (count line))
            :let [x (* (+ i 0.5) cell-size)
                  v (get line i)
                  c (get colors v)]
            :when c]
      (with ctx
        (fn [ctx]
          (doto ctx
            (.translate x y)
            (draw-one-piece cell-size c)))))))

(defn draw-grid
  [ctx cell-size data]
  (doto ctx
    (draw-grid-lines cell-size data)
    (draw-pieces cell-size data)))

(defn init
  []
  (let [canvas (js/document.getElementById "mainCanvas")
        ctx (.getContext canvas "2d")
        w (.-width canvas)
        h (.-height canvas)
        cell-size 40
        data [[0 0 0 0 0 0 0]
              [0 0 0 0 0 0 0]
              [0 0 0 0 0 0 0]
              [0 0 0 0 0 0 0]
              [0 0 0 2 1 0 0]
              [0 0 2 1 1 2 0]]]
    (with ctx draw-background w h)
    (with ctx draw-decoration w h)
    (with ctx draw-border w h)
    (with ctx
      (fn [ctx]
        (doto ctx
          (center-in w h)
          (draw-grid cell-size data))))))

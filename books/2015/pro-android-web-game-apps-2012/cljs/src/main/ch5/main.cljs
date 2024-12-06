(ns ch5.main
  (:require [clojure.core.async :as async]
            [clojure.core.match :refer [match]]))

(def knight-sheet
  {:path "/img/spritesheet.png"
   :sprites (->> [[0, 0, 94, 76, 44, 67],
                  [94, 0, 94, 76, 44, 68],
                  [188, 0, 91, 76, 42, 68],
                  [279, 0, 88, 76, 39, 68],
                  [367, 0, 83, 77, 34, 69],
                  [0, 77, 81, 75, 34, 67],
                  [81, 77, 80, 73, 34, 66],
                  [161, 77, 78, 72, 33, 65],
                  [239, 77, 77, 72, 33, 65],
                  [316, 77, 77, 72, 32, 65],
                  [393, 77, 79, 73, 33, 66],
                  [0, 152, 80, 75, 34, 67],
                  [80, 152, 81, 77, 34, 69],
                  [161, 152, 84, 77, 36, 69],
                  [245, 152, 89, 76, 41, 68],
                  [334, 152, 93, 75, 44, 67]]
                 (map (fn [[tx ty w h ax ay]]
                        {:rectangle [tx ty w h]
                         :anchor [ax ay]})))})

(defn start-image-loader!
  [images >bus]
  (let [<evt (async/chan)]
    (doseq [img images]
      (let [i (js/Image.)]
        (set! (.-onload i) (fn [] (async/put! <evt [:loaded (assoc img :image i)])))
        (set! (.-onerror i) (fn [error] (async/put! <evt [:error (assoc img :error error)])))
        (set! (.-src i) (:path img))))
    (async/go
      (loop []
        (let [msg (async/<! <evt)]
          (match msg
            [:loaded img] (async/>! >bus [:image img])
            [:error img] (js/console.log (clj->js img))))
        (recur)))))

(defn clear
  [ctx]
  (.save ctx)
  (set! (.-fillStyle ctx) "white")
  (.fillRect ctx 0 0 300 300)
  (.restore ctx))

(defn render
  [ctx model img]
  (clear ctx)
  (let [draw-dot (fn [x y]
                   (.save ctx)
                   (set! (.-fillStyle ctx) "red")
                   (.beginPath ctx)
                   (.arc ctx x y 5 0 (* 2 js/Math.PI))
                   (.fill ctx)
                   (.restore ctx))
        draw-frame (fn [image tx ty w h dx dy dir]
                     (.save ctx)
                     (when (= :left dir)
                       (.translate ctx dx dy)
                       (.scale ctx -1 1)
                       (.translate ctx dx (- dy)))
                     (.drawImage ctx image tx ty w h (- dx) (- dy) w h)
                     (draw-dot (- dx) (- dy))
                     (.restore ctx))]
  (doseq [m model]
    (match m
      [:knight x y :standing dir _]
      (let [i (:image img)
            s (first (:sprites img))
            [tx ty w h] (:rectangle s)
            [dx dy] (:anchor s)]
        (.save ctx)
        (.translate ctx x y)
        (draw-frame i tx ty w h dx dy dir)
        (.restore ctx)
        (draw-dot x y))
      [:knight x y :walking dir frame]
      (let [i (:image img)
            sprites (:sprites img)
            s (nth sprites (mod frame (count sprites)))
            [tx ty w h] (:rectangle s)
            [dx dy] (:anchor s)]
        (.save ctx)
        (.translate ctx x y)
        (draw-frame i tx ty w h dx dy dir)
        (.restore ctx)
        (draw-dot x y))))))

(defn start-render-loop!
  [canvas-id <bus]
  (let [<rcv (async/chan)
        model (atom nil)
        image (atom nil)
        raf js/window.requestAnimationFrame
        ctx (.getContext (js/document.getElementById canvas-id) "2d")
        raf-loop (fn ! [t]
                   (when-let [m @model]
                     (when-let [i @image]
                       (render ctx (m t) i)))
                   (raf !))]
    (doseq [signal [:image :anim]]
      (async/sub <bus signal <rcv))
    (raf-loop 0)
    (async/go
      (loop []
        (when-let [msg (async/<! <rcv)]
          (match msg
            [:image i] (do (reset! image i)
                           (recur))
            [:anim a] (do (reset! model a)
                          (recur))))))))

(defn model->anim
  [{:keys [x0 dx t0 orientation]}]
  (fn [t]
    (let [x (+ x0 (* dx (- t t0)))
          stance (if (zero? dx) :standing :walking)]
      [[:knight x 200 stance orientation (quot t 50)]])))

(defn start-model-loop!
  [>bus <bus]
  (let [<rcv (async/chan)
        speed 0.05]
    (async/sub <bus :move-right <rcv)
    (async/go
      (loop [m {:x0 0, :dx 0, :t0 (js/performance.now), :orientation :right}]
        (async/>! >bus [:anim (model->anim m)])
        (match (async/<! <rcv)
          [:move-right ddx] (recur (let [t (js/performance.now)
                                         dt (- t (:t0 m))
                                         traveled (* dt (:dx m))
                                         new-speed (+ (:dx m) (* ddx speed))]
                                     (-> m
                                         (update :x0 + traveled)
                                         (assoc :dx new-speed)
                                         (update :t0 + dt)
                                         (cond-> (not (zero? new-speed))
                                           (assoc :orientation (if (pos? new-speed) :right :left)))))))))))

(defn start-input-loop!
  [>bus]
  (doseq [t ["keydown" "keyup"]]
    (js/document.addEventListener
      t
      (fn [e]
        (let [m (match [(.-key e) t]
                  ["ArrowRight" "keydown"] [:move-right +1]
                  ["ArrowRight" "keyup"] [:move-right -1]
                  ["ArrowLeft" "keydown"] [:move-right -1]
                  ["ArrowLeft" "keyup"] [:move-right +1]
                  [_ _] nil)]
          (when m
            (.preventDefault e)
            (.stopPropagation e)
            (when (not (.-repeat e))
              (async/put! >bus m))))))))

(defn init
  []
  (let [canvas-id "canvas"
        >bus (async/chan)
        <bus (async/pub >bus first)]
    (start-image-loader! [knight-sheet] >bus)
    (start-render-loop! canvas-id <bus)
    (start-model-loop! >bus <bus)
    (start-input-loop! >bus)))

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
  (doseq [m model]
    (match m
      [:knight x y :standing :right _]
      (let [i (:image img)
            s (first (:sprites img))
            [tx ty w h] (:rectangle s)
            [dx dy] (:anchor s)]
        (.drawImage ctx i tx ty w h (- x dx) (- y dy) w h))
      [:knight x y :walking :right frame]
      (let [i (:image img)
            sprites (:sprites img)
            s (nth sprites (mod frame (count sprites)))
            [tx ty w h] (:rectangle s)
            [dx dy] (:anchor s)]
        (.drawImage ctx i tx ty w h (- x dx) (- y dy) w h)))))

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
    (async/sub <bus :input <rcv)
    (async/go
      (loop [m {:x0 0, :dx 0, :t0 (js/performance.now), :orientation :right}]
        (async/>! >bus [:anim (model->anim m)])
        (match (async/<! <rcv)
          [:input [:up _]] (recur (let [t (js/performance.now)
                                        dt (- t (:t0 m))
                                        dx (* dt (:dx m))]
                                    (-> m
                                        (update :x0 + dx)
                                        (assoc :dx 0)
                                        (assoc :t0 t))))
          [:input [:down :left]] (recur (let [t (js/performance.now)]
                                          (-> m
                                              (assoc :dx (- speed))
                                              (assoc :t0 t))))
          [:input [:down :right]] (recur (let [t (js/performance.now)]
                                          (-> m
                                              (assoc :dx speed)
                                              (assoc :t0 t)))))))))

(defn start-input-loop!
  [>bus]
  (doseq [[t k] [["keydown" :down]
                 ["keyup" :up]]]
    (js/document.addEventListener
      t
      (fn [e]
        (when-let [m (case (.-key e)
                       "ArrowRight" [k :right]
                       "ArrowLeft" [k :left]
                       nil)]
          (when (not (.-repeat e))
            (async/put! >bus [:input m])))))))

(defn init
  []
  (let [canvas-id "canvas"
        >bus (async/chan)
        <bus (async/pub >bus first)]
    (start-image-loader! [knight-sheet] >bus)
    (start-render-loop! canvas-id <bus)
    (start-model-loop! >bus <bus)
    (start-input-loop! >bus)))

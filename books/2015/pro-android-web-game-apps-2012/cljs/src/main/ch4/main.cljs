(ns ch4.main
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

(defn render
  [ctx model]
  (.save ctx)
  (set! (.-fillStyle ctx) "white")
  (.fillRect ctx 0 0 300 300)
  (.restore ctx)
  (doseq [m model]
    (match m
      [:sprite x y i] (let [img (:image i)
                            s (first (:sprites i))
                            [tx ty w h] (:rectangle s)
                            [dx dy] (:anchor s)]
                        (.drawImage ctx img tx ty w h x y w h)))))

(defn start-render-loop!
  [<bus]
  (let [<rcv (async/chan)
        model (atom nil)
        raf js/window.requestAnimationFrame
        ctx (.getContext (js/document.getElementById "canvas") "2d")
        raf-loop (fn ! [t]
                   (when-let [m @model]
                     (render ctx (m t)))
                   (raf !))]
    (doseq [signal [:image]]
      (async/sub <bus signal <rcv))
    (raf-loop 0)
    (async/go
      (loop []
        (when-let [msg (async/<! <rcv)]
          (match msg
            [:image img] (do (reset! model (fn [t]
                                             (let [x (mod (/ t 100) 100)
                                                   y (mod (/ (* 3 t) 100) 100)]
                                               [[:sprite x y img]])))
                             (recur))))))))

(defn init
  []
  (let [>bus (async/chan)
        <bus (async/pub >bus first)]
    (start-image-loader! [knight-sheet] >bus)
    (start-render-loop! <bus)))

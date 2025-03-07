(ns ch3.model
  (:require [clojure.core.async :as async]
            [clojure.core.match :refer [match]]))

(defn init
  [rows cols]
  {:rows rows
   :cols cols
   :tokens (vec (repeat cols []))
   :num-tokens 0
   :max-tokens (* cols rows)
   :player-color ["red" "green"]
   :current-player 0
   :status [:ongoing]})

(defn check-line
  [tokens dir col row]
  (let [p (get-in tokens [col row])
        count-in-dir (fn [[dx dy]]
                       (loop [c 0, x (+ col dx), y (+ row dy)]
                         (if (= p (get-in tokens [x y]))
                           (recur (inc c) (+ x dx) (+ y dy))
                           c)))]
    (>= (+ (count-in-dir dir)
           (count-in-dir (map - dir)))
        3)))

(defn update-status
  [state col]
  (let [row (-> state :tokens (get col) count dec)
        directions [[0 1] [1 1] [1 -1] [1 0]]]
    (assoc state :status (cond
                           (some #(check-line (:tokens state) % col row) directions)
                           [:win (:current-player state)]
                           (= (:num-tokens state) (:max-tokens state))
                           [:draw]
                           :else
                           [:ongoing]))))

(defn move
  [state col]
  (-> state
      (update-in [:tokens col] conj (:current-player state))
      (update :num-tokens inc)
      (update-status col)
      (update :current-player #(- 1 %))))

(defn start-model-loop!
  [<bus >bus]
  (let [<rec (async/chan)]
    (async/sub <bus :move <rec)
    (async/go
      (loop [model (init 6 7)]
        (async/>! >bus [:model model])
        (let [msg (async/<! <rec)]
          (match msg
            [:move col] (let [model (move model col)]
                          (match (:status model)
                            [:ongoing] (recur model)
                            [:draw] (do (js/alert "It's a draw!")
                                        (recur (init 6 7)))
                            [:win p] (do (js/alert (str "Player " (inc p) " wins!"))
                                         (recur (init 6 7)))))))))))

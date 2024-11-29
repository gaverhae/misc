(ns ch3.model)

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

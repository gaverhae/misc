(ns t.example
  (:require [clojure.core.match :refer [match]]))

(def loader
  ;; Taken from https://tailwindcss.com/docs/animation
  [:svg {:class ["animate-spin" "h-5" "w-5" "text-zinc-400"]
         :xmlns "https://www.w3.org/2000/svg"
         :fill "none"
         :viewBox "0 0 24 24"}
   [:circle {:class "opacity-50"
             :cx "12" :cy "12" :r "10" :stroke "currentColor" :stroke-width "4"}]
   [:path {:class "opacity-75" :fill "currentColor"
           :d "M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"}]])

(defn button
  [{:keys [disabled? invisible? text]
    :or {disabled? false invisible? false}}]
  [:button {:class (concat ["border-2" "border-lime-400" "rounded" "px-2" "py-1" "bg-lime-100" "m-2"
                            "flex" "justify-center" "items-center" "relative"]
                           (when (not disabled?) ["hover:bg-lime-400" "hover:border-lime-600"])
                           (when (not disabled?) ["transition" "duration-300"])
                           (when disabled? ["cursor-not-allowed"])
                           (when invisible? ["invisible"]))
            :disabled disabled?
            :type "button"}
   [:div {:class [(when disabled? "text-lime-300")]}
    text]
   [:div {:class ["absolute"
                  (when (not disabled?) "invisible")]}
    loader]])

(defn input-text
  [field-value button-text]
  [:form {:class ["size-full" "flex" "justify-center" "items-center" "space-x-2" "px-2"]}
   [:input {:type "text" :name "field"
            :class ["w-full" "rounded" "border-lime-400" "border"]
            :value field-value}]
   [:input {:class ["border-2" "border-lime-400" "rounded" "px-2" "bg-lime-100" "m-2"
                    "flex" "justify-center" "items-center" "cursor-pointer"]
            :type "submit" :value button-text}]])

(defn chat
  [logs draft]
  [:div.size-full.p-1
   [:div.size-full.flex.flex-col.justify-between.bg-amber-100.rounded.border.border-amber-200
    [:div.h-full.overflow-y-auto.p-1.flex.flex-col.space-y-1
     (if (nil? logs)
       [:div.flex.justify-center.items-center.h-full loader]
       (->> logs :me :game :logs
            (sort-by :created)
            (map (fn [{:keys [author message created]}]
                   [:div.w-full.p-1.bg-amber-200.flex.flex-col.rounded
                    [:div.bg-amber-300.rounded.px-1.flex.flex-col
                     [:span.w-full.font-bold (:name author)]
                     [:span.w-full.text-xs " @" created]]
                    [:div.w-full message]]))))]
    [:div.h-20.w-full.flex.flex-col.justify-center.items-center.p-2
     (input-text draft "Send")]]])

(defn show-creature
  [creature hp-panel]
  [:div {:class ["h-8" "w-44" "ring" "ring-amber-200" "rounded" "flex" "items-center" "p-1" "bg-amber-100"]}
   [:div {:class [(when (or (nil? hp-panel)
                            (not= (:creature hp-panel)
                                  (:id creature)))
                    "hidden")
                  "text-sm"
                  "absolute" "inset-0" "m-auto" "z-10" "rounded" "bg-amber-200" "w-40" "h-16" "border-amber-400" "border"
                  "flex" "flex-col" "justify-center" "items-center"]}
    [:div {:class ["top-0" "right-1" "absolute" "cursor-pointer"]}
     "x"]
    [:div (str (:name creature) " (" (:current_hp creature) "/" (:max_hp creature) ")")]
    [:div (input-text (:draft hp-panel) "Add")]]
   [:div {:class ["p-1" "w-1/4"]}
    (:name creature)]
   [:div {:class ["p-1" "pl-0" "w-3/4" "relative" "h-4"]}
    [:div {:class ["absolute" "left-0" "top-0" "bg-red-500" "h-4" "w-full"]}]
    [:div {:class ["absolute" "left-0" "top-0" "bg-green-200" "h-4"]
           :style {:width (->> (/ (:current_hp creature) (:max_hp creature)) (* 100.0) (format "%5.2f%%"))}}]]])

(defn grid
  [creatures targeting]
  (let [c-at (->> (cons (assoc (first creatures) :active? true)
                        (rest creatures))
                  (map (fn [c] [[(:y c) (:x c)] c]))
                  (into {}))]
    [:div.flex.w-full.flex-col
     (for [y (range 10)]
       [:div.flex.last:border-b
        (for [x (range 10)]
          [:div {:class ["w-10" "h-10" "border-t" "border-r" "first:border-l" "flex" "items-center" "justify-center"]}
           [:div {:class (when (:active? (c-at [y x]))
                           ["bg-amber-200" "rounded-lg" "px-1"])}
            (when-let [c (c-at [y x])]
              [:div (:name c)])]])])]))

(defn actions
  [creature hp-panel]
  [:div.size-full.flex.flex-col.rounded.border.border-amber-200.p-2.bg-amber-100.relative
   [:div.h-8.w-full.flex.justify-center.font-bold.bg-amber-200.rounded.items-center.p-2
    "Playing"]
   [:div.my-2.flex.justify-center.items-center.space-x-4
    (show-creature creature hp-panel)
    [:div.flex.justify-center.items-center.space-x-1
     (->> (range 3)
          (map (fn [i]
                 [:div {:class ["size-4" "rounded" (if (< i (:actions creature))
                                                     "bg-green-500"
                                                     "bg-red-500")]}])))]]
   [:div.flex.flex-col.overflow-scroll
    ;; TODO: disabled?
    (button {:text "Strike"})
    (button {:text "Stride"})]])

(defn coming-up
  [creatures hp-panel]
  [:div.size-full.flex.flex-col.rounded.border.border-amber-200.p-2.bg-amber-100.relative
   [:div.h-8.w-full.flex.justify-center.font-bold.bg-amber-200.rounded.items-center.p-2
    "Up next"]
   [:dix.flex.justify-center.items-center.size-full.my-2
    [:div {:class ["flex" "flex-col" "items-center"
                   "overflow-y-scroll" "h-32" "space-y-4" "p-2"
                   "border-amber-200" "border" "rounded" "bg-amber-50"]}
     (->> creatures
          (map-indexed (fn [idx c]
                         [:div (show-creature c hp-panel)])))]]])

(defn combat
  [creatures targeting hp-panel]
  [:div.flex.flex-col.size-full
   [:div.h-full.overflow-scroll.mx-auto
    (grid creatures targeting)]
   [:div.w-full.h-48.flex.justify-between
    [:div {:class "w-[49%]"}
     (actions (first creatures) hp-panel)]
    [:div {:class "w-[49%]"}
     (coming-up (rest creatures) hp-panel)]]])

(defn header
  [data]
  [:div.flex.items-center
   (button {:text "< Back to games list"})
   [:div (:name data)]
   [:div.ml-2.text-sm "(GM: " (-> data :owner :name) ")"]])

(defn game-page
  [state]
  [:div.size-full.flex.flex-col
   (header (:srv/header state))
   [:div.size-full.flex.justify-center.items-center
    [:div {:class ["w-3/4" "h-full" "p-2" "flex" "flex-col"]}
     (combat (:srv/combat state)
             (:ui/targeting state)
             (:dm/hp-panel state))]
    [:div {:class ["w-1/4" "h-full"]}
     (chat (:srv/chat state) (:chat/draft state))]]])

(defn lobby-page
  [state]
  (let [me (-> state :server/data :me :name)
        creating? (-> state :client/state :creating?)
        loading? (not (:server/ready? state))
        games (-> state :server/data :me :games)]
    [:div {:class ["flex" "flex-col" "w-4/5" "h-5/6" "justify-center" "items-center"]}
     [:div {:class ["w-full" "h-4/5" "overflow-y-auto"
                    "flex" "flex-col" "items-center"
                    "border" "border-lime-200"]}
      (cond
        loading? [:div loader]
        (empty? games) [:div "You don't have any game yet! Create one, or ask for an invite."]
        :else
        (->> games
             (sort-by :created)
             (map (fn [g]
                    (let [owner (-> g :owner :name)
                          players (->> g :players (map :name) set)]
                      [:div {:class ["w-11/12" "p-2" "border-2" "rounded" "border-lime-300" "m-2" "flex" "justify-between"]}
                       [:div.px-2.py-1
                        [:div.flex
                         [:div.mr-2 (:name g)]
                         [:div.mr-2
                          [:span.mx-2 owner]
                          (->> players
                               sort
                               (map (fn [u] [:span.text-sm.text-zinc-500.mx-1 u])))]]
                        [:div.text-xs.text-zinc-500
                         (str "Campaign created on " (:created g))]]
                       [:div.h-full.flex.items-center.justify-center
                        (button {:invisible? (not= me owner)
                                 :disabled? (:deleting g)
                                 :text "Delete"})
                        (button {:invisible? (or (= me owner)
                                                 (not (contains? players me)))
                                 :disabled? (:leaving g)
                                 :text "Leave"})
                        (button {:invisible? (and (not (= me owner))
                                                  (not (contains? players me)))
                                 :text "Open"})]])))))]
     [:div.mt-4.flex
      (button {:disabled? (or creating? loading?)
               :text "Create campaign"})]]))

(defn user
  [uid]
  [:span.text-xs.fixed.top-1.right-1.bg-amber-400.p-1.rounded
   (if (= :loading uid) loader uid)])

(defn version
  []
  [:pre.text-xs.fixed.bottom-1.right-1.bg-amber-400.p-1.rounded
   (or (System/getenv "VERSION") "local")])

(defn ui-root
  [state]
  [:div.size-full.flex.justify-center.items-center.p-2
   (match (:screen/current state)
     [:lobby] (lobby-page state)
     [:game _] (game-page state)
     ;else
     [:div "This page does not exist yet."])
   (user (:me state))
   (version)])

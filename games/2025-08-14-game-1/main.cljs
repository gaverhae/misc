(ns main
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(def game-title "Game Title")

(defn screen-from-hash []
  (let [frag (.. js/window -location -hash)]
    (if (> (count frag) 1)
      (keyword (subs frag 1))
      :title)))

(defonce state (r/atom {:screen (screen-from-hash)}))

; set up basic history popstate management
(.addEventListener js/window "popstate"
  (fn [_]
    (swap! state assoc :screen (screen-from-hash))))

(defn component:icon [id]
  [:img.icon
   {:src
    (str "https://cdn.jsdelivr.net/gh/twitter/twemoji/assets/svg/" id ".svg")}])

(defn component:title [game-title]
  [:svg#game-title {:viewBox "0 0 700 200"
                    :xmlns "http://www.w3.org/2000/svg"}
   [:defs
    [:path {:id "curve" :d "M 100 150 C 200 100 500 100 600 150"}]
    [:filter {:id "drop-shadow"}
     ["feFlood" {:result "flood"}]
     ["feComposite" {:in "flood" :in2 "SourceAlpha"
                     :operator "in" :result "clip"}]
     ["feOffset" {:in "clip" :dx "0" :dy "10" :result "offset"}]
     ["feMerge"
      ["feMergeNode" {:in "offset"}]
      ["feMergeNode" {:in "SourceGraphic"}]]]]
   [:text {:stroke-width "10px"
           :filter "url(#drop-shadow)"
           :stroke-linecap "round"
           :stroke-linejoin "round"}
    [:textPath {:href "#curve" :textAnchor "middle" :startOffset "50%"}
     [:tspan game-title]]]])

(defn component:game []
  [:main
   [:div.cgv {:style {"--size" 10}}
    [:img.cgv-entity
     {:src
      "https://cdn.jsdelivr.net/gh/twitter/twemoji/assets/svg/1f47b.svg"
      :style
      {"--w" 1
       "--h" 1
       "--x" 0
       "--y" 0}}]]])

(defn component:back-button []
  [:a.button.cta {:href "#"
                  :on-click (fn [e]
                              (.preventDefault e)
                              (.replaceState js/history nil "" (.. js/window -location -pathname))
                              (swap! state assoc :screen :title))} "Back"])

(defn component:instructions []
  [:main.title.page
   [:h1 "Instructions"]
   [:p "This is a placeholder for the instructions screen."]
   [component:back-button]])

(defn component:settings []
  [:main.title.page
   [:h1 "Settings"]
   [:p "This is a placeholder for the settings screen."]
   [component:back-button]])

(defn component:credits []
  [:main.title.page
   [:h1 "Credits"]
   [:p "This is a placeholder for the credits screen."]
   [component:back-button]])

(defn component:menu []
  [:nav.menu
   [:a {:href "#instructions"
        :on-click #(swap! state assoc :screen :instructions)} "instructions"]
   [:a {:href "#settings"
        :on-click #(swap! state assoc :screen :settings)} "settings"]
   [:a {:href "#credits"
        :on-click #(swap! state assoc :screen :credits)} "credits"]
   [:a.button.cta {:href "#game"
                   :on-click #(swap! state assoc :screen :game)} "Play"]])

(defn component:title-screen []
  [:main.title
   [component:title game-title]
   [component:menu]
   #_ [component:icon "1f3ae"]])

(defn component:app []
  (let [screen (:screen @state)]
    (case screen
      :title [component:title-screen]
      :game [component:game]
      :instructions [component:instructions]
      :settings [component:settings]
      :credits [component:credits]
      [:h1 "Unknown screen: " screen])))

(rdom/render [component:app] (.getElementById js/document "app"))

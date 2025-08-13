(ns main
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(def game-title "Game Title")

(defonce state (r/atom nil))

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

(defn component:menu []
  [:nav.menu
   [:a {:href "#instructions"} "instructions"]
   [:a {:href "#settings"} "settings"]
   [:a {:href "#credits"} "credits"]
   [:a.button.cta "Play"]])

(defn component:app []
  [:main.title
   [component:title game-title]
   [component:menu]
   #_ [component:icon "1f3ae"]])

(rdom/render [component:app] (.getElementById js/document "app"))

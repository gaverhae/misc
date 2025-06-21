(ns main
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defonce state (r/atom nil))

(defn app []
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

(rdom/render [app] (.getElementById js/document "app"))

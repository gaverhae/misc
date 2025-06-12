(ns main
  (:require [io.github.humbleui.ui :as ui])
  (:gen-class))

(ui/defcomp app
  []
  [ui/center
   [ui/label "Hello, World!"]])

(defn -main
  [& args]
  (ui/start-app!
    (ui/window #'app)))

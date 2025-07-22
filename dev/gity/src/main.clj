(ns main
  (:require [io.github.humbleui.ui :as ui])
  (:gen-class))

(ui/defcomp app
  []
  [ui/center
   [ui/label "Hello, World!"]])

(defn -main
  [& args]
  (println "hello")
  (ui/start-app!
    (ui/window app)))

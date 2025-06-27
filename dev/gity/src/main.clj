(ns main
  (:import (javax.swing JFrame
                        JLabel
                        SwingUtilities))
  (:gen-class))

(defn app
  []
  (let [frame (JFrame. "Hello")
        label (JLabel. "Hello World")]
    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.add (.getContentPane frame) label)
    (.pack frame)
    (.setVisible frame true)))

(defn -main
  [& args]
  (println "hello")
  (SwingUtilities/invokeLater app))

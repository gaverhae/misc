(defproject t "alpha"
  :dependencies [[criterium "0.4.6"]
                 [hiccup "2.0.0-RC3"]
                 [org.clojure/clojure "1.12.0"]
                 [org.clojure/core.match "1.1.0"]]
  :main ^:skip-aot t.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :vjm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :repl-options {:init-ns t.core})

(defproject t "app"
  :dependencies [[org.clojure/clojure "1.12.1"]
                 [org.clojure/core.async "1.8.741"]
                 [org.clojure/core.match "1.1.0"]
                 [org.jline/jline "3.30.4"]]
  :main ^:skip-aot t.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

(defproject t "app"
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/core.match "1.1.0"]]
  :main ^:skip-aot t.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

(defproject t "app"
  :dependencies [[org.clojure/clojure "1.12.2"]]
  :main ^:skip-aot t.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

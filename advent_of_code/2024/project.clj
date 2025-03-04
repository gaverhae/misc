(defproject t "app"
  :dependencies [[com.taoensso/tufte "2.6.3"]
                 [criterium "0.4.6"]
                 [hato "1.0.0"]
                 [instaparse "1.5.0"]
                 [org.clojure/clojure "1.12.0"]
                 [org.clojure/core.async "1.7.701"]
                 [org.clojure/core.match "1.1.0"]
                 [org.clojure/data.int-map "1.3.0"]
                 [org.clojure/data.json "2.5.1"]]
  :global-vars {*warn-on-reflection* true}
  :main ^:skip-aot t.core
  :jvm-opts ["-Xverify:none" "-XX:+EnableDynamicAgentLoading"]
  :target-path "target/%s"
  :plugins [[com.jakemccrary/lein-test-refresh "0.25.0"]]
  :test-refresh {:quiet true
                 :changes-only true}
  :test-paths ["src"]
  :repl-options {:init-ns t.core}
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

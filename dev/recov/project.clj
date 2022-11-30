(defproject recov "app"
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :global-vars {*warn-on-reflection* true}
  :main ^:skip-aot recov.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
(defproject myapp "0.1.0-SNAPSHOT"
  :description "My Clojure app"
  :dependencies [[org.clojure/clojure "1.10.3"]]
  :main ^:skip-aot myapp.core
  :target-path "target/%s/"
  :profiles {:uberjar {:aot :all}})
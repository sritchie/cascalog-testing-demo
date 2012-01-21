(defproject cascalog-testing-demo "1.1.0"
  :description "Cascalog testing examples."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [cascalog "1.8.5"]]
  :dev-dependencies [[swank-clojure "1.4.0-SNAPSHOT"]
                     [clojure-source "1.2.0"]
                     [lein-midje "1.0.7"]
                     [midje-cascalog "0.4.0"]
                     [org.apache.hadoop/hadoop-core "0.20.2-dev"]]
  :aot [cascalog.testing-demo.core])

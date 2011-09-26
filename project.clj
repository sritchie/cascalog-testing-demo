(defproject cascalog-testing-demo "1.0.0-SNAPSHOT"
  :description "Cascalog testing examples."
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [cascalog "1.8.1-SNAPSHOT"]]
  :dev-dependencies [[swank-clojure "1.4.0-SNAPSHOT"]
                     [clojure-source "1.2.0"]
                     [lein-midje "1.0.3"]
                     [midje-cascalog "0.1.1"]
                     [org.apache.hadoop/hadoop-core "0.20.2-dev"]])

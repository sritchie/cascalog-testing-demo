(ns cascalog.testing-demo.examples
  (:use cascalog.api)
  (:require [cascalog.ops :as c]))

(defn complex-subquery [& _]
  "we'll mock this out in the tests!")

(defn max-followers-query [datastore-path]
  (let [src (name-vars (complex-subquery datastore-path)
                       ["?user" "?followers"])]
    (c/first-n src 1 :sort ["?followers"] :reverse true)))

(ns cascalog.testing-demo.core
  (:use cascalog.api)
  (:require [cascalog.ops :as c])
  (:gen-class))
  
(defmapcatop split
  [^String sentence]
  (seq (.split sentence "\\s+")))

(defn wc-query
    "Returns a subquery that generates wordcounts for every word in
    the textfiles located at `text-path`."
    [text-path]
    (let [src (hfs-textline text-path)]
      (<- [?word ?count]
          (src ?textline)
          (split ?textline :> ?word)
          (c/count ?count))))
  
(defn -main
  "Accepts a path to a number of text files and a results path, and
   prints each distinct word along with its count to an `hfs-textline`
   tap at `results-path`."
  [text-path results-path]
  (?- (hfs-textline results-path)
      (wc-query text-path)))

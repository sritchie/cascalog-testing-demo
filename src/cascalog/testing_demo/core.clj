(ns cascalog.testing-demo.core
  (:use cascalog.api)
  (:require [cascalog.ops :as c])
  (:gen-class))

(defmapcatop split
  "Accepts a sentence 1-tuple, splits that sentence on whitespace, and
    emits a single 1-tuple for each word."
  [^String sentence]
  (seq (.split sentence "\\s+")))

(defn wc-query
  "Returns a subquery that generates counts for every word in
      the text-files located at `text-path`."
  [text-path]
  (let [src (hfs-textline text-path)]
    (<- [?word ?count]
        (src ?textline)
        (split ?textline :> ?word)
        (c/count ?count))))

(defn -main
  "Accepts the following arguments:
  
     - text-path (path to a textfile, or directory with textfiles)
     - results-path (location of textfile containing results)
  
       And prints lines of the form \"word count\" to a textfile at
       results-path. Each distinct word in the textfiles at text-path
       gets a count."
  [text-path results-path]
  (?- (hfs-textline results-path)
      (wc-query text-path)))

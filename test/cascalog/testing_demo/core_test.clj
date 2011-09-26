(ns cascalog.testing-demo.core-test
  (:use cascalog.testing-demo.core
        cascalog.api
        [midje sweet cascalog]))

;; Caveats: If we want to use provided, against-background etc, we
;; have to bring in midje.sweet.

(def textline-tuples
  [["Call me Ishmael. Some years ago -- never mind how long"]
   ["precisely -- having little or no money in my purse, and"]
   ["nothing particular to interest me on shore, I thought I"]
   ["would sail about a little and see the watery part of the world."]])

;.;. FAIL at (NO_SOURCE_FILE:1)
;.;. Actual result did not agree with the checking function.
;.;.         Actual result: (["--" 2] ["Call" 1] ["I" 2] ["Ishmael." 1] ["Some" 1] ["a" 1] ["about" 1] ["ago" 1] ["and" 2] ["having" 1] ["how" 1] ["in" 1] ["interest" 1] ["little" 2] ["long" 1] ["me" 2] ["mind" 1] ["money" 1] ["my" 1] ["never" 1] ["no" 1] ["nothing" 1] ["of" 1] ["on" 1] ["or" 1] ["part" 1] ["particular" 1] ["precisely" 1] ["purse," 1] ["sail" 1] ["see" 1] ["shore," 1] ["the" 2] ["thought" 1] ["to" 1] ["watery" 1] ["world." 1] ["would" 1] ["years" 1])
;.;.     Checking function: (midje.sweet/just [["face"]] :in-any-order)
;.;.     The checker said this about the reason:
;.;.         Expected one element. There were thirty-nine.
(fact?- [["face"]]
        (wc-query :text-path)
        (provided
          (hfs-textline :text-path) => textline-tuples))

;; (provided (hfs-textline :text-path) => textline-tuples)

;; Second version of the above -- say we need the mocking for multiple
;; queries? Use against-background.

(fact?- (contains [["Ishmael" 1] ["sail" 1]] :in-any-order)
        (wc-query :text-path)
        
        [[1]] (wc-query :text-path)
        (against-background
          (hfs-textline :text-path) => textline-tuples))


["--" 2]
["Call" 1]
["I" 2]
["Ishmael." 1]
["Some" 1]
["a" 1]
["about" 1]
["ago" 1]
["and" 2]
["having" 1]
["how" 1]
["in" 1]
["interest" 1]
["little" 2]
["long" 1]
["me" 2]
["mind" 1]
["money" 1]
["my" 1]
["never" 1]
["no" 1]
["nothing" 1]
["of" 1]
["on" 1]
["or" 1]
["part" 1]
["particular" 1]
["precisely" 1]
["purse," 1]
["sail" 1]
["see" 1]
["shore," 1]
["the" 2]
["thought" 1]
["to" 1]
["watery" 1]
["world." 1]
["would" 1]
["years" 1]

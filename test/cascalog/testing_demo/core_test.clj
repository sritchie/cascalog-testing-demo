(ns cascalog.testing-demo.core-test
  (:use cascalog.api
        [cascalog.testing-demo core examples]
        [midje sweet cascalog])
  (:require [cascalog.ops :as c]))

;; Test from
;; http://sritchie.github.com/2011/09/29/getting-creative-with-mapreduce.html
(fact?- "Query should return a single tuple containing
        [most-popular-user, follower-count]."
        [["richhickey" 2961]]
        (max-followers-query :path)
        (provided
          (complex-subquery :path) => [["sritchie09" 180]
                                       ["richhickey" 2961]]))

;; # Midje-Cascalog Testing Operators
;;
;; ## fact?-
;;
;; Let's begin by defining a function to test:

(defn mk-inc-query [src]
  (<- [?a ?b]
      (src ?a)
      (inc ?a :> ?b)))

;; `mk-inc-query` accepts a source of 1-tuples and returns a query
;; that generates 2-tuples. To test that `mk-inc-query` actually does
;; this, you need to:

;; - supply `mk-inc-query` with tuples and
;; - check that it produces an expected set of result tuples.

;; Each of the following forms uses the `fact?-` operator to state a
;; distinct "fact" about our query. `fact?-` expects a sequence of
;; result tuples followed by the query tasked with producing them.

;; These two facts about `mk-inc-query` are true, and pass:

;; The query returned by `(mk-inc-query [[1]])`,
;; when executed,
;; returns a single tuple: `[1 2]`
(fact?- [[1 2]]
        (mk-inc-query [[1]])) ;; fact is true!

;; The query returned by `(mk-inc-query [[1] [10]])`,
;; When executed,
;; returns two tuples: `[10 11]` and `[1 2]`
(fact?- [[10 11]
         [1 2]]
        (mk-inc-query [[1] [10]])) ;; fact is true!

;; This fact is false, and fails:

;; The query returned by (mk-inc-query [[1]]),
;; when executed,
;; returns a single tuple: ["fail!" 10].
(fact?- [["fail!" 10]]
        (mk-inc-query [[1]])) ;; fact is FALSE!

;; =fact?-= can take multiples pairs of result-tuples and queries:
;;
;; Same as two true facts above.
(fact?- [[1 2]]
        (mk-inc-query [[1]])
        
        [[10 11] [1 2]]
        (mk-inc-query [[1] [10]])) ;; both facts are true!

;; Strings are ignored wherever they appear, so feel free to pepper
;;your facts with comments.
(fact?- "These results:"
        [[1 2]]
        
        "Are produced by this query:"
        (mk-inc-query [[1]])) ;; true

;; Note that facts don't have to be top level forms. It's perfectly
;; acceptable to wrap facts in =let=, if it makes the test clearer:

(let [src     [[1]]
      results [[1 2]]]
  (fact?- results (mk-inc-query src))) ;; true

;; ### Custom Log Levels
;;
;; Cascalog pipes quite a bit of logging to =stdout=. Facts suppress
;; this logging by default, only showing entries with a FATAL log
;; level.

;; If you want to see more information on fact execution, you
;; customize the log level by placing a keyword at the beginning of
;; your fact:

(fact?- :info
        [[1 2]] (mk-inc-query [[1]])) ;; true

;; As of version 0.2.1, =midje-cascalog= supports the following
;; log-level keywords, and defaults to =:fatal=:
;;
;; :off
;; :fatal
;; :warn
;; :info
;; :debug

;; ## fact?<-

;; The =fact?<-= operator allows you to define a test a query within
;; the same form. The following two facts are equivalent:

(let [src [[1]]]
  (fact?- [[1 2]]
          (<- [?a ?b]
              (src ?a)
              (inc ?a :> ?b)))) ;; true

(let [src [[1]]]
  (fact?<- [[1 2]]
           [?a ?b]
           (src ?a)
           (inc ?a :> ?b))) ;; true

;; Where =fact?-= is useful for testing full queries and workflows, I
;; find =fact?<-= useful mostly for testing how =def*op= functions
;; behave inside of queries.

;; ## future-fact?- and future-fact?<-

;; If you want to stub out an unfinished test and prevent it from
;; throwing errors, you can use =future-fact?-=, like so:

(future-fact?- "unwritten-query will convert input integer tuples to
                 strings."
               [["one"] ["two"]]
               (unwritten-query [[1] [2]]))

(let [src [[1] [2]]]
  (future-fact?<- "num->string is unwritten."
                  [["one"] ["two"]]
                  [?string]
                  (src ?num)
                  (num->string ?string)))

(let [result-vec [[1]]]
  (future-fact?- "wc-query should count words from all lines of text at
          /path/to/textfile."
                 result-vec (wc-query "/path/to/textfile")))

;; =future-fact?-= and =future-fact?<-= prevent their forms from being
;; evaluated.
;;
;; If you include a string at the beginning of a stubbed fact, it
;; shows up in Midje's test report looking like this:
;;
;;  WORK TO DO: unwritten-query will convert input integer tuples to strings.
;;  WORK TO DO: num->string is unwritten.
;;
;; The =fact?-= and =fact?<-= operators provide the tools necessary to
;; test complex MapReduce workflows as pure functions. Let's expand on
;; these concepts by creating a small project with Cascalog code we'd
;; like to test.

;; # Example Project
;;
;; ## Dependencies
;;
;; To add =midje-cascalog= support to your own project, add these
;; entries to to the =:dev-dependencies= vector within =project.clj=:
;;
;;  [lein-midje "1.0.7"]
;;  [midje-cascalog "0.3.1"]
;;
;; ## Testing WordCount
;;
;; Test on the split function:

(let [sentence [["two words"]]
      words    [["two"] ["words"]]]
  (fact?<- "split converts a sentence into words."
           words
           [?word]
           (sentence ?sentence)
           (split ?sentence :> ?word)))

;; Here's an initial try at a test of =wc-query= using =fact?-=:
;;
;; /path/to/textfile points to a textfile with a single line:
;; "another another word"
(fact?- "wc-query should count words from all lines of text at
          /path/to/textfile."
        [["word" 1] ["another" 2]]
        (wc-query "/path/to/textfile")) ;; FALSE!

;; This fact fails. Here are a few of its problems:

;; The fact depends on the way tuples are stored; it depends on an
;; outside textfile located at a hard-coded path. If the textfile
;; disappears, the fact will fail whether or not the logic of
;; =wc-query= is correct.
;;
;; The fact depends on the correctness of =hfs-textline=. if
;; =hfs-textline= fails, our fact fails.
;;
;; *Testing wc-query in isolation is difficult!* How can one test the logic of =wc-query-= without regard to how lines of text are stored?

;; ## Mocking with Midje
;;
;; The solution lies in Midje's ability to mock out a function's
;; return values. Midje can hijack =hfs-textline= and force it to
;; return anything you choose inside the body of a fact.
;;
;; Using Midje's =provided= form, the above fact passes:

(fact?- "wc-query should count words from all input sentences."
        [["word" 1] ["another" 2]]
        (wc-query :path)
        (provided
          (hfs-textline :path) => [["another another word"]])) ;; true

;; This fact states 
;;
;; - when =wc-query= is called with =:path=,
;; - it will produce two tuples: =["word" 1]= and =["another" 2]=,
;; - provided =(hfs-textline :path)= produces a single tuple: =["another another word"]=.

;;  Here's another true fact about =wc-query= that uses multiple input
;;  sentences:

(def short-sentences
  [["this is a sentence sentence"]
   ["sentence with this is repeated"]])

(def short-wordcounts
  [["sentence" 3]
   ["repeated" 1]
   ["is" 2]
   ["a" 1]
   ["this" 2]
   ["with" 1]])

;; when =wc-query= is called with =:text-path=
;; it will produce =short-sentences=,
;; provided =(hfs-textline :text-path)= produces =short-wordcounts=.
(fact?- short-wordcounts (wc-query :text-path)
        (provided
          (hfs-textline :text-path) => short-sentences))

;; A =provided= form only applies to the result-query pair directly
;; above. The first fact is false, while the second fact is true:
(let [sentence [["two words"]]
      results  [["two" 1] ["words" 1]]]
  (fact?- "provided form won't apply here!"
          results (wc-query :path) ;; false
  
          "provided applies here."
          results (wc-query :path) ;; true
          (provided
            (hfs-textline :path) => sentence)))  

;; ## Mocking Arguments
;;
;; In the above facts, I used keywords (=:path=) as mocking
;; arguments. Any form that evaluates to itself can be used as a
;; mocking argument. In vanilla Clojure, this includes strings,
;; numbers and keywords. Midje adds any symbol surrounded by dots
;; (=..path..=, =.path.=, etc.) to this mix.
;;
;; These facts about =wc-query= from above are all true, and
;; identical:
(fact?- "Mocking with keywords,"
        [["one" 1]] (wc-query :path)
        (provided (hfs-textline :path) => [["one"]])
  
        "strings,"
        [["one" 1]] (wc-query "path")
        (provided (hfs-textline "path") => [["one"]])
  
        "numbers,"
        [["one" 1]] (wc-query 100)
        (provided (hfs-textline 100) => [["one"]])
  
        "and Midje dotted symbols."
        [["one" 1]] (wc-query ..path..)
        (provided (hfs-textline ..path..) => [["one"]]))

;; ## against-background
;;
;; As discussed, the =provided= form only applies to the result-query
;; pair directly above. This limitation can make for repetitive facts,
;; when each fact depends on a mocked result:

(defn text->words [path]
  (let [src (hfs-textline path)]
    (<- [?word]
        (src ?sentence)
        (split ?sentence :> ?word)
        (:distinct false))))
  
(let [sentence [["two two"]]]
  (fact?- "text->words cuts text into words."
          [["two"] ["two"]] (text->words :path)
          (provided
            (hfs-textline :path) => sentence)
  
          "wc-query converts a sentence into words."
          [["two" 2]] (wc-query :path)
          (provided
            (hfs-textline :path) => sentence)))

;; Midje allows facts to share mocked functions with
;; =against-background=. An =against-background= form placed anywhere
;; inside the body of =fact?-= will apply to all facts inside the
;; form:

(let [sentence [["two two"]]]
  (fact?- "text->words cuts text into words."
          [["two"] ["two"]] (text->words :path)
            
          "wc-query converts a sentence into words."
          [["two" 2]]
          (wc-query :path)
            
          "wc-query fact with difference inputs."
          [["what" 1] ["a" 1] ["world!" 1]]
          (wc-query :path)
          (provided
            (hfs-textline :path) => [["what a world!"]])
        
          (against-background
            (hfs-textline :path) => sentence)))

;; Note that the third of the three above facts used its own
;; =provided= form. When the two forms are mixed, =provided= takes
;; precedence, shadowing =against-background= if need be (as above).

;; ## Collection Checkers
;;
;; For the next set of facts, let's introduce a larger set of input
;; sentences:

(def longer-sentences
  [["Call me Ishmael. Some years ago -- never mind how long"]
   ["precisely -- having little or no money in my purse, and"]
   ["nothing particular to interest me on shore, I thought I"]
   ["would sail about a little and see the watery part of the world."]])

;; One issue with the above facts is that they use very small input
;; sentences. =wc-query= will produce a rather large sequence of
;; =<word, count>= pairs for a moderate number of input
;; sentences. Facts like this are overwhelming:

  (fact?- [["Ishmael." 1]
           ["Some" 1]
           ["a" 1]
           ["about" 1]
           ["ago" 1]
           ;; and on and on...
           ]
          (wc-query :path)
          (provided (hfs-textline :path) => longer-sentences))

;; To solve this, Midje provides a number of collection checkers that
;; provide you with finer control over how queries are compared with
;; result sequences.
;;
;; ## just
;;
;; =just= is the default checker for =fact?-= and =fact?<-=; bare
;; vectors of tuples resolve to =(just result-vec :in-any-order)=. The
;; following three facts are equivalent:
(let [src   [[1] [2]]
      query (<- [?a ?b]
                (src ?a)
                (inc ?a :> ?b))]
  (fact?- "Just form, fully qualified."
          (just [[2 3] [1 2]] :in-any-order) query ;;true
  
          "Wrapping tuples in a set is indentical to including
             the :in-any-order modifier."
          (just #{[2 3] [1 2]}) query ;; true
          
          "midje-cascalog lets us drop these wrappers."
          [[2 3] [1 2]] query)) ;; true

;; Each of these facts checks that its subquery returns =[2 3]= =[1
;; 2]= exclusively, in any order. Any missing or extra tuples in the
;; result vector will cause a failure.
;;
;; Note that dropping the =:in-any-order= modifier (or the set
;; wrapper) will cause facts to fail if ordering doesn't match. This
;; makes sense sometimes when checking against top-n queries, as noted
;; in the discussion below on [[has-prefix]].

;; ## contains
;;
;; The =contains= form allows facts to check against a subset of query
;; tuples. By default, =contains= requires result tuples to be
;; contiguous and ordered: =[1 2]= within =[3 4 1 2 1]=, for example.
;;
;; These restrictions are quite limiting for most Cascalog
;; queries. The following two facts avoid both restrictions:
(fact?- (contains #{["sail" 1] ["Ishmael." 1]} :gaps-ok)
        (wc-query :path) ;; true
          
        (contains [["sail" 1] ["Ishmael." 1]] :gaps-ok :in-any-order)
        (wc-query :path) ;; true
  
        (against-background
          (hfs-textline :path) => longer-sentences))

;; The above facts test that both =["sail" 1]= and =["Ishmael." 1]=
;; appear somewhere in the results, in any order.

;; - Wrapping the result tuples in a set (vs. a vector), or adding the =:in-any-order= keyword, relaxes the ordering restriction.
;; - The =:gaps-ok= keyword relaxes the restriction that tuples must contiguous.

;; ## has-prefix
;;
;; =has-prefix= checks that the supplied tuple sequence appears at the
;;  beginning of the query's results. =has-prefix= only makes sense
;;  with queries that return sorted tuples.

;; The following fact states that =["--" 2]=, =["I" 2]= and =["and"
;; 2]=, in order, are the three most common words across all words in
;; =longer-sentences=:

(fact?- (has-prefix [["--" 2] ["I" 2] ["and" 2]])
        (-> (wc-query :path)
            (c/first-n 10 :sort ["?count"] :reverse true))
        (provided
          (hfs-textline :path) => longer-sentences)) ;; true

;; ## has-suffix
;;
;; =has-suffix= checks that the supplied tuple sequence appears at the
;; end of the query's results.

;; The following fact states that =["world." 1]=, =["would" 1]= and
;; =["years" 2]=, in order, are the last three words (by alphabetical
;; order) across all words in =longer-sentences=:

(fact?- (has-suffix [["world." 1] ["would" 1] ["years" 1]])
        (-> (wc-query :text-path)
            (c/first-n 100 :sort ["?word"]))
        (provided
          (hfs-textline :text-path) => longer-sentences)) ;; true

;; ## tabular
;;
;; In certain cases, you might like to test a single query against a
;; wide range of inputs and outputs. This quickly grows repetitive:

(fact?- [["mock" 1] ["it" 1] ["out!" 1]]
        (wc-query :path)
        (provided
          (hfs-textline :path) => [["mock it out!"]]) ;;true
  
        [["two" 3]]
        (wc-query :path)
        (provided
          (hfs-textline :path) => [["two two two"]]) ;;true
  
        [["M.M" 1] ["nathan" 1]]
        (wc-query :path)
        (provided
          (hfs-textline :path) => [["nathan M.M"]])) ;; true

;; Gah! =against-background= doesn't work here, since these facts mock
;; against different sentences each time.

;; Midje's =tabular= form provides an elegant way to collapse this
;; repetition:

(tabular
 (fact?- "Tabular generates lots of facts, one for each set of
           substitutions in the table below."
         ?results
         (wc-query :path)
         (provided
           (hfs-textline :path) => [[?sentence]]))
 ?sentence       ?results
 "mock it out!"  [["mock" 1] ["it" 1] ["out!" 1]]
 "two two two"   [["two" 3]]
 "nathan M.M"    [["M.M" 1] ["nathan" 1]]) ;; 3 true facts

;; (This one's a little involved, but the results are really
;; beautiful.)

;; =tabular= accepts three types of arguments:

;; - a single =fact?-= or =fact?<-= templating form
;; - a number of "templating variables" that start with =?= (=?sentence= and =?results=, in the above fact)
;; - any number of rows of substitutions (the above fact has three)

;; and generates a separate fact for every substitution row. It does
;; this by substituting each value into the templating form in place
;; of the header variable at the top of column.

;; The first fact generated by the above tabular fact looks like this:

(tabular
 ;; Tabular takes this templating form:
 (fact?- "Tabular generates lots of facts, one for each set of
             substitutions in the table below."
         ?results
         (wc-query :path)
         (provided
           (hfs-textline :path) => [[?sentence]]))
  
 ;; and substitutes these variables:
 ?sentence       ?results
 "mock it out!"  [["mock" 1] ["it" 1] ["out!" 1]]) ;; true
  
  ;; to produce this fact:
(fact?- [["mock" 1] ["it" 1] ["out!" 1]]
        (wc-query :path)
        (provided
          (hfs-textline :path) => [["mock it out!"]])) ;; true

;; Any variable prefixed by =?= that appears inside both the fact
;; template AND the header variables row is earmarked for
;; substitution. This means that cascalog dynamic variables are
;; totally safe, and play well with tabular.

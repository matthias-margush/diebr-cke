(ns diebrücke.dispatcher
  "Dispatcher function for clojure wrappers to Java methods."
  (:import (org.apache.kafka.streams StreamsBuilder)
           (org.apache.kafka.streams.kstream KStream KTable KGroupedStream)))

(defn dispatcher
  "Dispatcher for the multimethods in the generated clojure code.

  The dispatcher returns a vector of each argument coerced as follows:

    - If an argument implements or derives from one of the kafka classes being
      wrapped, that class is used.
    - Each fn argument is represented by :fn.
    - Otherwise, the class of the argument.

  To illustrate, the dispatch value for Kstream.map is [KStream :fn Named]:

    (defmethod map [KStream :fn Named]
      [k-stream mapper named]
      (.map k-stream (reify ...) named))
  "
  [& args]
  (into []
        (for [arg args]
          (or (first
               (filter (fn [k] (instance? k arg))
                       [org.apache.kafka.streams.StreamsBuilder
                        org.apache.kafka.streams.kstream.KStream
                        org.apache.kafka.streams.kstream.KTable
                        org.apache.kafka.streams.kstream.KGroupedStream]))
              (if (ifn? arg) :fn) (class arg)))))

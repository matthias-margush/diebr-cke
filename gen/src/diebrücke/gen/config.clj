(ns diebrücke.gen.config
  "Configuration for kafka clojure wrapper generator."
  (:import (org.apache.kafka.streams StreamsBuilder)
           (org.apache.kafka.streams.kstream KStream KTable KGroupedStream)))

(def kafka-classes
  "Kafka classes to convert to clojure."
  [org.apache.kafka.streams.StreamsBuilder
   org.apache.kafka.streams.kstream.KStream
   org.apache.kafka.streams.kstream.KTable
   org.apache.kafka.streams.kstream.KGroupedStream])

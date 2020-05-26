(ns example.wordcount
  "The kafka wordcount example in clojure using diebrücke."
  (:require [clojure.string :as str]
            [diebrücke.streams :as k])
  (:import (java.util Properties)
           (org.apache.kafka.clients.consumer ConsumerConfig)
           (org.apache.kafka.common.serialization Serdes)
           (org.apache.kafka.streams KafkaStreams StreamsConfig)
           (org.apache.kafka.streams.kstream Consumed Produced)))

(defn words
  "Break apart the line into lower case words."
  [line]
  (-> line
      (str/lower-case)
      (str/split #"\W")))

(defn word
  "Returns the word."
  [[_ word]]
  word)

(defn word-count-topology
  "Creates the wordcount topology."
  [streams-builder]
  (let [text-lines (k/stream streams-builder "words" (Consumed/with (Serdes/String) (Serdes/String)))]
    (-> text-lines
        (k/flat-map-values words)
        (k/group-by word)
        (k/count)
        (k/to-stream)
        (k/to "word-counts" (Produced/with (Serdes/String) (Serdes/Long)))))
  streams-builder)

(def config
  "Kafka configuration."
  (let [cfg {StreamsConfig/APPLICATION_ID_CONFIG            "word-countx"
             StreamsConfig/BOOTSTRAP_SERVERS_CONFIG         "localhost:9092"
             StreamsConfig/CACHE_MAX_BYTES_BUFFERING_CONFIG 0
             StreamsConfig/DEFAULT_KEY_SERDE_CLASS_CONFIG   (.getName (class (Serdes/String)))
             StreamsConfig/DEFAULT_VALUE_SERDE_CLASS_CONFIG (.getName (class (Serdes/String)))
             ConsumerConfig/AUTO_OFFSET_RESET_CONFIG        "earliest"}
        props (Properties.)]
    (.putAll props cfg)
    props))


(defn -main
  "Starts the wordcount stream topology."
  []
  (let [builder (-> (k/streams-builder)
                    (word-count-topology))
        stream (KafkaStreams. (k/build builder) config)]
    (.start stream)
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(.close stream)))))


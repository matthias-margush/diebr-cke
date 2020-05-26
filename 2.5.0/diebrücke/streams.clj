(ns diebrücke.streams
  (:require [diebrücke.dispatcher :refer [dispatcher]])
  (:import (java.lang String)
           (java.util Collection Properties)
           (java.util.function Function)
           (java.util.regex Pattern)
           (org.apache.kafka.streams KeyValue Topology StreamsBuilder)
           (org.apache.kafka.streams.kstream Reducer Printed ValueTransformerSupplier SessionWindowedKStream StreamJoined GlobalKTable SessionWindows TimeWindowedKStream Aggregator Produced Consumed KeyValueMapper Initializer ValueMapper Suppressed ForeachAction Joined Named Windows KGroupedTable CogroupedKStream KGroupedStream JoinWindows Grouped ValueMapperWithKey ValueJoiner KTable TransformerSupplier Predicate ValueTransformerWithKeySupplier KStream Materialized Serialized)
           (org.apache.kafka.streams.processor ProcessorSupplier TopicNameExtractor)
           (org.apache.kafka.streams.state StoreBuilder))
  (:refer-clojure :only [boolean defmethod defmulti let reify]))

(defmulti join dispatcher)

(defmethod join [KTable KTable Function :fn Materialized]
  [k-table other foreign-key-extractor joiner materialized]
  (.join k-table other foreign-key-extractor (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) materialized))

(defmethod join [KTable KTable Function :fn]
  [k-table other foreign-key-extractor joiner]
  (.join k-table other foreign-key-extractor (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2)))))

(defmethod join [KStream KStream :fn JoinWindows]
  [k-stream other-stream joiner windows]
  (.join k-stream other-stream (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) windows))

(defmethod join [KStream :fn :fn :fn]
  [k-stream global-table key-selector joiner]
  (.join k-stream (reify GlobalKTable (queryableStoreName [_] (global-table))) (reify KeyValueMapper (apply [_ key value] (let [[k v] (key-selector [key value])] (KeyValue. k v)))) (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2)))))

(defmethod join [KStream KStream :fn JoinWindows Joined]
  [k-stream other-stream joiner windows joined]
  (.join k-stream other-stream (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) windows joined))

(defmethod join [KStream KTable :fn]
  [k-stream table joiner]
  (.join k-stream table (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2)))))

(defmethod join [KTable KTable :fn Named]
  [k-table other joiner named]
  (.join k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named))

(defmethod join [KStream KStream :fn JoinWindows StreamJoined]
  [k-stream other-stream joiner windows stream-joined]
  (.join k-stream other-stream (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) windows stream-joined))

(defmethod join [KTable KTable :fn Materialized]
  [k-table other joiner materialized]
  (.join k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) materialized))

(defmethod join [KTable KTable Function :fn Named]
  [k-table other foreign-key-extractor joiner named]
  (.join k-table other foreign-key-extractor (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named))

(defmethod join [KTable KTable Function :fn Named Materialized]
  [k-table other foreign-key-extractor joiner named materialized]
  (.join k-table other foreign-key-extractor (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named materialized))

(defmethod join [KStream KTable :fn Joined]
  [k-stream table joiner joined]
  (.join k-stream table (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) joined))

(defmethod join [KTable KTable :fn]
  [k-table other joiner]
  (.join k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2)))))

(defmethod join [KStream :fn :fn :fn Named]
  [k-stream global-table key-selector joiner named]
  (.join k-stream (reify GlobalKTable (queryableStoreName [_] (global-table))) (reify KeyValueMapper (apply [_ key value] (let [[k v] (key-selector [key value])] (KeyValue. k v)))) (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named))

(defmethod join [KTable KTable :fn Named Materialized]
  [k-table other joiner named materialized]
  (.join k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named materialized))

(defmulti reduce dispatcher)

(defmethod reduce [KGroupedStream :fn]
  [k-grouped-stream reducer]
  (.reduce k-grouped-stream (reify Reducer (apply [_ value-1 value-2] (reducer value-1 value-2)))))

(defmethod reduce [KGroupedStream :fn Materialized]
  [k-grouped-stream reducer materialized]
  (.reduce k-grouped-stream (reify Reducer (apply [_ value-1 value-2] (reducer value-1 value-2))) materialized))

(defmethod reduce [KGroupedStream :fn Named Materialized]
  [k-grouped-stream reducer named materialized]
  (.reduce k-grouped-stream (reify Reducer (apply [_ value-1 value-2] (reducer value-1 value-2))) named materialized))

(defmulti peek dispatcher)

(defmethod peek [KStream :fn]
  [k-stream action]
  (.peek k-stream (reify ForeachAction (apply [_ key value] (do (action [key value]) nil)))))

(defmethod peek [KStream :fn Named]
  [k-stream action named]
  (.peek k-stream (reify ForeachAction (apply [_ key value] (do (action [key value]) nil))) named))

(defmulti global-table dispatcher)

(defmethod global-table [StreamsBuilder String Consumed]
  [streams-builder topic consumed]
  (.globalTable streams-builder topic consumed))

(defmethod global-table [StreamsBuilder String Consumed Materialized]
  [streams-builder topic consumed materialized]
  (.globalTable streams-builder topic consumed materialized))

(defmethod global-table [StreamsBuilder String]
  [streams-builder topic]
  (.globalTable streams-builder topic))

(defmethod global-table [StreamsBuilder String Materialized]
  [streams-builder topic materialized]
  (.globalTable streams-builder topic materialized))

(defmulti streams-builder dispatcher)

(defmethod streams-builder []
  []
  (StreamsBuilder.))

(defmulti build dispatcher)

(defmethod build [StreamsBuilder Properties]
  [streams-builder props]
  (.build streams-builder props))

(defmethod build [StreamsBuilder]
  [streams-builder]
  (.build streams-builder))

(defmulti group-by dispatcher)

(defmethod group-by [KTable :fn Serialized]
  [k-table selector serialized]
  (.groupBy k-table (reify KeyValueMapper (apply [_ key value] (selector [key value]))) serialized))

(defmethod group-by [KStream :fn Serialized]
  [k-stream key-selector serialized]
  (.groupBy k-stream (reify KeyValueMapper (apply [_ key value] (key-selector [key value]))) serialized))

(defmethod group-by [KStream :fn]
  [k-stream key-selector]
  (.groupBy k-stream (reify KeyValueMapper (apply [_ key value] (key-selector [key value])))))

(defmethod group-by [KStream :fn Grouped]
  [k-stream key-selector grouped]
  (.groupBy k-stream (reify KeyValueMapper (apply [_ key value] (key-selector [key value]))) grouped))

(defmethod group-by [KTable :fn Grouped]
  [k-table selector grouped]
  (.groupBy k-table (reify KeyValueMapper (apply [_ key value] (selector [key value]))) grouped))

(defmethod group-by [KTable :fn]
  [k-table selector]
  (.groupBy k-table (reify KeyValueMapper (apply [_ key value] (selector [key value])))))

(defmulti branch dispatcher)

(defmethod branch [KStream (Class/forName "[Lorg.apache.kafka.streams.kstream.Predicate;")]
  [k-stream predicates]
  (.branch k-stream predicates))

(defmethod branch [KStream Named (Class/forName "[Lorg.apache.kafka.streams.kstream.Predicate;")]
  [k-stream named predicates]
  (.branch k-stream named predicates))

(defmulti windowed-by dispatcher)

(defmethod windowed-by [KGroupedStream Windows]
  [k-grouped-stream windows]
  (.windowedBy k-grouped-stream windows))

(defmethod windowed-by [KGroupedStream SessionWindows]
  [k-grouped-stream windows]
  (.windowedBy k-grouped-stream windows))

(defmulti to-table dispatcher)

(defmethod to-table [KStream Materialized]
  [k-stream materialized]
  (.toTable k-stream materialized))

(defmethod to-table [KStream Named]
  [k-stream named]
  (.toTable k-stream named))

(defmethod to-table [KStream Named Materialized]
  [k-stream named materialized]
  (.toTable k-stream named materialized))

(defmethod to-table [KStream]
  [k-stream]
  (.toTable k-stream))

(defmulti queryable-store-name dispatcher)

(defmethod queryable-store-name [KTable]
  [k-table]
  (.queryableStoreName k-table))

(defmulti to dispatcher)

(defmethod to [KStream :fn Produced]
  [k-stream topic-extractor produced]
  (.to k-stream (reify TopicNameExtractor (extract [_ key value record-context] (topic-extractor record-context [key value]))) produced))

(defmethod to [KStream String Produced]
  [k-stream topic produced]
  (.to k-stream topic produced))

(defmethod to [KStream String]
  [k-stream topic]
  (.to k-stream topic))

(defmethod to [KStream :fn]
  [k-stream topic-extractor]
  (.to k-stream (reify TopicNameExtractor (extract [_ key value record-context] (topic-extractor record-context [key value])))))

(defmulti filter-not dispatcher)

(defmethod filter-not [KTable :fn Named Materialized]
  [k-table predicate named materialized]
  (.filterNot k-table (reify Predicate (test [_ key value] (boolean (predicate [key value])))) named materialized))

(defmethod filter-not [KTable :fn]
  [k-table predicate]
  (.filterNot k-table (reify Predicate (test [_ key value] (boolean (predicate [key value]))))))

(defmethod filter-not [KStream :fn]
  [k-stream predicate]
  (.filterNot k-stream (reify Predicate (test [_ key value] (boolean (predicate [key value]))))))

(defmethod filter-not [KTable :fn Named]
  [k-table predicate named]
  (.filterNot k-table (reify Predicate (test [_ key value] (boolean (predicate [key value])))) named))

(defmethod filter-not [KTable :fn Materialized]
  [k-table predicate materialized]
  (.filterNot k-table (reify Predicate (test [_ key value] (boolean (predicate [key value])))) materialized))

(defmethod filter-not [KStream :fn Named]
  [k-stream predicate named]
  (.filterNot k-stream (reify Predicate (test [_ key value] (boolean (predicate [key value])))) named))

(defmulti group-by-key dispatcher)

(defmethod group-by-key [KStream Grouped]
  [k-stream grouped]
  (.groupByKey k-stream grouped))

(defmethod group-by-key [KStream]
  [k-stream]
  (.groupByKey k-stream))

(defmethod group-by-key [KStream Serialized]
  [k-stream serialized]
  (.groupByKey k-stream serialized))

(defmulti flat-transform-values dispatcher)

(defmulti flat-transform-values-with-key dispatcher)

(defmethod flat-transform-values [KStream :fn (Class/forName "[Ljava.lang.String;")]
  [k-stream value-transformer-supplier state-store-names]
  (.flatTransformValues k-stream (reify ValueTransformerSupplier (get [_] (value-transformer-supplier))) state-store-names))

(defmethod flat-transform-values-with-key [KStream :fn Named (Class/forName "[Ljava.lang.String;")]
  [k-stream value-transformer-supplier named state-store-names]
  (.flatTransformValues k-stream (reify ValueTransformerWithKeySupplier (get [_] (value-transformer-supplier))) named state-store-names))

(defmethod flat-transform-values [KStream :fn Named (Class/forName "[Ljava.lang.String;")]
  [k-stream value-transformer-supplier named state-store-names]
  (.flatTransformValues k-stream (reify ValueTransformerSupplier (get [_] (value-transformer-supplier))) named state-store-names))

(defmethod flat-transform-values-with-key [KStream :fn (Class/forName "[Ljava.lang.String;")]
  [k-stream value-transformer-supplier state-store-names]
  (.flatTransformValues k-stream (reify ValueTransformerWithKeySupplier (get [_] (value-transformer-supplier))) state-store-names))

(defmulti transform-values dispatcher)

(defmulti transform-values-with-key dispatcher)

(defmethod transform-values-with-key [KTable :fn (Class/forName "[Ljava.lang.String;")]
  [k-table transformer-supplier state-store-names]
  (.transformValues k-table (reify ValueTransformerWithKeySupplier (get [_] (transformer-supplier))) state-store-names))

(defmethod transform-values-with-key [KTable :fn Named (Class/forName "[Ljava.lang.String;")]
  [k-table transformer-supplier named state-store-names]
  (.transformValues k-table (reify ValueTransformerWithKeySupplier (get [_] (transformer-supplier))) named state-store-names))

(defmethod transform-values-with-key [KStream :fn (Class/forName "[Ljava.lang.String;")]
  [k-stream value-transformer-supplier state-store-names]
  (.transformValues k-stream (reify ValueTransformerWithKeySupplier (get [_] (value-transformer-supplier))) state-store-names))

(defmethod transform-values-with-key [KTable :fn Materialized (Class/forName "[Ljava.lang.String;")]
  [k-table transformer-supplier materialized state-store-names]
  (.transformValues k-table (reify ValueTransformerWithKeySupplier (get [_] (transformer-supplier))) materialized state-store-names))

(defmethod transform-values-with-key [KTable :fn Materialized Named (Class/forName "[Ljava.lang.String;")]
  [k-table transformer-supplier materialized named state-store-names]
  (.transformValues k-table (reify ValueTransformerWithKeySupplier (get [_] (transformer-supplier))) materialized named state-store-names))

(defmethod transform-values [KStream :fn (Class/forName "[Ljava.lang.String;")]
  [k-stream value-transformer-supplier state-store-names]
  (.transformValues k-stream (reify ValueTransformerSupplier (get [_] (value-transformer-supplier))) state-store-names))

(defmethod transform-values-with-key [KStream :fn Named (Class/forName "[Ljava.lang.String;")]
  [k-stream value-transformer-supplier named state-store-names]
  (.transformValues k-stream (reify ValueTransformerWithKeySupplier (get [_] (value-transformer-supplier))) named state-store-names))

(defmethod transform-values [KStream :fn Named (Class/forName "[Ljava.lang.String;")]
  [k-stream value-transformer-supplier named state-store-names]
  (.transformValues k-stream (reify ValueTransformerSupplier (get [_] (value-transformer-supplier))) named state-store-names))

(defmulti process dispatcher)

(defmethod process [KStream :fn Named (Class/forName "[Ljava.lang.String;")]
  [k-stream processor-supplier named state-store-names]
  (.process k-stream (reify ProcessorSupplier (get [_] (processor-supplier))) named state-store-names))

(defmethod process [KStream :fn (Class/forName "[Ljava.lang.String;")]
  [k-stream processor-supplier state-store-names]
  (.process k-stream (reify ProcessorSupplier (get [_] (processor-supplier))) state-store-names))

(defmulti left-join dispatcher)

(defmethod left-join [KStream KTable :fn]
  [k-stream table joiner]
  (.leftJoin k-stream table (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2)))))

(defmethod left-join [KTable KTable Function :fn Named]
  [k-table other foreign-key-extractor joiner named]
  (.leftJoin k-table other foreign-key-extractor (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named))

(defmethod left-join [KTable KTable :fn Named Materialized]
  [k-table other joiner named materialized]
  (.leftJoin k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named materialized))

(defmethod left-join [KTable KTable Function :fn Materialized]
  [k-table other foreign-key-extractor joiner materialized]
  (.leftJoin k-table other foreign-key-extractor (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) materialized))

(defmethod left-join [KStream KStream :fn JoinWindows]
  [k-stream other-stream joiner windows]
  (.leftJoin k-stream other-stream (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) windows))

(defmethod left-join [KStream KTable :fn Joined]
  [k-stream table joiner joined]
  (.leftJoin k-stream table (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) joined))

(defmethod left-join [KTable KTable :fn Materialized]
  [k-table other joiner materialized]
  (.leftJoin k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) materialized))

(defmethod left-join [KStream KStream :fn JoinWindows StreamJoined]
  [k-stream other-stream joiner windows stream-joined]
  (.leftJoin k-stream other-stream (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) windows stream-joined))

(defmethod left-join [KTable KTable :fn]
  [k-table other joiner]
  (.leftJoin k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2)))))

(defmethod left-join [KTable KTable Function :fn]
  [k-table other foreign-key-extractor joiner]
  (.leftJoin k-table other foreign-key-extractor (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2)))))

(defmethod left-join [KTable KTable Function :fn Named Materialized]
  [k-table other foreign-key-extractor joiner named materialized]
  (.leftJoin k-table other foreign-key-extractor (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named materialized))

(defmethod left-join [KStream :fn :fn :fn]
  [k-stream global-table key-selector value-joiner]
  (.leftJoin k-stream (reify GlobalKTable (queryableStoreName [_] (global-table))) (reify KeyValueMapper (apply [_ key value] (let [[k v] (key-selector [key value])] (KeyValue. k v)))) (reify ValueJoiner (apply [_ value-1 value-2] (value-joiner value-1 value-2)))))

(defmethod left-join [KTable KTable :fn Named]
  [k-table other joiner named]
  (.leftJoin k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named))

(defmethod left-join [KStream :fn :fn :fn Named]
  [k-stream global-table key-selector value-joiner named]
  (.leftJoin k-stream (reify GlobalKTable (queryableStoreName [_] (global-table))) (reify KeyValueMapper (apply [_ key value] (let [[k v] (key-selector [key value])] (KeyValue. k v)))) (reify ValueJoiner (apply [_ value-1 value-2] (value-joiner value-1 value-2))) named))

(defmethod left-join [KStream KStream :fn JoinWindows Joined]
  [k-stream other-stream joiner windows joined]
  (.leftJoin k-stream other-stream (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) windows joined))

(defmulti foreach dispatcher)

(defmethod foreach [KStream :fn Named]
  [k-stream action named]
  (.foreach k-stream (reify ForeachAction (apply [_ key value] (do (action [key value]) nil))) named))

(defmethod foreach [KStream :fn]
  [k-stream action]
  (.foreach k-stream (reify ForeachAction (apply [_ key value] (do (action [key value]) nil)))))

(defmulti map dispatcher)

(defmethod map [KStream :fn Named]
  [k-stream mapper named]
  (.map k-stream (reify KeyValueMapper (apply [_ key value] (let [[k v] (mapper [key value])] (KeyValue. k v)))) named))

(defmethod map [KStream :fn]
  [k-stream mapper]
  (.map k-stream (reify KeyValueMapper (apply [_ key value] (let [[k v] (mapper [key value])] (KeyValue. k v))))))

(defmulti to-stream dispatcher)

(defmethod to-stream [KTable Named]
  [k-table named]
  (.toStream k-table named))

(defmethod to-stream [KTable :fn]
  [k-table mapper]
  (.toStream k-table (reify KeyValueMapper (apply [_ key value] (let [[k v] (mapper [key value])] (KeyValue. k v))))))

(defmethod to-stream [KTable :fn Named]
  [k-table mapper named]
  (.toStream k-table (reify KeyValueMapper (apply [_ key value] (let [[k v] (mapper [key value])] (KeyValue. k v)))) named))

(defmethod to-stream [KTable]
  [k-table]
  (.toStream k-table))

(defmulti filter dispatcher)

(defmethod filter [KStream :fn]
  [k-stream predicate]
  (.filter k-stream (reify Predicate (test [_ key value] (boolean (predicate [key value]))))))

(defmethod filter [KTable :fn Named Materialized]
  [k-table predicate named materialized]
  (.filter k-table (reify Predicate (test [_ key value] (boolean (predicate [key value])))) named materialized))

(defmethod filter [KTable :fn Materialized]
  [k-table predicate materialized]
  (.filter k-table (reify Predicate (test [_ key value] (boolean (predicate [key value])))) materialized))

(defmethod filter [KTable :fn Named]
  [k-table predicate named]
  (.filter k-table (reify Predicate (test [_ key value] (boolean (predicate [key value])))) named))

(defmethod filter [KStream :fn Named]
  [k-stream predicate named]
  (.filter k-stream (reify Predicate (test [_ key value] (boolean (predicate [key value])))) named))

(defmethod filter [KTable :fn]
  [k-table predicate]
  (.filter k-table (reify Predicate (test [_ key value] (boolean (predicate [key value]))))))

(defmulti count dispatcher)

(defmethod count [KGroupedStream Named Materialized]
  [k-grouped-stream named materialized]
  (.count k-grouped-stream named materialized))

(defmethod count [KGroupedStream Named]
  [k-grouped-stream named]
  (.count k-grouped-stream named))

(defmethod count [KGroupedStream]
  [k-grouped-stream]
  (.count k-grouped-stream))

(defmethod count [KGroupedStream Materialized]
  [k-grouped-stream materialized]
  (.count k-grouped-stream materialized))

(defmulti flat-map dispatcher)

(defmethod flat-map [KStream :fn Named]
  [k-stream mapper named]
  (.flatMap k-stream (reify KeyValueMapper (apply [_ key value] (let [[k v] (mapper [key value])] (KeyValue. k v)))) named))

(defmethod flat-map [KStream :fn]
  [k-stream mapper]
  (.flatMap k-stream (reify KeyValueMapper (apply [_ key value] (let [[k v] (mapper [key value])] (KeyValue. k v))))))

(defmulti stream dispatcher)

(defmethod stream [StreamsBuilder Collection]
  [streams-builder topics]
  (.stream streams-builder topics))

(defmethod stream [StreamsBuilder Collection Consumed]
  [streams-builder topics consumed]
  (.stream streams-builder topics consumed))

(defmethod stream [StreamsBuilder Pattern Consumed]
  [streams-builder topic-pattern consumed]
  (.stream streams-builder topic-pattern consumed))

(defmethod stream [StreamsBuilder String]
  [streams-builder topic]
  (.stream streams-builder topic))

(defmethod stream [StreamsBuilder Pattern]
  [streams-builder topic-pattern]
  (.stream streams-builder topic-pattern))

(defmethod stream [StreamsBuilder String Consumed]
  [streams-builder topic consumed]
  (.stream streams-builder topic consumed))

(defmulti through dispatcher)

(defmethod through [KStream String]
  [k-stream topic]
  (.through k-stream topic))

(defmethod through [KStream String Produced]
  [k-stream topic produced]
  (.through k-stream topic produced))

(defmulti merge dispatcher)

(defmethod merge [KStream KStream]
  [k-stream stream]
  (.merge k-stream stream))

(defmethod merge [KStream KStream Named]
  [k-stream stream named]
  (.merge k-stream stream named))

(defmulti outer-join dispatcher)

(defmethod outer-join [KTable KTable :fn]
  [k-table other joiner]
  (.outerJoin k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2)))))

(defmethod outer-join [KTable KTable :fn Materialized]
  [k-table other joiner materialized]
  (.outerJoin k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) materialized))

(defmethod outer-join [KStream KStream :fn JoinWindows Joined]
  [k-stream other-stream joiner windows joined]
  (.outerJoin k-stream other-stream (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) windows joined))

(defmethod outer-join [KTable KTable :fn Named]
  [k-table other joiner named]
  (.outerJoin k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named))

(defmethod outer-join [KStream KStream :fn JoinWindows]
  [k-stream other-stream joiner windows]
  (.outerJoin k-stream other-stream (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) windows))

(defmethod outer-join [KTable KTable :fn Named Materialized]
  [k-table other joiner named materialized]
  (.outerJoin k-table other (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) named materialized))

(defmethod outer-join [KStream KStream :fn JoinWindows StreamJoined]
  [k-stream other-stream joiner windows stream-joined]
  (.outerJoin k-stream other-stream (reify ValueJoiner (apply [_ value-1 value-2] (joiner value-1 value-2))) windows stream-joined))

(defmulti print dispatcher)

(defmethod print [KStream Printed]
  [k-stream printed]
  (.print k-stream printed))

(defmulti flat-map-values dispatcher)

(defmulti flat-map-values-with-key dispatcher)

(defmethod flat-map-values [KStream :fn]
  [k-stream mapper]
  (.flatMapValues k-stream (reify ValueMapper (apply [_ value] (mapper value)))))

(defmethod flat-map-values-with-key [KStream :fn Named]
  [k-stream mapper named]
  (.flatMapValues k-stream (reify ValueMapperWithKey (apply [_ read-only-key value] (mapper [read-only-key value]))) named))

(defmethod flat-map-values-with-key [KStream :fn]
  [k-stream mapper]
  (.flatMapValues k-stream (reify ValueMapperWithKey (apply [_ read-only-key value] (mapper [read-only-key value])))))

(defmethod flat-map-values [KStream :fn Named]
  [k-stream mapper named]
  (.flatMapValues k-stream (reify ValueMapper (apply [_ value] (mapper value))) named))

(defmulti transform dispatcher)

(defmethod transform [KStream :fn (Class/forName "[Ljava.lang.String;")]
  [k-stream transformer-supplier state-store-names]
  (.transform k-stream (reify TransformerSupplier (get [_] (transformer-supplier))) state-store-names))

(defmethod transform [KStream :fn Named (Class/forName "[Ljava.lang.String;")]
  [k-stream transformer-supplier named state-store-names]
  (.transform k-stream (reify TransformerSupplier (get [_] (transformer-supplier))) named state-store-names))

(defmulti map-values dispatcher)

(defmulti map-values-with-key dispatcher)

(defmethod map-values [KStream :fn Named]
  [k-stream mapper named]
  (.mapValues k-stream (reify ValueMapper (apply [_ value] (mapper value))) named))

(defmethod map-values-with-key [KTable :fn Named]
  [k-table mapper named]
  (.mapValues k-table (reify ValueMapperWithKey (apply [_ read-only-key value] (mapper [read-only-key value]))) named))

(defmethod map-values [KStream :fn]
  [k-stream mapper]
  (.mapValues k-stream (reify ValueMapper (apply [_ value] (mapper value)))))

(defmethod map-values-with-key [KStream :fn]
  [k-stream mapper]
  (.mapValues k-stream (reify ValueMapperWithKey (apply [_ read-only-key value] (mapper [read-only-key value])))))

(defmethod map-values [KTable :fn Materialized]
  [k-table mapper materialized]
  (.mapValues k-table (reify ValueMapper (apply [_ value] (mapper value))) materialized))

(defmethod map-values-with-key [KTable :fn Named Materialized]
  [k-table mapper named materialized]
  (.mapValues k-table (reify ValueMapperWithKey (apply [_ read-only-key value] (mapper [read-only-key value]))) named materialized))

(defmethod map-values [KTable :fn Named Materialized]
  [k-table mapper named materialized]
  (.mapValues k-table (reify ValueMapper (apply [_ value] (mapper value))) named materialized))

(defmethod map-values [KTable :fn Named]
  [k-table mapper named]
  (.mapValues k-table (reify ValueMapper (apply [_ value] (mapper value))) named))

(defmethod map-values-with-key [KTable :fn Materialized]
  [k-table mapper materialized]
  (.mapValues k-table (reify ValueMapperWithKey (apply [_ read-only-key value] (mapper [read-only-key value]))) materialized))

(defmethod map-values-with-key [KStream :fn Named]
  [k-stream mapper named]
  (.mapValues k-stream (reify ValueMapperWithKey (apply [_ read-only-key value] (mapper [read-only-key value]))) named))

(defmethod map-values-with-key [KTable :fn]
  [k-table mapper]
  (.mapValues k-table (reify ValueMapperWithKey (apply [_ read-only-key value] (mapper [read-only-key value])))))

(defmethod map-values [KTable :fn]
  [k-table mapper]
  (.mapValues k-table (reify ValueMapper (apply [_ value] (mapper value)))))

(defmulti add-global-store dispatcher)

(defmethod add-global-store [StreamsBuilder StoreBuilder String String Consumed String :fn]
  [streams-builder store-builder topic source-name consumed processor-name state-update-supplier]
  (.addGlobalStore streams-builder store-builder topic source-name consumed processor-name (reify ProcessorSupplier (get [_] (state-update-supplier)))))

(defmethod add-global-store [StreamsBuilder StoreBuilder String Consumed :fn]
  [streams-builder store-builder topic consumed state-update-supplier]
  (.addGlobalStore streams-builder store-builder topic consumed (reify ProcessorSupplier (get [_] (state-update-supplier)))))

(defmulti aggregate dispatcher)

(defmethod aggregate [KGroupedStream :fn :fn Named Materialized]
  [k-grouped-stream initializer aggregator named materialized]
  (.aggregate k-grouped-stream (reify Initializer (apply [_] (initializer))) (reify Aggregator (apply [_ key value aggregate] (aggregator aggregate [key value]))) named materialized))

(defmethod aggregate [KGroupedStream :fn :fn]
  [k-grouped-stream initializer aggregator]
  (.aggregate k-grouped-stream (reify Initializer (apply [_] (initializer))) (reify Aggregator (apply [_ key value aggregate] (aggregator aggregate [key value])))))

(defmethod aggregate [KGroupedStream :fn :fn Materialized]
  [k-grouped-stream initializer aggregator materialized]
  (.aggregate k-grouped-stream (reify Initializer (apply [_] (initializer))) (reify Aggregator (apply [_ key value aggregate] (aggregator aggregate [key value]))) materialized))

(defmulti select-key dispatcher)

(defmethod select-key [KStream :fn Named]
  [k-stream mapper named]
  (.selectKey k-stream (reify KeyValueMapper (apply [_ key value] (let [[k v] (mapper [key value])] (KeyValue. k v)))) named))

(defmethod select-key [KStream :fn]
  [k-stream mapper]
  (.selectKey k-stream (reify KeyValueMapper (apply [_ key value] (let [[k v] (mapper [key value])] (KeyValue. k v))))))

(defmulti add-state-store dispatcher)

(defmethod add-state-store [StreamsBuilder StoreBuilder]
  [streams-builder builder]
  (.addStateStore streams-builder builder))

(defmulti flat-transform dispatcher)

(defmethod flat-transform [KStream :fn Named (Class/forName "[Ljava.lang.String;")]
  [k-stream transformer-supplier named state-store-names]
  (.flatTransform k-stream (reify TransformerSupplier (get [_] (transformer-supplier))) named state-store-names))

(defmethod flat-transform [KStream :fn (Class/forName "[Ljava.lang.String;")]
  [k-stream transformer-supplier state-store-names]
  (.flatTransform k-stream (reify TransformerSupplier (get [_] (transformer-supplier))) state-store-names))

(defmulti table dispatcher)

(defmethod table [StreamsBuilder String Materialized]
  [streams-builder topic materialized]
  (.table streams-builder topic materialized))

(defmethod table [StreamsBuilder String Consumed Materialized]
  [streams-builder topic consumed materialized]
  (.table streams-builder topic consumed materialized))

(defmethod table [StreamsBuilder String]
  [streams-builder topic]
  (.table streams-builder topic))

(defmethod table [StreamsBuilder String Consumed]
  [streams-builder topic consumed]
  (.table streams-builder topic consumed))

(defmulti suppress dispatcher)

(defmethod suppress [KTable Suppressed]
  [k-table suppressed]
  (.suppress k-table suppressed))

(defmulti cogroup dispatcher)

(defmethod cogroup [KGroupedStream :fn]
  [k-grouped-stream aggregator]
  (.cogroup k-grouped-stream (reify Aggregator (apply [_ key value aggregate] (aggregator aggregate [key value])))))
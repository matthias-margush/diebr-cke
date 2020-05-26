(ns diebrücke.gen.lambda
  "Code that generates wrappers around kafka's java lambda callbacks to
  idimoatic clojure functions."
  (:require [diebrücke.gen.sym :as sym])
  (:import (org.apache.kafka.streams.kstream Aggregator ForeachAction KeyValueMapper Predicate ValueMapperWithKey)))

(defn- ->lambda-info
  "Extracts contextual cues about a kafka callback that are needed in ->lambda-call."
  [{:keys [name]} {:keys [declaring-class return-type parameters] :as p}]
  (let [params (mapv (comp sym/->param :name) parameters)
        kv (->> (partition 2 1 params)
                (some #{['key 'value] ['read-only-key 'value]}))
        rest (remove (into #{} kv) params)]
    [(if (= name 'groupBy) :group-by :_)
     (sym/->type declaring-class)
     (sym/->type return-type)
     (if kv `[~@rest ~kv]  params)]))

(defn- ->lambda-call
  "Generates code that calls a clojure callback.

  When a clojure fn is called back from a streams topology, the parameters from
  the Java side are coerced into a form that's idiomatic clojure, and the
  clojure return value needs to be coerced back into what the Java API expects.

  * Callbacks with a key and value pair are translated to a single clojure
    parameter consisting of the pair `[k v]`. This matches the clojure idiom
    when mapping over clojure maps.
  * Aggregator parameters on the Java side are moved to the front of the
    parameter list for the clojure side. This matches the way clojure functions
    like `reduce` work.
  * Return values from the clojure callback are coerced back to what Java will
    expect - truthy values are coerced to Boolean, when Void is expected, nil
    is returned, and [k v] pairs are returned as kafka stream's `KeyValue`.

  This is one of the few places that needs some heuristics. The way this is
  accomplished is by case-ing rules from the contextual cues that obtained by
  the ->lambda-info function."
  [name method parameter]
  (let [callback (sym/->param name)
        [caller cls ret args] (->lambda-info method (:lambda parameter))]
    (case [caller cls ret args]
      [:_ Predicate Boolean [[key value]]]
      ;; coerce the result back to a Java boolean
      `(~'boolean (~callback ~@args))

      [:_ KeyValueMapper Object [[key value]]]
      ;; coerce (key, value) java parameters to [k v], and the result back to `KeyValue`
      `(~'let [[~'k ~'v] (~callback ~@args)] (~'KeyValue. ~'k ~'v))

      [:group-by KeyValueMapper Object [[key value]]]
      ;; the groupBy method takes a key and value, but returns a single value.
      ;; this needs to be accounted for separately from the default KeyValueMapper behavior above
      `(~callback ~@args)

      [:_ ValueMapperWithKey Object [[read-only-key value]]]
      ;; ValueMapperWith key takes a key value pair, but returns a single value.
      ;; This rule accounts for the parameter name `read-only-key`.
      `(~callback ~@args)

      [:_ ForeachAction Void [[key value]]]
      ;; for-each returns void
      `(~'do (~callback ~@args) nil)

      ;; Default
      `(~callback ~@args))))

(defn ->reify
  "Generate a `reify` statement that wraps a clojure fn in an instance of the
  Java lambda class."
  [method {:keys [type name lambda] :as parameter}]
  (let [args (map (comp sym/->param :name) (:parameters lambda))]
    `(~'reify ~(sym/->type type)
               (~(:name lambda) [~'_ ~@args]
                  ~(->lambda-call name method parameter)))))

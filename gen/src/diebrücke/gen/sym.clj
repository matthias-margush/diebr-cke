(ns diebrücke.gen.sym
  "Helpers for converting Java-style identifies to clojure-style."
  (:require [camel-snake-kebab.core :refer [->kebab-case-symbol ->PascalCaseSymbol]]
            [clojure.string :as str]))

(defn ->param
  "Converts a Java symbol into one suitable as a clojure parameter.

  For example: org.apache.kafka.streams.kstream.ValueJoiner -> value-joiner"
  [s]
  (-> (str s)
      (str/split #"\.")
      (last)
      (->kebab-case-symbol)))

(defn ->type
  "Converts a Java symbol onto one suitable as a clojure deftype name.

  For example: org.apache.kafka.streams.kstream.ValueJoiner -> ValueJoiner"
  [s]
  (-> (str s)
      (str/split #"\.")
      (last)
      (->PascalCaseSymbol)))

(defn ->class
  "Converts cls-symbol into a symbol representing the class in clojure.

  A plain java class is passed through as is:

      java.lang.Integer -> java.lang.Integer

  But an array is wrapped in a Class.forName:

      (java.lang.Class/forName \"[Ljava.lang.String;\")
  "
  [cls-symbol]
  (if (str/starts-with? (str cls-symbol) "[L")
    `(~'Class/forName ~(str (.getName cls-symbol)))
    (->type cls-symbol)))

(defn ->instantiate
  "Converts the symbol to a clojure instantiation call (like `(ClassName.)`)."
  [cls-symbol]
  (symbol (str (->type cls-symbol) ".")))

(defn ->invoke
  "Converts the symbol into a clojure java interop call (like `(.methodName)`)."
  [method-name]
  (symbol (str "." method-name)))


(ns diebrücke.gen.reflect
  "Helper methods to get information about Java classes using reflection. This
  is similar to clojure.reflect, but will also get parameter names from classes
  compiled with debug information, and identifies parameters that are lambda
  callbacks."
  (:import (java.lang.reflect Modifier))
  (:refer-clojure :exclude [methods]))

(declare ->params)

(defn- ->method
  "The reflected details of a java method as a clojure map."
  [java-class java-method constructor?]
  {:name            (symbol (.getName java-method))
   :return-type     (if-not constructor? (symbol (.getName (.getReturnType java-method))))
   :declaring-class (symbol (.getName java-class))
   :parameters      (->params java-method)
   :exception-types nil
   :constructor?    constructor?})

(defn- ->public-constructors
  "Gets the public methods of a Java class."
  [java-class]
  (->>
    (.getDeclaredConstructors java-class)
    (filter #(Modifier/isPublic (.getModifiers %)))
    (map #(->method java-class % :constructor))))

(defn- ->public-methods
  "Gets the public constructors of a java class."
  [java-class]
  (->>
    (.getDeclaredMethods java-class)
    (filter #(Modifier/isPublic (.getModifiers %)))
    (map #(->method java-class % nil))))

(defn- ->methods
  "Gets the public methods and constructors of a Java class."
  [java-class]
  (concat (->public-methods java-class)
          (->public-constructors java-class)))

(defn- ->params
  "Translates a java.lang.reflect.Parameter to a clojure map."
  [method]
  (for [p (.getParameters method)]
    (let [cls (.getType p)]
      {:name       (symbol (.getName p))
       :type       (symbol (.getName cls))
       :interface? (.isInterface cls)
       :lambda     (when (= 1 (count (.getDeclaredMethods cls))) (first (->public-methods cls)))
       :array?     (.isArray cls)})))

(defn- ->java-class
  "Coerces the input to the Java class it represents."
  [java-class]
  (if (symbol? java-class) (Class/forName (str java-class)) java-class))

(defn methods
  "Gets the public methods of the java class."
  [java-classes]
  (->>
    (map ->java-class java-classes)
    (map ->methods)
    (apply concat)))


(ns diebrücke.gen.methods
  "Generates clojure code wrapping Java methods.

  The general strategy is to wrap all of the overloads of a Java method with a
  clojure defmulti/defmethods that dispatch on the argument types.

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
  (:require [camel-snake-kebab.core :refer [->kebab-case-symbol ->snake_case_string ->PascalCaseSymbol]]
            [clojure.set :as set]
            [clojure.string :as str]
            [diebrücke.gen.lambda :as lambda]
            [diebrücke.gen.sym :as sym]))

(defn- has-lambda-value-with-key?
  "Checks whether a method has a parameter that is a lambda callback with a
  class name that has 'WithKey' in the name.

  This is one of the few things that needs to be special cased, because the
  Java code relies on static typing to resolve the right method overload,
  which isn't possible in clojure. For these 'WithKey' cases, the generated
  wrapping clojure function name has '-with-key' concatenated.

  An example is the KStream.flatMapValues overloads that accept a
  ValueMapperWithKey. These are mapped to a clojure function named
  flat-map-values-with-key, while the KStream.flatMapValues overloads that
  accept a ValueMapper are mapped to the clojure function flat-map."
  [{:keys [parameters]}]
  (->>
   (map (comp :declaring-class :lambda) parameters)
   (filter #(str/includes? (str %) "WithKey"))
   (first)))

(defn ->fn-name
  "Creates a name for a clojure fn wrapper from a Java method name."
  [{:keys [constructor? name] :as method}]
  (has-lambda-value-with-key? method)
  (if constructor?
    (->kebab-case-symbol (sym/->type name))
    (->kebab-case-symbol (if (has-lambda-value-with-key? method)
                           (str name "WithKey")
                           name))))

(defn dispatch-val
  "Translates a java parameter type into something suitable as a defmethod
  dispatch value."
  [{:keys [lambda type]}]
  (if lambda :fn (sym/->class type)))

(defn ->fn-dispatch-vals
  "Generate a multimethod dispatch value that is an array of the Java parameter
  types of the Java method."
  [{:keys [constructor? declaring-class parameters]}]
  (if constructor?
    (mapv dispatch-val parameters)
    (mapv dispatch-val (cons {:type declaring-class} parameters))))

(defn ->fn-params
  "Converts Java method parameters into a list of clojure fn arguments."
  [{:keys [constructor? declaring-class parameters]}]
  (let [params (mapv (comp sym/->param :name) parameters)]
    (if constructor?
      params
      `[~(sym/->param declaring-class) ~@params])))

(defn- ->fn-arg
  "Wraps an argument to a Java method.

  When the argument is a fn, it's wrapped in a reification of the corresponding
  Java lambda interface.

  Otherwise it is passed as-is to the Java method."
  [method {:keys [lambda name] :as parameter}]
  (if lambda
    (lambda/->reify method parameter)
    (sym/->param name)))

(defn ->fn-body
  "Generates the body of a function."
  [{:keys [constructor? name declaring-class parameters] :as method}]
  (if constructor?
    `(~(sym/->instantiate name) ~@(mapv #(->fn-arg method %) parameters))
    `(~(sym/->invoke name) ~@(mapv #(->fn-arg method %) (cons {:name declaring-class} parameters)))))

(defn ->defmethod
  "Generates a clojure defmethod that wraps the java method."
  [method]
  `(~'defmethod ~(->fn-name method) ~(->fn-dispatch-vals method)
      ~(->fn-params method)
      ~(->fn-body method)))

(defn- ->defmulti
  "Generates a clojure defmulti statement that will dispatch on the types of
  the arguments. See `dispatcher` for details the dispatch strategy."
  [methods]
  (for [fn-name (into #{} (map ->fn-name methods))]
    `(~'defmulti ~fn-name ~'dispatcher)))

(defn- ->defmultimethod
  "Generates one defmulti and a defmethod for each of the overloaded Java methods."
  [[_ methods]]
  `(~@(->defmulti methods)
    ~@(map ->defmethod methods)))

(defn generate
  "Generates clojure forms for the multimethods wrapping the given Java methods."
  [methods]
  (->> (set/index methods [:name])
       (mapcat ->defmultimethod)))

(defmulti ^:private render-method
 "Renders a generated clojure form (defn, defmulti, or defmethod) into text."
  first)

(defmethod render-method 'defn
  [_def]
  (pr-str _def))

(defmethod render-method 'defmulti
  [_def]
  (pr-str _def))

(defmethod render-method 'defmethod
  [[defmethod_ name dispatch-values args body]]
  (format "(%s %s %s\n  %s\n  %s)" defmethod_ name dispatch-values args body))

(defn render
  "Renders the generated method wrappers into text."
  [generated-forms]
  (str/join "\n\n" (map render-method generated-forms)))


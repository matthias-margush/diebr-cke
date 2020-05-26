(ns diebrücke.gen.imports
  "Code to generate the clojure forms for an `(:import ..)` clause, and to
  render that into text."
  (:require [clojure.string :as str]))

(defn- package-class
  "Splits the fully qualified java class into a package/class pair."
  [fully-qualified-class]
  (let [parts (str/split (str fully-qualified-class) #"\.")]
    [(symbol (str/join "." (butlast parts)))
     (symbol (last parts))]))

(defn- parameter-types
  "Gets the types of the parameters"
  [{:keys [parameters]}]
  (map :type parameters))

(defn generate
  "Scans methods, constructors, return types, exceptions and parameters for
  java classes that need to be imported in the generated clojure."
  [methods]
  (->>
   (concat
    (map :name (filter :constructor? methods))
    (map :return-type methods)
    (map :declaring-class methods)
    (map :exception-types methods)
    (mapcat parameter-types methods))
   (remove nil?)
   (remove #(= 'void %))
   (remove #(str/starts-with? (str %) "[L"))
   (cons 'org.apache.kafka.streams.KeyValue)
   (into #{})
   (map package-class)
   (group-by first)
   (map (fn [[package klasses]]
          `(~package ~@(map second klasses))))
   (sort-by first)))


(defn render
  "Render the generated import forms into a string."
  [generated-imports]
  (->> generated-imports
       (str/join "\n           ")
       (format "(:import %s)")))





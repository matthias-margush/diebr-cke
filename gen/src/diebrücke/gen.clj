(ns diebrücke.gen
  "Code to generate clojure wrappers for kafka classes."
  (:require [camel-snake-kebab.core :refer [->kebab-case-symbol ->snake_case_string ->PascalCaseSymbol]]
            [clojure.java.io :as io]
            [diebrücke.gen.reflect :as reflect]
            [diebrücke.gen.imports :as imports]
            [diebrücke.gen.methods :as methods]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn generate
  "Generates clojure forms for the kafka `klasses`."
  [klasses]
  (let [methods (reflect/methods klasses)]
    {:ns "diebrücke.streams"
     :imports (imports/generate methods)
     :fns (methods/generate methods)}))

(defn- ->namespace
  "Generate a namespace form."
  [{:keys [ns imports]}]
  (format "(ns %s\n  %s\n  %s\n  %s)"
            ns
            "(:require [diebrücke.dispatcher :refer [dispatcher]])"
            (imports/render imports)
            "(:refer-clojure :only [boolean defmethod defmulti let reify])"))

(defn render
  "Renders generated forms to text."
  [{:keys [fns] :as generated}]
  (->>
     [(->namespace generated)
      (methods/render fns)]
    (str/join "\n\n")))

(defn save
  "Saves the generated output."
  [{:keys [ns] :as generated} output-dir]
  (let [f (format "%s/%s.clj" output-dir (str/replace (->snake_case_string ns) #"\." "/"))]
    (io/make-parents f)
    (spit f (render generated))
    (io/copy (io/file "src/diebrücke/dispatcher.clj")
             (io/file (format "%s/diebrücke/dispatcher.clj" output-dir)))))


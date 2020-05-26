(ns diebrücke.main
  (:require [diebrücke.gen.config :as config]
            [diebrücke.gen :as gen]))

(defn -main
  "Main entry point."
  [& [output-dir]]
  (-> config/kafka-classes
      (gen/generate)
      (gen/save (or output-dir "gen"))))


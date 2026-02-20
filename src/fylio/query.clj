(ns fylio.query
  (:require
    [clojure.edn :as edn]
    [com.walmartlabs.lacinia.util :refer [inject-resolvers]]
    [com.walmartlabs.lacinia :refer [execute]]
    [com.walmartlabs.lacinia.schema :as schema]
    ))

(defn get-hero
  [context arguments value]
  (let [{:keys [episode]} arguments]
    (if (= episode :NEWHOPE)
      {:id         1000
       :name       "Luke"
       :homePlanet "Tatooine"
       :appearsIn  ["NEWHOPE" "EMPIRE" "JEDI"]}
      {:id         2000
       :name       "Lando Calrissian"
       :homePlanet "Socorro"
       :appearsIn  ["EMPIRE" "JEDI"]})))

(def star-wars-schema
  (-> "resources/schema.edn"
      slurp
      edn/read-string
      (inject-resolvers {:Query/hero  get-hero
                         :Query/droid (constantly {})})
      schema/compile))

(def query "{hero {id name}}")

(execute star-wars-schema query nil nil)

(def query2 "{hero(episode: NEWHOPE) {movies: appearsIn}}")
(execute star-wars-schema query2 nil nil)

{:id         1000
 :name       "Luke"
 :homePlanet "Tatooine"
 :appearsIn  ["NEWHOPE" "EMPIRE" "JEDI"]}
(ns fylio.system
  (:require [com.stuartsierra.component :as component]
            [fylio.schema :as schema]
            [fylio.server :as server]))

(defn new-system
  []
  (assoc (component/system-map)
    :server (component/using (server/map->Server {})
                             [:schema-provider])
    :schema-provider (schema/map->SchemaProvider {})))

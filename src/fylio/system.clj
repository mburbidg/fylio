(ns fylio.system
  (:require [com.stuartsierra.component :as component]
            [fylio.schema :as schema]
            [fylio.server :as server]
            [fylio.db :as db]))

(defn new-system
  []
  (assoc (component/system-map)
    :db (db/map->FylioDb {})
    :server (component/using (server/map->Server {})
                             [:schema-provider])
    :schema-provider (component/using
                       (schema/map->SchemaProvider {})
                       [:db])))

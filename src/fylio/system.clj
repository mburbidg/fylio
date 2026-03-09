(ns fylio.system
  (:require [com.stuartsierra.component :as component]
            [fylio.schema :as schema]
            [fylio.server :as server]
            [fylio.db :as db]))

(defn new-system
  ([]
   (new-system nil))
  ([opts]
   (let [{:keys [port]
          :or {port 8888}} opts]
     (assoc (component/system-map)
       :db (db/map->FylioDb {})
       :server (component/using (server/map->Server {:port port})
                                [:schema-provider])
       :schema-provider (component/using
                          (schema/map->SchemaProvider {})
                          [:db])))))

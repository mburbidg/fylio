(ns user
  (:require [com.stuartsierra.component :as component]
            [fylio.system :as system]
            [fylio.test-utils :refer [simplify]]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.java.browse :refer [browse-url]]
            [clojure.walk :as walk])
  (:import (clojure.lang IPersistentMap)))

(defonce system (system/new-system))

(defn q
  [query-string]
  (-> system
      :schema-provider
      :schema
      (lacinia/execute query-string nil nil)
      simplify))

(defn start
  []
  (alter-var-root #'system component/start-system)
  (browse-url "http://localhost:8888/ide")
  :started)

(defn stop
  []
  (alter-var-root #'system component/stop-system)
  :stopped)

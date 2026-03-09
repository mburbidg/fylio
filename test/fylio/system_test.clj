(ns fylio.system-test
  (:require [clojure.test :refer [deftest is]]
            [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia :as lacinia]
            [fylio.test-utils :refer [simplify]]
            [fylio.system :as system]))

(defn- test-system
  "Creates a new system suitable for testing, and ensures that
  the HTTP port won't conflict with a default running system."
  []
  (system/new-system {:port 8989}))

(defn- q
  "Extracts the compiled schema and executes a query."
  [system query variables]
  (-> system
      (get-in [:schema-provider :schema])
      (lacinia/execute query variables nil)
      simplify))

(deftest can-read-user
  (let [system (component/start-system (test-system))]
    (try
      (is (= {:data {:userById {:firstName "Michael"
                                :lastName  "Burbidge"
                                :email     "mburbidg@gmail.com"
                                }}}
             (q system
                "{ userById(id: 1237) { firstName lastName email }}"
                nil)))
      (finally
        (component/stop-system system)))))

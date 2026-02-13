(ns fylio.server
  (:gen-class)
  (:require
    [fylio.utils :as utils]
    [cheshire.core :as json]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as rrc]
    [reitit.coercion.malli :as malli]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.response :as resp]))

(def app
  (wrap-params
    (ring/ring-handler
      (ring/router
        [["/health"
          {:get (fn [_] (resp/response "OK\n"))}]

         ["/api"
          ["/users"
           {:get  {:parameters {:query [:map
                                        [:id string?]]}
                   :handler    (fn [_] (utils/json-response {:message "Get User"}))}
            :post {:handler (fn [_] (utils/json-response {:message "Create User"}))}}]

          ["/hello"
           {:get (fn [_] (utils/json-response {:message "Hello, World!"}))}]

          ["/hello/:name"
           {:get (fn [req]
                   (let [name (get-in req [:path-params :name])]
                     (utils/json-response {:message (str "Hello, " name "!")})))}]

          ["/echo"
           {:post (fn [req]
                    (let [body (utils/read-json-body req)]
                      (if body
                        (utils/json-response {:you_sent body})
                        (utils/json-response {:error "Expected JSON body"} 400))))}]]]
        {:data {:coercion   malli/coercion
                :middleware [parameters/parameters-middleware
                             rrc/coerce-request-middleware
                             rrc/coerce-response-middleware]}})
      (ring/create-default-handler))
    )
  )

(defn -main [& _args]
  (println "REST server on http://localhost:3000")
  (jetty/run-jetty app {:port 3000 :join? true}))
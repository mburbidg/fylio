(ns fylio.server
  (:gen-class)
  (:require
    [cheshire.core :as json]
    [reitit.ring :as ring]
    [ring.adapter.jetty :as jetty]
    [ring.util.response :as resp]))

(defn json-response
  ([data] (json-response data 200))
  ([data status]
   (-> (resp/response (json/generate-string data))
       (resp/status status)
       (resp/header "content-type" "application/json; charset=utf-8"))))

(defn read-json-body [req]
  ;; Reads JSON body into a Clojure map (or nil if no body)
  (when-let [body (:body req)]
    (let [s (slurp body)]
      (when (seq s)
        (json/parse-string s true)))))

(def app
  (ring/ring-handler
    (ring/router
      [["/health"
        {:get (fn [_] (resp/response "OK\n"))}]

       ["/api"
        ["/hello"
         {:get (fn [_] (json-response {:message "Hello, World!"}))}]

        ["/hello/:name"
         {:get (fn [req]
                 (let [name (get-in req [:path-params :name])]
                   (json-response {:message (str "Hello, " name "!")})))}]

        ["/echo"
         {:post (fn [req]
                  (let [body (read-json-body req)]
                    (if body
                      (json-response {:you_sent body})
                      (json-response {:error "Expected JSON body"} 400))))}]]])
    (ring/create-default-handler)))

(defn -main [& _args]
  (println "REST server on http://localhost:3000")
  (jetty/run-jetty app {:port 3000 :join? true}))
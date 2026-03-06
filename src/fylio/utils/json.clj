(ns fylio.json
  (:require
    [cheshire.core :as json]
    [ring.util.response :as resp]))

(defn read-json-body [req]
  (when-let [body (:body req)]
    (let [s (slurp body)]
      (when (seq s)
        (json/parse-string s true)))))

(defn json-response
  ([data] (json-response data 200))
  ([data status]
   (-> (resp/response (json/generate-string data))
       (resp/status status)
       (resp/header "content-type" "application/json; charset=utf-8"))))

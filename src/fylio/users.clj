(ns fylio.users
  (:require
    [fylio.utils as fylio.utils]))

(def users {})

;(defn create-user-handler
;  [req]
;  (let [body (read-json-body req)]
;    (if body
;      (let [id (:id body)
;            name (:name body)]
;      (utils/json-response {:error "Expected JSON body"} 400)
;      )
;    ))

(defn get-user-handler
  [req]
  (:id users)
  )

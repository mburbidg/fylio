(ns fylio.regsys
  (:require
    [clojure.edn :as edn]
    [com.walmartlabs.lacinia.util :refer [inject-resolvers]]
    [com.walmartlabs.lacinia :refer [execute]]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.walmartlabs.lacinia.parser.schema :as parser]
    ))

(def users {})

(defn get-users
  [context arguments value]
  (into [] users)
  )

(defn get-user
  [context arguments value]
  (let [id (:id arguments)]
    (id users))
  )

(defn create-user
  [context arguments value]
  (let [id (.toString (random-uuid))]
    (let [user {:id id
                :firstName (:firstName arguments)
                :lastName (:lastName arguments)
                :dateOfBirth (:dateOfBirth arguments)
                :email (:email arguments)
                :password (:password arguments)}]
      (print user)
      (def users (assoc users id user))
      id)
    )
  )

(defn debug-create
  [context arguments value]
  (.toString (random-uuid))
  )

(def regsys-schema
  (-> "resources/datamodel.sdl"
      slurp
      parser/parse-schema
      (inject-resolvers {:Query/users  get-users
                         :Query/user get-user
                         :Mutation/createUser create-user})
      schema/compile))

(execute regsys-schema "{users {firstName lastName}}" nil nil)

(execute regsys-schema
  "mutation{createUser(firstName: \"Mike\" lastName: \"Burbidge\" dateOfBirth: 10 email: \"mburbidg@gmail.com\" password: \"snapple\")}" nil nil)

(print users)
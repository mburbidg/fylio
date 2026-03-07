(ns fylio.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [fylio.db :as db]
            [clojure.edn :as edn]))

(defn user-by-id
  [db]
  (fn [_ args _]
    (db/find-user-by-id db (:id args))))

(defn course-by-id
  [db]
  (fn [_ args _]
    (db/find-course-by-id db (:id args))))

(defn user-courses
  [db]
  (fn [_ _ user]
    (db/list-courses-for-user db (:id user))))

(defn course-students
  [db]
  (fn [_ _ course]
    (db/list-users-for-course db (:id course))))

(defn resolver-map
  [component]
  (let [{:keys [db]} component]
    {:Query/userById   (user-by-id db)
     :Query/courseById (course-by-id db)
     :User/courses     (user-courses db)
     :Course/students  (course-students db)}))


(defn load-schema
  [component]
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/inject-resolvers (resolver-map component))
      schema/compile))

(defrecord SchemaProvider [schema]

  component/Lifecycle

  (start [this]
    (assoc this :schema (load-schema this)))

  (stop [this]
    (assoc this :schema nil)))

(ns fylio.schema
  "Contains custom resolvers and a function to provide the full schema."
  (:require [clojure.java.io :as io]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [clojure.edn :as edn]))

(defn resolve-user-by-id
  [users-map context args value]
  (let [{:keys [id]} args]
    (get users-map id)
    ))

(defn resolve-course-by-id
  [courses-map context args value]
  (let [{:keys [id]} args]
    (get courses-map id)
    ))

(defn resolve-user-courses
  [courses-map context args user]
  (->> user
       :courses
       (map courses-map)))

(defn resolve-course-students
  [users-map context args course]
  (let [{:keys [id]} course]
    (->> users-map
         vals
         (filter #(-> % :courses (contains? id))))))

(defn entity-map
  [data k]
  (reduce #(assoc %1 (:id %2) %2)
          {}
          (get data k)))

(defn resolver-map
  []
  (let [user-data (-> (io/resource "user-data.edn")
                     slurp
                     edn/read-string)
        users-map (entity-map user-data :users)
        courses-map (entity-map user-data :courses)]
    {:Query/userById (partial resolve-user-by-id users-map)
     :Query/courseById (partial resolve-course-by-id courses-map)
     :User/courses (partial resolve-user-courses courses-map)
     :Course/students (partial resolve-course-students users-map)}))
(defn load-schema
  []
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string
      (util/inject-resolvers (resolver-map))
      schema/compile))

(ns fylio.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [com.stuartsierra.component :as component])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(defn- pooled-data-source
  [host dbname user password port]
  (doto (ComboPooledDataSource.)
    (.setDriverClass "org.postgresql.Driver")
    (.setJdbcUrl (str "jdbc:postgresql://" host ":" port "/" dbname))
    (.setUser user)
    (.setPassword password)))

(defrecord FylioDb [^ComboPooledDataSource datasource]

  component/Lifecycle

  (start [this]
    (assoc this :datasource (pooled-data-source "localhost" "fyliodb" "fylio_role" "lacinia" 25432)))

  (stop [this]
    (.close datasource)
    (assoc this :datasource nil)))

(defn- remap-user
  [row-data]
  (set/rename-keys row-data {:user_id    :id
                             :first_name :firstName
                             :last_name  :lastName
                             :email      :email
                             :password   :password}))

(defn find-user-by-id
  [component user-id]
  (-> (jdbc/query component
                  ["select user_id, first_name, last_name, email, password
                    from users where user_id = ?" user-id])
      first
      remap-user))

;(defn find-user-by-id
;  [db user-id]
;  (->> db
;       :data
;       deref
;       :users
;       (filter #(= user-id (:id %)))
;       first))

(defn find-course-by-id
  [db course-id]
  (->> db
       :data
       deref
       :courses
       (filter #(= course-id (:id %)))
       first))

(defn list-courses-for-user
  [db user-id]
  (let [courses (:courses (find-user-by-id db user-id))]
    (->> db
         :data
         deref
         :courses
         (filter #(contains? courses (:id %))))))

(defn list-users-for-course
  [db course-id]
  (->> db
       :data
       deref
       :users
       (filter #(-> % :courses (contains? course-id)))))

(defn ^:private apply-user
  [users user-id user]
  (let [new-user (-> user
                     (update :courses set)
                     (assoc :id user-id))]
    (->> users
         (remove #(= user-id (:id %)))
         (cons new-user)
         vec)))
(defn upsert-user
  "Adds a new user, or changes the value of an existing user."
  [db user-id user]
  (-> db
      :data
      (swap! update :users apply-user user-id user)))

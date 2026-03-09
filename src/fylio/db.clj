(ns fylio.db
  (:require [clojure.set :as set]
            [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(defn- pooled-data-source
  [host dbname user password port]
  (doto (ComboPooledDataSource.)
    (.setDriverClass "org.postgresql.Driver")
    (.setJdbcUrl (str "jdbc:postgresql://" host ":" port "/" dbname))
    (.setUser user)
    (.setPassword password)
    (.setMinPoolSize 3)
    (.setInitialPoolSize 3)
    (.setMaxPoolSize 15)))

(defrecord FylioDb [^ComboPooledDataSource datasource]

  component/Lifecycle

  (start [this]
    (assoc this :datasource
                (pooled-data-source "localhost" "fyliodb" "fylio_role" "lacinia" 25432)))

  (stop [this]
    (when datasource
      (.close datasource))
    (assoc this :datasource nil)))

(def query-opts
  {:builder-fn rs/as-unqualified-lower-maps})

(defn- remap-user
  [row-data]
  (set/rename-keys row-data {:user_id    :id
                             :first_name :firstName
                             :last_name  :lastName}))

(defn- remap-course
  [row-data]
  (set/rename-keys row-data {:course_id :id}))

(defn find-user-by-id
  [db user-id]
  (some-> (jdbc/execute-one!
            (:datasource db)
            ["select user_id, first_name, last_name, email, password
             from users
             where user_id = ?"
             user-id]
            query-opts)
          remap-user))

(defn find-course-by-id
  [db course-id]
  (some-> (jdbc/execute-one!
            (:datasource db)
            ["select course_id, name, location
             from courses
             where course_id = ?"
             course-id]
            query-opts)
          remap-course))

(defn list-courses-for-user
  [db user-id]
  (mapv remap-course
        (jdbc/execute!
          (:datasource db)
          ["select c.course_id, c.name, c.location
           from courses c
           join registrations r on r.course_id = c.course_id
           where r.user_id = ?
           order by c.course_id"
           user-id]
          query-opts)))

(defn list-users-for-course
  [db course-id]
  (mapv remap-user
        (jdbc/execute!
          (:datasource db)
          ["select u.user_id, u.first_name, u.last_name, u.email, u.password
           from users u
           join registrations r on r.user_id = u.user_id
           where r.course_id = ?
           order by u.user_id"
           course-id]
          query-opts)))

(defn upsert-user
  "Adds a new user, or changes the value of an existing user."
  [db user-id user]
  (jdbc/with-transaction
    [tx (:datasource db)]
    (let [{:keys [firstName lastName email password courses]} user
          existing (jdbc/execute-one!
                     tx
                     ["select user_id
                       from users
                       where user_id = ?"
                      user-id]
                     query-opts)]
      (if existing
        (jdbc/execute!
          tx
          ["update users
            set first_name = ?, last_name = ?, email = ?, password = ?
            where user_id = ?"
           firstName lastName email password user-id])
        (jdbc/execute!
          tx
          ["insert into users
            (user_id, first_name, last_name, email, password)
            values (?, ?, ?, ?, ?)"
           user-id firstName lastName email password]))
      (jdbc/execute!
        tx
        ["delete from registrations where user_id = ?"
         user-id])
      (doseq [course-id courses]
        (jdbc/execute!
          tx
          ["insert into registrations
            (user_id, course_id)
            values (?, ?)"
           user-id course-id])))))
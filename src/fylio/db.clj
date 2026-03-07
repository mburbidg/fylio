(ns fylio.db
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]))

(defrecord FylioDb [data]

  component/Lifecycle

  (start [this]
    (assoc this :data (-> (io/resource "user-data.edn")
                          slurp
                          edn/read-string
                          atom)))

  (stop [this]
    (assoc this :data nil)))

(defn find-user-by-id
  [db user-id]
  (->> db
       :data
       deref
       :users
       (filter #(= user-id (:id %)))
       first))

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

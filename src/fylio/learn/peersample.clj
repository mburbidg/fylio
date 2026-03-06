(ns fylio.learn.peersample
  (:require
    [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/hello")

(d/create-database db-uri)
(d/delete-database db-uri)
(ns fylio.db.schema
  (:require
    [datomic.api :as d]))

(def ^:private db-uri "datomic:dev://localhost:4334/fylio-dev")

(defn ensure-db!
  "Creates the dev database if it doesn't exist yet (safe to call on startup)."
  []
  (try
    (d/create-database db-uri)
    (catch Exception _
      ;; db already exists (or already created concurrently) - ok for startup
      nil))
  )

(defn conn
  []
  (d/connect db-uri))

(def schema-tx
  [;; -------------------------
   ;; Users
   ;; -------------------------
   {:db/ident       :user/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/index       true
    :db/doc         "Stable external id for a user (UUID)."}

   {:db/ident       :user/first-name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :user/last-name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :user/date-of-birth
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one}

   {:db/ident       :user/email
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/index       true}

   {:db/ident       :user/password-hash
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Password hash (do not store plaintext password)."}

   ;; -------------------------
   ;; RoleNames enum (idents)
   ;; -------------------------
   {:db/ident :role.name/admin}
   {:db/ident :role.name/student}
   {:db/ident :role.name/instructor}

   ;; -------------------------
   ;; Roles (role assignments)
   ;; -------------------------
   {:db/ident       :role/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/index       true}

   {:db/ident       :role/name
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Ref to enum ident :role.name/*"}

   {:db/ident       :role/user
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Ref to the user entity that has this role."}

   {:db/ident       :role/user+name
    :db/valueType   :db.type/tuple
    :db/tupleAttrs  [:role/user :role/name]
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Uniqueness constraint: a user can only have a given role once."}

   ;; -------------------------
   ;; Courses
   ;; -------------------------
   {:db/ident       :course/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/index       true}

   {:db/ident       :course/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :course/description
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :course/class-size
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}

   ;; -------------------------
   ;; Registrations
   ;; -------------------------
   {:db/ident       :registration/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/index       true}

   {:db/ident       :registration/user
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Ref to user entity."}

   {:db/ident       :registration/course
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Ref to course entity."}

   {:db/ident       :registration/date-registered
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one}

   {:db/ident       :registration/user+course
    :db/valueType   :db.type/tuple
    :db/tupleAttrs  [:registration/user :registration/course]
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Uniqueness constraint: a user can register for a course only once."}

   ;; -------------------------
   ;; Instructors (assign user to course as instructor)
   ;; -------------------------
   {:db/ident       :instructor/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/index       true}

   {:db/ident       :instructor/user
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}

   {:db/ident       :instructor/course
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}

   {:db/ident       :instructor/user+course
    :db/valueType   :db.type/tuple
    :db/tupleAttrs  [:instructor/user :instructor/course]
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}])

(defn schema-installed?
  "Checks whether core schema attributes exist (used to avoid re-installing on startup)."
  [db]
  (boolean (d/entity db :user/id)))

(defn ensure-schema!
  "Transact schema once (safe to call on startup)."
  [conn]
  (let [db (d/db conn)]
    (when-not (schema-installed? db)
      (d/transact conn {:tx-data schema-tx})
      :installed)))

(def ^:private seed
  {:users
   [{:user/id            #uuid "11111111-1111-1111-1111-111111111111"
     :user/first-name    "Mike"
     :user/last-name     "Burbidge"
     :user/date-of-birth #inst "1980-01-01T00:00:00.000-00:00"
     :user/email         "mike@example.com"
     :user/password-hash "dev-only-hash"}

    {:user/id            #uuid "22222222-2222-2222-2222-222222222222"
     :user/first-name    "Karen"
     :user/last-name     "Burbidge"
     :user/date-of-birth #inst "1981-01-01T00:00:00.000-00:00"
     :user/email         "karen@example.com"
     :user/password-hash "dev-only-hash"}]

   :courses
   [{:course/id          #uuid "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
     :course/name        "Intro to GraphQL"
     :course/description "Build a GraphQL API with Clojure"
     :course/class-size  20}

    {:course/id          #uuid "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
     :course/name        "Datomic Fundamentals"
     :course/description "Schema, transactions, and querying"
     :course/class-size  25}]

   ;; role assignments: separate entities
   :roles
   [{:role/id        #uuid "33333333-3333-3333-3333-333333333333"
     :role/user      [:user/id #uuid "11111111-1111-1111-1111-111111111111"]
     :role/name      :role.name/admin
     :role/user+name [[:user/id #uuid "11111111-1111-1111-1111-111111111111"] :role.name/admin]}

    {:role/id        #uuid "44444444-4444-4444-4444-444444444444"
     :role/user      [:user/id #uuid "11111111-1111-1111-1111-111111111111"]
     :role/name      :role.name/instructor
     :role/user+name [[:user/id #uuid "11111111-1111-1111-1111-111111111111"] :role.name/instructor]}

    {:role/id        #uuid "55555555-5555-5555-5555-555555555555"
     :role/user      [:user/id #uuid "22222222-2222-2222-2222-222222222222"]
     :role/name      :role.name/student
     :role/user+name [[:user/id #uuid "22222222-2222-2222-2222-222222222222"] :role.name/student]}]

   :registrations
   [{:registration/id              #uuid "66666666-6666-6666-6666-666666666666"
     :registration/user            [:user/id #uuid "22222222-2222-2222-2222-222222222222"]
     :registration/course          [:course/id #uuid "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"]
     :registration/date-registered #inst "2026-01-01T00:00:00.000-00:00"
     :registration/user+course     [[:user/id #uuid "22222222-2222-2222-2222-222222222222"]
                                    [:course/id #uuid "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"]]}]

   :instructors
   [{:instructor/id          #uuid "77777777-7777-7777-7777-777777777777"
     :instructor/user        [:user/id #uuid "11111111-1111-1111-1111-111111111111"]
     :instructor/course      [:course/id #uuid "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"]
     :instructor/user+course [[:user/id #uuid "11111111-1111-1111-1111-111111111111"]
                              [:course/id #uuid "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"]]}]})

(defn seeded?
  "Heuristic: if at least one user exists, treat DB as seeded."
  [db]
  (boolean
    (ffirst
      (d/q '[:find ?e
             :where
             [?e :user/id _]]
           db))))

(defn seed!
  "Seed sample data once (safe to call on startup)."
  [conn]
  (let [db (d/db conn)]
    (when-not (seeded? db)
      (let [tx-data (vec (concat (:users seed)
                                 (:courses seed)
                                 (:roles seed)
                                 (:registrations seed)
                                 (:instructors seed)))]
        (d/transact conn {:tx-data tx-data})
        :seeded))))

(defn init!
  "Startup hook: ensure DB exists, schema installed, and dev seed data present."
  []
  (let [c (conn)]
    (ensure-schema! c)
    (seed! c)
    c))
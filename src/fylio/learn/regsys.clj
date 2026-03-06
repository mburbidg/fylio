(ns fylio.learn.regsys
  (:require
    [clojure.edn :as edn]
    [com.walmartlabs.lacinia.util :refer [inject-resolvers]]
    [com.walmartlabs.lacinia :refer [execute]]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.walmartlabs.lacinia.parser.schema :as parser]
    [datomic.api :as d]
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

(def cfg {:server-type :datomic-local
          :system "fylio"})

(def client (d/client cfg))

(d/create-database client {:db-name "inventory"})
(d/delete-database client {:db-name "inventory"})

(def conn (d/connect client {:db-name "inventory"}))

; Add a fact
[:db/add "foo" :db/ident :green]

(d/transact
  conn
  {:tx-data [{:db/ident :red}
             {:db/ident :green}
             {:db/ident :blue}
             {:db/ident :yellow}]})

(defn make-idents
  [x]
  (mapv #(hash-map :db/ident %) x))

(def sizes [:small :medium :large :xlarge])
(def types [:shirt :pants :dress :hat])
(def colors [:red :green :blue :yellow])

(d/transact conn {:tx-data (make-idents sizes)})
(d/transact conn {:tx-data (make-idents colors)})
(d/transact conn {:tx-data (make-idents types)})

(def schema-1
  [{:db/ident :inv/sku
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :inv/color
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :inv/size
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :inv/type
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}])
(d/transact conn {:tx-data schema-1})

;;; Defined earlier, but repeated for clarity
(def colors [:red :green :blue :yellow])
(def sizes [:small :medium :large :xlarge])
(def types [:shirt :pants :dress :hat])

(defn create-sample-data
  "Create a vector of maps of all permutations of args"
  [colors sizes types]
  (->> (for [color colors size sizes type types]
         {:inv/color color
          :inv/size size
          :inv/type type})
       (map-indexed
         (fn [idx map]
           (assoc map :inv/sku (str "SKU-" idx))))
       vec)) ;; 64 (4 x 4 x 4) maps

@(def sample-data (create-sample-data colors sizes types))

@(def sample-data-transaction (d/transact conn {:tx-data sample-data}))

(d/pull
  (d/db conn)
  [{:inv/color [:db/ident]}
   {:inv/size [:db/ident]}
   {:inv/type [:db/ident]}]
  [:inv/sku "SKU-42"])

@(def msg (d/pull
       (d/db conn)
       [{:inv/color [:db/ident]}
        {:inv/size [:db/ident]}
        {:inv/type [:db/ident]}]
       [:inv/sku "SKU-42"]))

msg

(seq msg)

(d/q
  '[:find ?sku ?size
    :where
    [?e :inv/sku "SKU-42"]
    [?e :inv/color ?color]
    [?e2 :inv/color ?color]
    [?e2 :inv/sku ?sku]
    [?e2 :inv/size ?size]]
  (d/db conn))

(def order-schema
  [{:db/ident :order/items
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true}
   {:db/ident :item/id
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :item/count
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}])

(d/transact conn {:tx-data order-schema})

(def add-order
  {:order/items
   [{:item/id [:inv/sku "SKU-25"]
     :item/count 10}
    {:item/id [:inv/sku "SKU-26"]
     :item/count 20}]})

(d/transact conn {:tx-data [add-order]})

(def db (d/db conn))


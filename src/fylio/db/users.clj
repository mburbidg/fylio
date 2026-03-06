(ns fylio.db.users
  (:require
    [datomic.api :as d]))

(def ^:private user-pull-pattern
  [:user/id
   :user/first-name
   :user/last-name
   :user/date-of-birth
   :user/email
   ;; Intentionally omit :user/password-hash from reads returned to API callers
   ])

(defn get-user-by-id
  "Fetch a user by external UUID (the value stored in :user/id).
  Accepts either a db value or a connection as the first argument.

  Returns nil if not found."
  ([db-or-conn user-id]
   (let [db (if (and (map? db-or-conn) (contains? db-or-conn :db/basisT))
              db-or-conn
              (d/db db-or-conn))
         user-uuid (cond
                     (uuid? user-id) user-id
                     (string? user-id) (java.util.UUID/fromString user-id)
                     :else (throw (ex-info "user-id must be a UUID or UUID string"
                                           {:user-id user-id})))
         e [:user/id user-uuid]
         u (d/pull db user-pull-pattern e)]
     (when (seq u)
       {:id          (str (:user/id u))
        :firstName   (:user/first-name u)
        :lastName    (:user/last-name u)
        :dateOfBirth (:user/date-of-birth u)
        :email       (:user/email u)}))))

(defn create-user!
  "Create a user entity.

  Input map keys:
    :firstName, :lastName, :dateOfBirth, :email, :passwordHash

  - :dateOfBirth should be a java.util.Date (an \"instant\") or an ISO-8601 string.
  - Returns the new user's external id (UUID)."
  [conn {:keys [firstName lastName dateOfBirth email passwordHash] :as _user}]
  (when-not (and firstName lastName dateOfBirth email passwordHash)
    (throw (ex-info "Missing required fields"
                    {:required [:firstName :lastName :dateOfBirth :email :passwordHash]})))
  (let [id (random-uuid)
        dob (cond
              (instance? java.util.Date dateOfBirth) dateOfBirth
              (string? dateOfBirth) (java.util.Date/from (java.time.Instant/parse dateOfBirth))
              :else (throw (ex-info ":dateOfBirth must be java.util.Date or ISO-8601 string"
                                    {:dateOfBirth dateOfBirth})))
        tx {:tx-data
            [{:user/id            id
              :user/first-name    firstName
              :user/last-name     lastName
              :user/date-of-birth dob
              :user/email         email
              :user/password-hash passwordHash}]}
        _ (d/transact conn tx)]
    id))
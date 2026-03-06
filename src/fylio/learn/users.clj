(ns fylio.learn.users)

(def user-data
  {:users
   [{:id        "1234"
     :firstName "Michael"
     :lastName  "Burbidge"
     :email     "mburbidg@gmail.com"
     :password  "snapple"}
    {:id        "1235"
     :firstName "Karen"
     :lastName  "Burbidge"
     :email     "karenburbidge@comcast.net"
     :password  "snoop"}
    {:id        "1236"
     :firstName "Tom"
     :lastName  "Taylor"
     :email     "tomtaylor@gmail.com"
     :password  "mechanic"}
    {:id        "1237"
     :firstName "Lonnie "
     :lastName  "Millett"
     :email     "lmillett@gmail.com"
     :password  "sailboat"}]})

(defn users-map
  [d]
  (->> d
       :users
       (reduce #(assoc %1 (:id %2) %2) {})))

(println (users-map user-data))
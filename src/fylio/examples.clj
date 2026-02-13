(ns fylio.examples)

(defn my-inc
  [n]
  (+ 1 n))

(map my-inc [1 2 3 4])

(defn titlesize
  [topic ]
  (str topic " for the brave and true"))

(map titlesize ["hampster" "ragnarok"])

(map titlesize '("Mike" "Kent"))

(map titlesize #{"Mike" "Kent"})

(map #(titlesize (second %)) {:mike "Mike" :kent "Kent"})

(seq {:kent "Kent" :mike "Mike"})

(into {} (seq {:kent "Kent" :mike "Mike"}))

(into [] (seq {:kent "Kent" :mike "Mike"}))

(into #{} (seq {:kent "Kent" :mike "Mike"}))

(map str ["a" "b" "c"] ["1" "2" "3"])

(def sum #(reduce + %))
(def avg #(/ (sum %) (count %)))
(defn stats [numbers]
  (map #(% numbers) [sum count avg]))

(stats [33 6 2])

(reduce + [1 2 3])

(assoc {:a 1 :b 2} :c 3)

(assoc {:a 1 :b 2} :a 10)

(reduce (fn [new-vector val]
          (if (> val 4)
            (conj new-vector val)
            new-vector))
        []
        [1 5 2 10]
        )

(count ["a" "bb" "c" "333"])
(count "aaa")

(sort-by :age [{:name "mike" :age 66} {:name "karen" :age 70}])

(def vampire-database
  {0 {:makes-blood-puns? false :has-pulse? true :name "McFishwich"}
   1 {:makes-blood-puns? false :has-pulse? true :name "McMackson"}
   2 {:makes-blood-puns? true :has-pulse? false :name "Damon Salvatore"}
   3 {:makes-blood-puns? true :has-pulse? true :name "Mickey Mouse"}})

(defn vampire-related-details [ssn]
  (Thread/sleep 1000)
  (get vampire-database ssn))

(defn vampire? [record]
  (and (:makes-blood-puns? record)
       (not (:has-pulse? record))
       record))

(defn identify-vampire [ssn]
  (first (filter vampire?
                 (map vampire-related-details ssn))))

((time (vampire-related-details 2)))

(time (def mapped-details (map vampire-related-details (range 0 100))))

(time (first mapped-details))

(time (identify-vampire (range 0 100)))

(map identity {:mike "mike" :karen "karen"})

(into {} (map identity {:mike "mike" :karen "karen"}))

(def add10 (partial + 10))

(add10 1)
(add10 1 4)

[:map
 [:id int?]]

{:id int?}
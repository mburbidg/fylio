(ns fylio.server
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]))

(defn handler [_request]
  {:status  200
   :headers {"content-type" "text/plain; charset=utf-8"}
   :body    "Hello, World!\n"})

(defn -main [& _args]
  (println "Starting server on http://localhost:3000")
  (jetty/run-jetty handler {:port 3000 :join? true}))
(ns jmxhttpgateway.server
  (:gen-class)
  (:require
    compojure
    compojure.http.request
    jmxhttpgateway.utils
  )
)

(def pool (jmxhttpgateway.utils/setup-pool '("localhost:2011")))

(defn pp-bean-attribute ""
  [conn bean-name attribute-name]
  (str attribute-name " : "
       (jmxhttpgateway.utils/get-bean-attribute conn bean-name attribute-name)))

(defn basic-get [request]
  {:status  200
   :headers {}
   :body    (pp-bean-attribute (pool (:JMXConn (:params request)))
                               (:JMXBean (:params request))
                               (:JMXAttribute (:params request)))})

(defn -main [& args]
  (compojure/run-server {:port 8080}
    "/*" (compojure/servlet (compojure.http.request/with-request-params basic-get))))

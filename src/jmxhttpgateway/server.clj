(ns jmxhttpgateway.server
  (:gen-class)
  (:require
    compojure
    compojure.http.request
    jmxhttpgateway.utils
  )
)

(def pool (ref {}))

(defn get-connection
  "Gets a jmx connection - opening it if necessary"
  [target]
  (if (nil? (@pool target))
      (dosync
        (alter pool assoc target (jmxhttpgateway.utils/connect-with-catch target))
        (@pool target))
      (@pool target)))

(defn get-bean-attribute-with-retry
  "Gets an attribute value - reconnecting if necessary"
  [conn bean-name attribute attempts]
  (try
   (jmxhttpgateway.utils/get-bean-attribute conn bean-name attribute)
   (catch Exception _ nil)))

(defn pp-bean-attribute ""
  [conn bean-name attribute-name]
  (let [val (get-bean-attribute-with-retry conn bean-name attribute-name 5)]
       (if (nil? val)
           nil
           (str attribute-name " : " val))))

(defn basic-get [request]
  {:status  200
   :headers {}
   :body    (pp-bean-attribute (get-connection (:JMXConn (:params request)))
                               (:JMXBean (:params request))
                               (:JMXAttribute (:params request)))})

(defn -main [& args]
  (compojure/run-server {:port 8080}
    "/*" (compojure/servlet (compojure.http.request/with-request-params basic-get))))

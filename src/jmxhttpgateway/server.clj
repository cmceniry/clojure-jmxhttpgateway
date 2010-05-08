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

; This seems like a great place for lazy evaluation
; but not sure on how to approach that
; so using the borrowing old recur
(defn get-bean-attribute-with-retry
  "Gets an attribute value - reconnecting if necessary"
  [target bean-name attribute attempts]
  (if (= attempts 0)
      nil
      (let [conn (get-connection target)
	    val (jmxhttpgateway.utils/get-bean-attribute-with-catch conn
								    bean-name
								    attribute)]
	   (if (nil? val)
	       (recur target bean-name attribute (- attempts 1))
	       val))))

(defn pp-bean-attribute ""
  [target bean-name attribute-name]
  (let [val (get-bean-attribute-with-retry target bean-name attribute-name 5)]
       (if (nil? val)
           nil
           (str attribute-name " : " val))))

(defn basic-get [request]
  {:status  200
   :headers {}
   :body    (pp-bean-attribute (:JMXConn (:params request))
                               (:JMXBean (:params request))
                               (:JMXAttribute (:params request)))})

(defn -main [& args]
  (compojure/run-server {:port 8080}
    "/*" (compojure/servlet (compojure.http.request/with-request-params basic-get))))

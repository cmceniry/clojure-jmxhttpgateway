(ns jmxhttpgateway.server
  (:require
    [ring.adapter.jetty :as ring]
    [ring.middleware.params]
    [jmxhttpgateway.utils :as utils]
  )
)

(def pool (ref {}))

(defn get-connection
  "Gets a jmx connection - opening it if necessary"
  [target]
  (if (nil? (@pool target))
      (dosync
        (alter pool assoc target (utils/connect-with-catch target))
        (@pool target))
      (@pool target)))

(defn remove-connection
  "Removes a connection from the pool"
  [target]
  (dosync
   (alter pool dissoc target)))

; This seems like a great place for lazy evaluation
; but not sure on how to approach that
; so using the boring old recur
(defn get-bean-attribute-with-retry
  "Gets an attribute value - reconnecting if necessary"
  [target bean-name attribute attempts]
  (if (= attempts 0)
      nil
      (let [conn (get-connection target)
	    val (utils/get-bean-attribute-with-catch conn
								    bean-name
								    attribute)]
	   (if (nil? val)
	       (do
		(remove-connection target)
		(recur target bean-name attribute (- attempts 1)))
	       val))))

(defn pp-bean-attribute ""
  [target bean-name attribute-name]
  (let [val (get-bean-attribute-with-retry target bean-name attribute-name 5)]
       (if (nil? val)
           nil
           (str attribute-name " : " val))))

(defn basic-get
  "Handles the GET method. Expects JMXConn, JMXBean, and JMXAttribtue to be parameters"
  [request]
  {:status  200
   :headers {}
   :body    (pp-bean-attribute
     ((:params request) "JMXConn")
     ((:params request) "JMXBean")
     ((:params request) "JMXAttribute")
   )
  }
)

(defn handler
  "General request dispatcher"
  [request]
  (case (:request-method request)
    :get (basic-get request)
    {:status 501 :headers {"Content-Type" "text/plain"} :body "Not implemented\n"}
  )
)

(def app
  (-> handler
    (ring.middleware.params/wrap-params)
  )
)

(defn -main [& args]
  (let [server-properties (utils/get-server-properties args)
        port (utils/get-listenerport server-properties)]
    (ring/run-jetty app {:port port})
  )
)

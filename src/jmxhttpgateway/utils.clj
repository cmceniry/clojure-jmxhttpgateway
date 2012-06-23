
(ns jmxhttpgateway.utils
  (:gen-class)
  (:require
    [clojure.java.jmx :as jmx]
  )
  (:import (javax.management.remote JMXConnectorFactory JMXServiceURL))
)


(defn get-bean-attribute
  "Get back a value"
  [connection bean attribute]
  (binding [jmx/*connection* connection]
	   (jmx/read bean attribute)))

(defn get-bean-attribute-with-catch
  "Get back a value, nil if there's an exception"
  [connection bean attribute]
  (try
   (get-bean-attribute connection bean attribute)
   (catch Exception _ nil)))

(defn get-bean-attributes
  "Get all attribute values back"
  [connection bean]
  (binding [jmx/*connection* connection]
	   (jmx/mbean bean)))

(defn get-bean-attributes-with-catch
  "Get all attribute values back, {} if there's an exception"
  [connection bean]
  (try
   (get-bean-attributes connection bean)
   (catch Exception _ {})))

(defn bare-connect
  "Helper function that does the actual connect."
  [serviceurl]
  (let [conn (.getMBeanServerConnection (JMXConnectorFactory/connect serviceurl))]
    conn))

(defn connect
  "Connect to a JMX listener.  Abort after 10 seconds."
  [hostport]
  (let [serviceurl (JMXServiceURL.
                    (str "service:jmx:rmi:///jndi/rmi://" hostport "/jmxrmi"))
        f (future (bare-connect serviceurl))]
    (try
     (let [val (.get f 10000 java.util.concurrent.TimeUnit/MILLISECONDS)]
       val)
     (catch java.util.concurrent.TimeoutException
            _
            (do
             (try (.cancel f true) (catch Exception _ nil))
             nil)))))

(defn connect-with-catch
  "Connect to a JMX listener but pass back nil if unable to connect"
  [hostport]
  (try
   (connect hostport)
   (catch Exception _ nil)))

(defn setup-pool
  "Sets up a pool to connect with"
  [sources]
  (loop [s sources pool {}]
    (if (empty? s)
        pool
	(recur
	  (rest s)
	  (assoc pool
		 (first s)
		 (connect-with-catch (first s)))))))

; test code

;(setup-pool '("localhost:2011", "192.168.181.190:2011"))

;(def service-uri-s "service:jmx:rmi:///jndi/rmi://192.168.181.190:2011/jmxrmi")
;(def service-uri (JMXServiceURL. service-uri-s))
;(def connfactory (JMXConnectorFactory/connect service-uri))
;(def connection (.getMBeanServerConnection connfactory))

;(println (get-bean-attribute connection "org.apache.activemq:BrokerName=localhost,Type=Queue,Destination=example.A" :MemoryLimit))
;(loop []
;      (do
;	  (println (get-bean-attributes connection "org.apache.activemq:BrokerName=localhost,Type=Queue,Destination=example.A"))
;	  (. Thread (sleep 5000))
;	(recur)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Command/config line processing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get-default-server-properties
  "Set the default server properties"
  []
  {"listenerport" 8080})

(defn merge-server-properties
  "Merge the server properties of two hashes"
  [orig newbies]
  (if (empty? newbies)
      orig
      (let [pair (first newbies)
            propname (first pair)
            propval (second pair)]
       (recur
        (assoc orig propname propval)
        (dissoc newbies propname)))))

(defn get-server-properties-from-file
  "Load properties from a file"
  [filename]
  (let [contents (slurp filename)
 	buf (new java.io.StringReader contents)
	properties (new java.util.Properties)]
    (.load properties buf)
    (into {} 
     (map (fn [prop]
           {prop
            (.get properties (str "jmxhttpgateway." prop))}
          ) ["listenerport"]))))
      
(defn get-server-properties
  "Pull off the command line arguments and get the server properties"
  [args]
  (let [defaults (get-default-server-properties)]
    (if (nil? args)
      	defaults
      	(merge-server-properties defaults
       	 (get-server-properties-from-file (first args))))))

(defn get-listenerport
  "Get the port from the arguments"
  [properties]
  (let [propval (properties "listenerport")]
  (if (= java.lang.Long (type propval))
      propval
      (Long/parseLong propval))))


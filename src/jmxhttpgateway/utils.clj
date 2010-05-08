
(ns jmxhttpgateway.utils
  (:gen-class)
  (:require clojure.contrib.jmx)
  (:import (javax.management.remote JMXConnectorFactory JMXServiceURL))
)

(alias 'jmx 'clojure.contrib.jmx)

(defn get-bean-attribute
  "Get back a value"
  [connection bean attribute]
  (binding [jmx/*connection* connection]
	   (jmx/read bean attribute)))

(defn get-bean-attribute-with-catch
  "Get back a value, nil if there's an exception"
  [connection bean attribute]
  (try
   (get-bean-attribute conn bean attribute)
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

(defn connect
  "Connect to a JMX listener"
  [hostport]
  (.getMBeanServerConnection
   (JMXConnectorFactory/connect
    (JMXServiceURL. 
     (str "service:jmx:rmi:///jndi/rmi://" hostport "/jmxrmi")))))

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

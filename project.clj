(defproject jmxhttpgateway "0.1"
  :description "A simple http server that gateways jmx calls"
  :dependencies [
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/java.jmx "0.1"]
                 [ring/ring-core "1.1.0"],
                 [ring/ring-jetty-adapter "1.1.0"]
                ]
  ;:main jmxhttpgateway/server)
)

(defproject jmxhttpgateway "0.0.1"
  :description "A simple http server that gateways jmx calls"
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.0-SNAPSHOT"]
                 [compojure "0.3.2"]]
  :dev-dependencies [[leiningen-init-script "0.1.0"]]
  :lis-opts {:install-dir "/usr/lib/jmxhttpgateway"}
  :namespaces [jmxhttpgateway.server
               jmxhttpgateway.utils]
  :main jmxhttpgateway/server)

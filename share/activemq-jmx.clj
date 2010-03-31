;
; ActiveMQ JMX
;   http://activemq.apache.org/jmx.html
;
; Clojure command line/getting started
;   http://clojure.org/getting_started
;
;
; ActiveMQ has to be started with
;   SUNJMX="-Dcom.sun.management.jmxremote=true        \
;   -Dcom.sun.management.jmxremote.port=2011           \
;   -Dcom.sun.management.jmxremote.authenticate=false  \
;   -Dcom.sun.management.jmxremote.ssl=false"
;
; Need the right jmx stuff from contrib
;   http://richhickey.github.com/clojure-contrib/jmx-api.html
;
; Build the contrib under ant
;   ant -Dclojure.jar /Users/mac/projects/clojure/clojure-1.1.0/clojure.jar
;
; Copy contrib directories into place
;   ./clojure/contrib...
;
; Start clojure:
;   java -cp jline-0_9_5.jar:clojure-1.1.0/clojure.jar:clojure-contrib.jar:. \
;   jline.ConsoleRunner clojure.main

(require '[clojure.contrib.jmx :as jmx])

(jmx/with-connection {:host "localhost", :port 2011}
 (jmx/read "org.apache.activemq:BrokerName=localhost,Type=Broker" :TotalEnqueueCount))

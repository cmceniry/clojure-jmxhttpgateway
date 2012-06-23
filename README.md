JMX/HTTP Gateway in Clojure
===========================

This is just a simple standalone JMX->HTTP gateway, and some misc tools.

Example
-------

Start up a java process with JMX enabled without any security:

   -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=4270 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

Then start up the jmxhttpgateway.server:

   lein run -m jmxhttpgateway.server

And you should be able to hit up information through the gateway:

   $ curl 'http://localhost:8080?JMXConn=localhost:9999&JMXBean=java.lang:type=Memory&JMXAttribute=NonHeapMemoryUsage'
   NonHeapMemoryUsage : {:committed 24317952, :init 24317952, :max 136314880, :used 10280144}


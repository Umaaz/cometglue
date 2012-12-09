Comet Glue
==========

Introduction
------------

CometGlue is a (very) simple project to bring up CometD in an OSGi environemnt, and to expose the Bayeux server as a DS service.

Prerequisites
-------------

### Bundle Prerequisites ###

The following information pertains to [**Apache Karaf**](http://karaf.apache.org/).  This is an OSGi server which is delivered with all necessary bundles for running webapps.

*Mandatory*

The CometGlue bundle has the following mandatory bundle pre-requisite bundles from the [CometD](http://download.cometd.org/) project:

* cometd-java-common-2.5.0.jar
* cometd-java-client-2.5.0.jar
* cometd-java-server-2.5.0.jar
* bayeux-api-2.5.0.jar

The following [Felix](http://felix.apache.org/site/downloads.cgi) mandatory dependencies are required to have the Comet Glue component scanned.

* org.apache.felix.scr-1.6.0.jar
    

*Optional* 

The following dependency from the CometD distribution is optional, but must be supplied if a websocket transport is required:

* cometd-websocket-jetty-2.5.0.jar

In all cases, newer versions of these bundles up to the next major version should be okay.  If anyone has experience of later versions, please let me know what they are and I'll update this page.

### Service Prerequisites ###

*Mandatory*

* A standard OSGi HTTP Service.
 * I've tested this with Apache Karaf, which is supplied with a full Jetty suite, including Websocket and Continuations.  Putting together your own set of bundles to make this work is theoretically possible but would probably be difficult.  If anyone comes up with a set of bundles which can be used with Felix (for example), please let me know.

*Optional*

* A standard OSGi Log Service
 * If this is supplied, limited logging will be performed.

Building
--------

The Comet Glue project requires Maven Central, as well as the [osgi-parent](https://github.com/john-hawksley/osgi-parent) POM to be installed in the local repo.

Configuration
-------------

Configuration of the bundle is performed using Java environmental ("-D") options.  The [CometD documentation](http://docs.cometd.org/reference/#java_server) lists options for the CometD servlet, and these can be used by prepending "bayeux." to them, and supplying them as options.  For instance, to enable the Websocket transport in the server, the documentation specifies the following servlet option:

    transports=org.cometd.websocket.server.WebSocketTransport
  
This can be passed to Comet Glue by starting the OSGi engine with following define:

    -Dbayeux.transports=org.cometd.websocket.server.WebSocketTransport
    
Apache Karaf (and possibly Felix too) can be passed this with the `JAVA_OPTS` env var:

    export JAVA_OPTS="-Dbayeux.transports=org.cometd.websocket.server.WebSocketTransport"

Usage
-----

* Place the prerequisite bundles in your OSGi engine's `deploy` folder, or arrange to have them deployed via OBR or other mechanism.
* Place the Comet Glue bundle in the same place.
* Ensure any `JAVA_OPTS` reqiured to configure CometD are set.
* When the Comet Glue bundle activates, the CometD servlet is registered with the OSGi HTTP Service under the path `/cometd`
* The servlet implementation, a `BayeuxServerImpl` is then registered with the OSGi service registry under the class name `org.cometd.bayeux.server.BayeuxServer`. 

Your client bundles can then express a dependency on this type using SCR annotations, manual DS XML entries or by pulling the service themselves out of the service registry via the bundle context.

You can then register CometD services etc. using this reference.

* When the Comet Glue bundle is deactivated, the servlet is stopped and removed.
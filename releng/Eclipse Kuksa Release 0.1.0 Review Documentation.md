<!---
The purposes of a Release Review are: to summarize the **accomplishments of the release**, to verify that the IP Policy has been followed and all approvals have been received, to highlight any **remaining quality and/or architectural issues**, and to verify that the project is continuing to operate according to the Principles and Purposes of Eclipse.

**CHECKLIST** https://wiki.eclipse.org/Development_Resources/HOWTO/Release_Reviews
--->

# Eclipse Kuksa Release 0.1.0

The first Eclipse Kuksa 0.1.0 release contains various software across the domains in-vehicle, cloud, apps, and IDE outlined in the following.

## In-Vehicle

In-Vehicle projects run on a target device such as the Raspberry Pi.

* *agl-kuksa* - Scripts to automate the AGL build system with the meta-kuksa layers.
* *kuksa-hawkbit* - Barebone API for connecting to Eclipse Hawkbit. The kuksa-appmanager (see below) should though be the prior choice to interact with Eclipse HawkBit.
* *w3c-visserver-api* - W3C Vehicle Information Specification API.
* *elm327-visdatafeeder* - ELM 327 app that reads OBDII data from the vehicle and feeds data to the w3c-visserver.
* *direct-access-api* - Enables sending CAN messages from the cloud to a vehicle using web sockets.
* *kuksa-appmanager* - Hawkbit appmanager which deploys in-vehicle apps as docker containers and more.
* *app-ids* - A proof-of-concept implementation of a modular Intrusion Detection System (IDS), whose architecture enables a distributed deployment of modules.
* *FOTA-raspberrypi* - The FOTA scripts are used by the kuksa-appmanager to flash firmware images to the Raspberry Pi.
* *netIDS* - A network intrusion detection implementation that scans a CAN bus and tries to detect anomalies.

* *datalogger-http* - Example app that sends data from the vehicle to an Eclipse Hono instance with http.
* *datalogger-mqtt* - Example app that sends data from the vehicle to an Eclipse Hono instance with mqtt.
* *remoteAccess* - Example app that subscribes to control topic with Hono and receives commands sent.
* *email-notifier* - Example app that talks to an email-server and sends e-mails to the configured email address. Used at the moment only for internal demos.

#### Open In-Vehicle Issues

https://github.com/eclipse/kuksa.invehicle/issues (#=11; tag to come here)

## Cloud

The Eclipse Kuksa Cloud forms the counterpart to the in-Vehicle projects. For the Eclipse Kuksa 0.1.0 release, deployment scripts and an Appstore are provided.

* *Deployment* scripts to setup an Eclipse Kuksa Kubernetes cloud cluster (https://github.com/eclipse/kuksa.cloud/tree/master/deployment)
* *Appstore* The Eclipse Kuksa Appstore's initial version provides rudimental functionalities to provide communication between a target device, the cloud, and target owners (users) as well as administrative persons via a Web-GUI (https://github.com/eclipse/kuksa.cloud/tree/master/kuksa-appstore)

* *Malfunction Indicator Light* This example service sends telemetry data to the Kuksa cloud backend in order to check for a possible malfunction of the car. On the occurrence of a malfunction, the driver gets notified by email including the next workshop-garage to get the car repaired.
* *HonoInfluxDBConnector* A Spring-Boot application that connects to an Eclipse Hono instance with a running InfluxDB database so that messages received by Hono can be stored in the InfluxDB. This is especially useful to easily create a visualization of some measurements eg with Grafana.

Eclipse Kuksa Cloud software will further be provided as binary artifacts at the [Eclipse Kuksa Download Page](https://projects.eclipse.org/projects/iot.kuksa/downloads).

#### Open Cloud Issues

https://github.com/eclipse/kuksa.cloud/issues (#=19 tag to come )

## Apps

Eclipse Kuksa application projects are subdivided into cloud and in-vehicle applications. In-Vehicle (docker) apps can be published to the Eclipse Kuksa App-Store and Eclipse HawkBit using the *app-publisher* project (https://github.com/eclipse/kuksa.apps/tree/master/kuksa-app-publisher).
For the Eclipse Kuksa 0.1.0 release, the following in-vehicle applications are available:

* *kuksa-cloud-dashboard* - Connects to the w3c-visserver service via Websocket. Reads the RPM, Speed, Fuel status and also custom DTC from the w3c-visserver to sends this information as telemetry data to the Hono MQTT adapter.
* *kuksa-cloud-mechanic* - Connects to the w3c-visserver service via Websocket and communicates with the Eclipse Hono MQTT adapter. Consequently, commands can be sent to a vehicle remotely. 
* *kuksa-traccar-client* - Sends location (GPS) data to the Traccar GPS tracking suite (https://www.traccar.org).

#### Open App Issues

https://github.com/eclipse/kuksa.apps/issues (#=10, tag to come)

## IDE

This project contains documentation and implementation to setup an Eclipse Che Kuksa instance, which eases the development of Kuksa In-Vehicle applications as well as Kuksa Cloud services. With the Eclipse Kuksa 0.1.0 release, only Che_v6.10 is supported (https://github.com/eclipse/kuksa.ide). 
Future releases will contain Eclipse Theia extensions, which are compliant to Eclipse Che 7 and support Kuksa in-vehicle application build processes, application registrations and transmissions to the Eclipse Kuksa Appstore and the Eclipse Kuksa Cloud.

#### Open IDE issues

https://github.com/eclipse/kuksa.ide/issues (#=3, tag to come)

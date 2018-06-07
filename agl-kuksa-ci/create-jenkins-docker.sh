#!/bin/bash

MIRRORDIR=/srv/mirror
SHARED_STATES=/srv/sstate

docker run -it -d -p 8080:8080 \
  -p 50000:50000 \
  -v /var/jenkins_home:/var/jenkins_home \
  --name=jenkins-docker \
  agl-kuksa-jenkins-docker:0.0.1


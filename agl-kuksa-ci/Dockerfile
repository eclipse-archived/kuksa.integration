# ******************************************************************************
# Copyright (c) 2018 Dortmund University of Applied Sciences and Arts
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
#  Contributors:
#      Pedro Cuadra - initial build system and tooling
# *****************************************************************************

FROM jenkins/jenkins:lts

USER root

RUN apt-get update && apt-get install -y cmake curl chrpath build-essential texinfo wget python3

RUN wget -O git-lfs.tar.gz https://github.com/git-lfs/git-lfs/releases/download/v2.3.4/git-lfs-linux-amd64-2.3.4.tar.gz

RUN mkdir git-lfs
RUN tar -xvf git-lfs.tar.gz -C git-lfs --strip-components=1

WORKDIR git-lfs

RUN ./install.sh

USER jenkins

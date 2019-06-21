/*********************************************************************
 * Copyright (c) 2019 Bosch Software Innovations GmbH [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

pipeline {
    agent any
    stages {
        stage ("Test") {
            steps {
                git 'https://github.com/eclipse/kuksa.integration.git'
                sh """cd testing && \
                export JAVA_HOME=/opt/tools/java/openjdk/latest && \
                /opt/tools/apache-maven/latest/bin/mvn --batch-mode test -Dhono_device_registry=${hono_device_registry}  \
                -Dhono_dispatch_router=${hono_dispatch_router}  \
                -Dhono_adapter_http_vertx=${hono_adapter_http_vertx} \
                -Dhono_adapter_mqtt_vertx=${hono_adapter_mqtt_vertx} \
                -Dhawkbit_address=${hawkbit_address} \
                -Dhawkbit_username=${hawkbit_username} \
                -Dhawkbit_password=${hawkbit_password} \
                -Dappstore_address=${appstore_address} \
                -Dappstore_username=${appstore_username} \
                -Dappstore_password=${appstore_password} \
                """
            }
        }
    }
    post {
        failure {
            mail to: 'kuksa-dev@eclipse.org',
                subject: "Failed Jenkins Pipeline: ${currentBuild.fullDisplayName}",
                body: "Something is wrong with ${env.BUILD_URL}"
        }
    }
}

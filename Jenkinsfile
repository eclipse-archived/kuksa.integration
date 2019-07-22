pipeline {
    agent any
    stages {
        stage ("Test") {
            parallel {
                stage ("Run Suite") {
                    steps {
                	git 'https://github.com/eclipse/kuksa.integration.git'
                        sh """cd testing && \
                        mvn clean test -Dhono_device_registry=${hono_device_registry}  \
                        -Dhono_dispatch_router=${hono_dispatch_router} \
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
                stage ("Run Hono-Consumer") {
                    steps {
                        sh """cd testing && \
			export JAVA_HOME=/opt/tools/java/openjdk/latest && \
                        java -jar hono-cli-1.0-M3-exec.jar \
                        """
                    }
                }
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
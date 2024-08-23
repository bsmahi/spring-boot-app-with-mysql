pipeline {
    agent any

    environment {
        // Define any environment variables if needed
        MAVEN_HOME = tool name: 'Maven 3.9.8', type: 'maven'
        DOCKER_IMAGE = "bsmahi/spring-boot-app-with-mysql"
        DOCKER_TAG = "latest"
        DOCKER_CREDENTIALS_ID = "DockerHubCredentials"
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from GitHub
                git url: 'https://github.com/bsmahi/spring-boot-app-with-mysql.git', branch: 'master'
            }
        }

        stage('Build') {
            steps {
                // Run Maven build
                sh "${MAVEN_HOME}/bin/mvn clean install"
            }
        }

        stage('Test') {
            steps {
                // Run tests
                sh "${MAVEN_HOME}/bin/mvn test"
            }
        }

        stage('Build Docker Image') {
             steps {
                script {
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
             }
        }

        stage('Push to Docker Hub') {
              steps {
                script {
                    docker.withRegistry('', "${env.DOCKER_CREDENTIALS_ID}") {
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                    }
                }
              }
         }

         stage('Docker Scout Analysis') {
             steps {
                 script {
                     def image = "${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                     sh "docker scout cves ${image}"
                 }
             }
         }


    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
        success {
            echo 'Build and Test were successful!'
        }
        failure {
            echo 'Build or Test failed.'
        }
    }
}

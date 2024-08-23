pipeline {
    agent any

    environment {
        // Define any environment variables if needed
        MAVEN_HOME = tool name: 'Maven 3.9.8', type: 'maven'
        DOCKER_IMAGE = "bsmahi/spring-boot-app-with-mysql"
        DOCKER_TAG = "latest"
        DOCKER_CREDENTIALS_ID = credentials('DockerHubCredentials')
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

        stage('Install Docker') {
            steps {
                script {
                    sh '''
                    if ! command -v docker &> /dev/null
                    then
                        apt-get update
                        apt-get install -y docker.io
                    fi
                    '''
                }
            }
        }

        stage('Build Docker Image') {
             steps {
                sh """
                 docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} .
                """
             }
        }

        stage('Push to Docker Hub') {
              steps {
                script {
                    withCredentials([usernamePassword(credentialsId: "${env.DOCKER_CREDENTIALS_ID}", passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh """
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                        docker push ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                        """
                   }
                }
              }
         }

         stage('Docker Scout Analysis') {
             steps {
                 script {
                      sh """
                       docker scout cves ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}
                      """
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

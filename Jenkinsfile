pipeline {
    agent any

    environment {
        // Define any environment variables if needed
        MAVEN_HOME = tool name: 'Maven 3.9.8', type: 'maven'
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

        // stage('Deploy') {
        //     steps {
        //         // Deploy to an environment (e.g., staging or production)
        //         // You can customize this step based on your deployment strategy
        //         sh "${MAVEN_HOME}/bin/mvn deploy"
        //     }
        // }
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

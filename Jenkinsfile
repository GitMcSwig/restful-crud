pipeline {
    agent { docker 'mongodb:3.4.2' }
    stages {
        stage('build') {
            steps {
                sh 'mongod --dbpath .'
                sh './gradlew clean build'
            }
        }
    }
}

pipeline {
    agent { docker 'mongo' }
    stages {
        stage('build') {
            steps {
                sh 'mongod --dbpath .'
                sh './gradlew clean build'
            }
        }
    }
}

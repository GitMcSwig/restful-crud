pipeline {
    agent { docker 'ubuntu' }
    stages {
        stage('build') {
            steps {
                sh 'mongod --dbpath .'
                sh './gradlew clean build'
            }
        }
    }
}

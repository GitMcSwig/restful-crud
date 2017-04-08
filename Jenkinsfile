pipeline {
    agent { docker 'ubuntu' }
    stages {
        stage('build') {
            steps {
                sh './gradlew clean build'
            }
        }
    }
}

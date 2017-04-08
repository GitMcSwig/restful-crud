pipeline {
    agent { docker 'gradle:3.4.1' }
    stages {
        stage('build') {
            steps {
                sh 'gradle clean'
            }
        }
    }
}

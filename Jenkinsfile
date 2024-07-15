pipeline {
  agent any
  stages {
    stage('build project') {
      steps {
        sh './gradlew build -x test'
      }
    }
    stage('test project') {
      steps {
        script {
            try {
                sh './gradlew test'
            } finally {
                junit 'build/test-results/test/*.xml'
            }
        }
      }
    }
    stage('build docker image') {
        steps {
            sh './gradlew --no-build-cache :dockerPush'
        }
    }
  }

  tools {
    jdk 'zulu-21'
  }
}

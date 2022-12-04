pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean kotlin:compile install'
      }
    }

    stage('Achieve') {
      steps {
        archiveArtifacts 'eladmin-system/target/*.jar'
      }
    }

  }
}
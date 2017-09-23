pipeline {
  agent any
  stages {
    stage('test') {
      steps {
        sh 'curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/bin/lein'
      }
    }
  }
}
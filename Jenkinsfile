pipeline {
 agent any
 environment {
 // Setup Maven home
  PATH = "/usr/local/Cellar/maven/3.6.3_1/libexec/bin:$PATH"
 }

 stages {

  // Ignore this stage on failure.
  stage('Code Quality') {
   steps {
    catchError {
     script {
      sh "mvn clean verify sonar:sonar"
     }
    }
    echo currentBuild.result
   }
  }

  stage('Test') {
   steps {
     script {
      sh "mvn test"
     }
   }
  }

  stage('Build') {
   steps {
    script {
     sh "mvn clean package"
    }
   }
  }

  stage('Store to GCS') {
   steps {
    script {
     config = readYaml file: 'config.yml'
     dir("target") {
      step([$class: 'ClassicUploadStep',
       credentialsId: "${config.project}",
       bucket: "gs://${config.bucket}/${config.environment}",
       pattern: "${config.pattern}"
      ])
     }
    }
   }
  }
 }
}
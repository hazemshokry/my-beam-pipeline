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
      sh "mvn verify sonar:sonar -Dmaven.test.skip=true"
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
     sh "mvn clean package -Dmaven.test.skip=true"
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
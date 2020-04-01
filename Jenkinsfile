pipeline {
  agent any
  environment {
      PATH = "/usr/local/Cellar/maven/3.6.3_1/libexec/bin:$PATH"
  }

   stages {
        stage('Code Quality') {
                           steps {
                               script {
                               {
                                  sh "mvn clean verify sonar:sonar"
                                  }
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
            steps{
            script{
            config = readYaml file: 'config.yml'
            dir("target")
                            {
                            step([$class: 'ClassicUploadStep',
                              credentialsId: "${config.project}",
                              bucket: "gs://${config.bucket}/${config.environment}",
                              pattern: "${config.pattern}"])
                              }
            }
            }
         }
      }
   }
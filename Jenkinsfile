pipeline {
  agent any
//   {
//         docker {
//             image 'gcr.io/cloud-builders/mvn'
//         }
//   }
  environment {
      PATH = "/usr/local/Cellar/maven/3.6.3_1/libexec/bin:$PATH"
      CREDENTIALS_ID ='myspringml2'
      BUCKET = 'dataflow-cicd'
      PATTERN = '*bundled*.jar'
  }

   stages {
      stage('Build') {
         steps {
            // Get some code from a GitHub repository
            git 'https://github.com/hazemshokry/my-beam-pipeline'

            // Run Maven on a Unix agent.
            sh "mvn clean package"
         }
      }
      stage('Store to GCS') {
            steps{
                sh '''
                    env > build_environment.txt
                '''

                dir("target"){step([$class: 'ClassicUploadStep', credentialsId: env
                        .CREDENTIALS_ID,  bucket: "gs://${env.BUCKET}/dev",
                      pattern: env.PATTERN])
}
            }
         }
      }
   }

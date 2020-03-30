// node {
//   config = readYaml file: 'config.yml'
// }

pipeline {
  agent any
//   {
//         docker {
//             image 'gcr.io/cloud-builders/mvn'
//         }
//   }
  environment {
      PATH = "/usr/local/Cellar/maven/3.6.3_1/libexec/bin:$PATH"
  }

   stages {
      stage('Build') {
         steps {
         script
         {
         config = readYaml file: 'config.yml'
         git "${config.repo}"
         sh "mvn clean package"
         }
            // Get some code from a GitHub repository



            // Run Maven on a Unix agent.

         }
      }
      stage('Store to GCS') {
            steps{
            script{
            config = readYaml file: 'config.yml'
            dir("target")
                            {
                            step([$class: 'ClassicUploadStep',
                              credentialsId: ${config.project},
                              bucket: "gs://${config.bucket}/${config.environment}",
                              pattern: ${config.pattern}])
                              }
            }
            }
         }
      }
   }
node {
  config = readYaml file: 'config.yaml'
}

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
            // Get some code from a GitHub repository
            git "${config.repo}"

            // Run Maven on a Unix agent.
            sh "mvn clean package"
         }
      }
      stage('Store to GCS') {
            steps{
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
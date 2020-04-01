pipeline {

 agent any
 environment {
 // Setup Maven home
  PATH = "/usr/local/Cellar/maven/3.6.3_1/libexec/bin:$PATH"
 }

 stages {

  // Ignore this stage on failure.
//   stage('Code Quality') {
//    steps {
//     catchError {
//      script {
//       sh "mvn sonar:sonar -Dmaven.test.skip=true"
//      }
//     }
//     echo currentBuild.result
//    }
//   }
//
//   stage('Test') {
//    steps {
//      script {
//       sh "mvn test"
//      }
//    }
//   }

  stage('Build') {
   steps {
    script {
     sh "mvn clean package -Dmaven.test.skip=true"
    }
   }
  }

  stage('Store artifact to GCS') {
   steps {
    script {
     config = readYaml file: 'config.yml'
     dir("target") {
      step([$class: 'ClassicUploadStep',
       credentialsId: 'myspringml2',
       bucket: "gs://${config.bucket}/${config.environment}/artifacts/${config.version}",
       pattern: '*bundled*.jar'
      ])
     }
    }
   }
  }

 stage('Deploy to Google Dataflow approval'){
  steps {
    script{
     config = readYaml file: 'config.yml'
     input """
     You are about to deploy ${config.jobtype} job \"${config.jobname}-${config.version}.${BUILD_NUMBER}\"
     to ${config.environment}. Note that update batch job is not yes supported, confirm?"
     """
    }
   }
  }


 stage('Deploy to prod'){
    steps {
     script{
     config = readYaml file: 'config.yml'
     def stagingLocation = "gs://${config.environment}/staging/${config.version}"
     def templateLocation = "gs://${config.environment}/templates/${config.version}/${config.jobname}-${config.version}.${BUILD_NUMBER}"
     sh """mvn compile exec:java \
              -Dexec.mainClass=com.springml.pipelines.StarterPipeline \
              -Dexec.args=\"--runner=DataflowRunner \
                           --project=${config.gcpProject} \
                           --stagingLocation=${stagingLocation} \
                           --templateLocation=${templateLocation}\""""
     sh "terraform init"
            dir("Terraform/prod") {
                sh """
                terraform plan -var job_name=${config.jobname} \
                 -var template_gcs_path=${templateLocation} \
                 -var template_gcs_path=gs://${config.environment}/tmp/${config.version}
                """
                input "Are you sure to apply these plan towards your Google account?"
                sh """
                terraform apply -var job_name=${config.jobname} \
                 -var template_gcs_path=${templateLocation} \
                 -var template_gcs_path=gs://${config.environment}/tmp/${config.version}
                """
                }
    }
    }
  }
 }
}

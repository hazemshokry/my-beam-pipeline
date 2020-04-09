pipeline {

 agent any
 environment {
 // Define Terraform home
  def tfHome = tool name: "Terraform"

 // Setup Maven home
  PATH = "/usr/local/Cellar/maven/3.6.3_1/libexec/bin:${tfHome}:$PATH"
 }

 stages {

//   Ignore this stage on failure.
  stage('Code Quality') {
   steps {
    catchError {
     script {
      sh "mvn sonar:sonar -Dmaven.test.skip=true"
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

//   stage('Build') {
//    steps {
//     script {
//      sh "mvn clean package -Dmaven.test.skip=true"
//     }
//    }
//   }
//
//   stage('Store artifact to GCS') {
//    steps {
//     script {
//      config = readYaml file: 'config.yml'
//      dir("target") {
//       step([$class: 'ClassicUploadStep',
//        credentialsId: 'myspringml2',
//        bucket: "gs://${config.bucket}/${config.environment}/${config.jobname}/${config.version}/artifacts",
//        pattern: '*bundled*.jar'
//       ])
//      }
//     }
//    }
//   }

  stage('Build template to GCS') {
   steps {
        script{
        config = readYaml file: 'config.yml'
        def stagingLocation = "gs://${config.bucket}/${config.environment}/${config.jobname}/version-${config.version}/staging"
        def templateLocation = "gs://${config.bucket}/${config.environment}/${config.jobname}/version-${config.version}/templates/${config.jobname}-${config.version}.${BUILD_NUMBER}"
        def temp_gcs_location = "gs://${config.bucket}/${config.environment}/${config.jobname}/version-${config.version}/temp"
        def gcsFilePath = "gs://dataflow-cicd/data/input/*"
        sh """mvn compile exec:java \
              -Dexec.mainClass=com.springml.pipelines.StarterPipeline \
              -Dexec.args=\"--runner=DataflowRunner \
              --project=${config.gcpProject} \
              --stagingLocation=${stagingLocation} \
              --templateLocation=${templateLocation} \
              --tempLocation=${temp_gcs_location} \
              --GCSFilePath=${gcsFilePath}\""""
    }
   }
  }

 stage('Deploy to Google Dataflow approval'){
  when {
        branch 'master'
      }
  steps {
    script{
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
     def stagingLocation = "gs://${config.bucket}/${config.environment}/${config.jobname}/version-${config.version}/staging"
     def templateLocation = "gs://${config.bucket}/${config.environment}/${config.jobname}/version-${config.version}/templates/${config.jobname}-${config.version}.${BUILD_NUMBER}"
     def temp_gcs_location = "gs://${config.bucket}/${config.environment}/${config.jobname}/version-${config.version}/temp"
     def gcsFilePath = "gs://dataflow-cicd/data/input/*"
     dir("Terraform/prod") {
         sh "terraform init"
         sh """
            terraform plan -destroy -var job_name=${config.jobname} \
            -var template_gcs_path=${templateLocation} \
            -var temp_gcs_location=${temp_gcs_location} \
            -var gcpProject=${config.gcpProject}
            """
            input "Are you sure to apply this plan towards your GCP account?"
            sh "terraform destroy -auto-approve -target google_dataflow_job.big_data_job"
         sh """
            terraform apply -auto-approve -var job_name=${config.jobname} \
            -var template_gcs_path=${templateLocation} \
            -var temp_gcs_location=${temp_gcs_location} \
            -var gcpProject=${config.gcpProject}
            """
     }
    }
   }
  }
 }
}

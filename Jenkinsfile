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
     def stagingLocation = "gs://${config.bucket}/${config.environment}/staging/${config.version}"
     def templateLocation = "gs://${config.bucket}/${config.environment}/templates/${config.version}/${config.jobname}-${config.version}.${BUILD_NUMBER}"
     def temp_gcs_location = "gs://${config.bucket}/${config.environment}/tmp/${config.version}"
     sh """mvn compile exec:java \
              -Dexec.mainClass=com.springml.pipelines.StarterPipeline \
              -Dexec.args=\"--runner=DataflowRunner \
                           --project=${config.gcpProject} \
                           --stagingLocation=${stagingLocation} \
                           --templateLocation=${templateLocation} \
                           --tempLocation=${temp_gcs_location}\""""
            dir("Terraform/prod") {
                sh "terraform init"
                sh """
                terraform plan -var job_name=${config.jobname} \
                 -var template_gcs_path=${templateLocation} \
                 -var temp_gcs_location=${temp_gcs_location}
                """
                input "Are you sure to apply these plan towards your Google account?"
                sh """
                terraform apply -auto-approve -var job_name=${config.jobname} \
                 -var template_gcs_path=${templateLocation} \
                 -var temp_gcs_location=${temp_gcs_location}
                """
                }
    }
    }
  }
 }
}

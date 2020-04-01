node {
 config = readYaml file: 'config.yml'
 }
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

  stage('Store to GCS') {
   steps {
    script {
     dir("target") {
      step([$class: 'ClassicUploadStep',
       credentialsId: 'myspringml2',
       bucket: "gs://${config.bucket}/${config.environment}",
       pattern: '*bundled*.jar'
      ])
     }
    }
   }
  }

 stage('Deploy to Google Dataflow approval'){
  steps {
    script{
     input "You're about to deploy ${config.Jobtype} job \"${config.jobname}-${config.version}.${build.number}\" to ${config.environment}. Note that update batch job is not yes supported, confirm?"
    }
   }
  }

 stage('deploy to prod'){
    steps {
        dir("target") {
        sh 'java -jar my-beam-pipeline-bundled-${config.version}.${build.number}.jar \
              --runner=DataflowRunner \
              --project=${config.gcpProject} \
              --tempLocation="gs://${config.bucket}/temp/" \
              --Job_Name=${config.jobname}'
        }

    }
  }
 }
}

/* groovylint-disable CompileStatic, LineLength */
pipeline {
  agent 'any'

    environment {
        DOCKERHUB_CREDENTIALS=credentials('docker_hub_creds')
    }

  tools {
    jdk 'java8'
    maven 'maven'
  }

  parameters {
    choice(
      name: 'Branch',
      choices: ['master', 'deploy', 'GaganKalra'],
      description: 'Choose Branch'
    )
    choice(
      name: 'Environment',
      choices: ['dev','prod'],
      description: 'Choose Environment'
    )
    string(
        name: 'Release_Image_Tag',
        defaultValue: 'latest',
        description: 'Provide image tag to deploy on Prod (e.g. - 1.0.0, 2.0.0, 3.0.0 etc.)'
    )
  }

  stages {

        stage('Setup MySQL for Dev'){
            when {
                expression { params.Environment == 'dev' }
            }
            stages {
                stage('Initialize Terraform') {
                    steps {
                        sh "cd terraform_code/terraform.dev && terraform init"
                    }
                }
                stage('Lint Terraform') {
                    steps {
                        sh "cd terraform_code/terraform.dev && terraform fmt"
                    }
                }
                stage('Plan Terraform') {
                    steps {
                        sh "cd terraform_code/terraform.dev && terraform plan -out mytfplan"
                    }
                }
                stage('Execute Terraform Code') {
                    steps {
                        sh "cd terraform_code/terraform.dev && terraform apply -auto-approve mytfplan"
                    }
                }
                stage('Save the IP') {
                          steps {
                              sh "cd terraform_code/terraform.dev && terraform output Instance_IP > instance_ip_dev"
                              archiveArtifacts "terraform_code/terraform.dev/instance_ip_dev"
                          }
                }
              }
          }
        stage('Setup MySQL for Prod'){  
              when {
                  expression { params.Environment == 'prod' }
              }
              stages {
                  stage('Initialize Terraform') {
                      steps {
                          sh "cd terraform_code/terraform.prod && terraform init"
                      }
                  }
                  stage('Formatting') {
                      steps {
                          sh "cd terraform_code/terraform.prod && terraform fmt"
                      }
                  }
                  stage('Plan Terraform') {
                      steps {
                          sh "cd terraform_code/terraform.prod && terraform plan -out mytfplan"
                      }
                  }
                  stage('Execute Terraform') {
                      steps {
                          sh "cd terraform_code/terraform.prod && terraform apply -auto-approve mytfplan"
                      }
                  }
                  stage('Archive IP') {
                      steps {
                          sh "cd terraform_code/terraform.prod && terraform output Instance_IP > instance_ip_prod"
                          archiveArtifacts "terraform_code/terraform.prod/instance_ip_prod"
                      }
                  }
                }
            }

        stage('Execute Ansible Playbook on Dev') {
          when {
              expression { params.Environment == 'dev' }
          }
          steps {
              script{
                  sleep 20
                  sh 'sed -i \'s/"//g\' terraform_code/terraform.dev/instance_ip_dev && hostip_dev=$(cat terraform_code/terraform.dev/instance_ip_dev) && cd ansible && ansible-playbook -i hosts mysqlsetup.yml --extra-vars "ansible_host=${hostip_dev}"'

                }
            }
        }

        stage('Execute Ansible Playbook on Prod') {
          when {
              expression { params.Environment == 'prod' }
          }
          steps {
              script{
                  sleep 20
                  sh 'sed -i \'s/"//g\' terraform_code/terraform.prod/instance_ip_prod && hostip_prod=$(cat terraform_code/terraform.prod/instance_ip_prod) && cd ansible && ansible-playbook -i hosts mysqlsetup.yml --extra-vars "ansible_host=${hostip_prod}"'

              }
          }
        }

        stage('CI for Dev'){
            when {
              expression { params.Environment == 'dev' }
          }
          stages{

              stage('Intializing the project') {
                steps {
                  echo 'Building Spring3Hibernate Application'
                }
              }
              stage('Checking Code Stability') {
                steps {
                  dir('.') {
                    sh 'mvn validate'
                    echo 'Checking Code Stability completed'
                  }
                }
              }
              stage('Code Quality') {
                  parallel {
                      stage ('PMD') {
                          steps {
                              dir('.') {
                                  sh 'mvn pmd:pmd'
                              }
                          }
                      }
                      stage ('Bug Scan') {
                          steps {
                              dir('.') {
                                  sh 'mvn findbugs:findbugs'
                              }
                          }
                      }
                  }
              }
              stage ('Run Unit Tests') {
                steps {
                  dir('.') {
                    sh 'mvn test'
                  }
                }
              }

              stage ('Code Coverage Analysis') {
                steps {
                  dir('.') {
                    sh 'mvn cobertura:cobertura'
                  }
                }
              }

              stage ('Test Report') {
                steps {
                  dir('.') {
                    sh 'mvn surefire-report:report'
                  }
                }
              }

              stage('Security Checks - OWASP') {
                steps {
                  sh 'mvn dependency-check:check'
                  sh 'mkdir -p target/dependency-check-report && cp target/dependency-check-report.html target/dependency-check-report/'
                  echo 'Security Checks completed'
                }
              }

              stage('Build Package') {
                steps {
                  dir('.') {
                    sh 'mvn package'
                    echo 'Build completed'
                  }
                }
              }

              stage('Login') {
                steps {
                  sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                  echo 'Login Successful'
                }
              }

              stage('Build Docker Image') {
                steps {
                  dir('.') {
                    sh 'docker build -t team4/spring_image -f Dockerfile.deploy.dev .'
                    echo 'Build completed'
                  }
                }
              }

              stage('Tag Docker Image') {
                steps {
                  dir('.') {
                    sh 'docker image tag team4/spring_image:latest igagankalra/team4-spring-devimage:latest'
                    echo 'Image Tagged'
                  }
                }
              }

              stage('Push Docker Image') {
                steps {
                  dir('.') {
                    sh 'docker push igagankalra/team4-spring-devimage:latest'
                    sh 'docker rmi igagankalra/team4-spring-devimage:latest'
                    echo 'Image pushed to Docker Hub'
                  }
                }
              }
          }
      }

        stage('CI for Prod'){
            when {
                expression { params.Environment == 'prod' }
            }
            stages{
                stage('Intializing the project') {
                  steps {
                    echo 'Building Spring3Hibernate Application'
                  }
                }
                stage('MVN Clean') {
                  steps {
                    dir('.') {
                      sh 'mvn clean'
                      echo 'MVN Clean completed'
                    }
                  }
                }
                stage('Checking Code Stability') {
                  steps {
                    dir('.') {
                      sh 'mvn validate'
                      echo 'Checking Code Stability completed'
                    }
                  }
                }
                stage('Code Quality') {
                    parallel {
                        stage ('PMD') {
                            steps {
                                dir('.') {
                                    sh 'mvn pmd:pmd'
                                }
                            }
                        }
                        stage ('Bug Scan') {
                            steps {
                                dir('.') {
                                    sh 'mvn findbugs:findbugs'
                                }
                            }
                        }
                    }
                }
                stage('Security Checks - OWASP') {
                  steps {
                    dir('.') {
                      sh 'mvn dependency-check:check'
                      sh 'mkdir -p target/dependency-check-report && cp target/dependency-check-report.html target/dependency-check-report/'
                      echo 'Security Checks completed'
                    }
                  }
                }
                stage ('Run Unit Tests') {
                  steps {
                    dir('.') {
                      sh 'mvn test'
                    }
                  }
                }

                stage ('Code Coverage Analysis') {
                  steps {
                    dir('.') {
                      sh 'mvn cobertura:cobertura'
                    }
                  }
                }
                stage ('Test Report') {
                  steps {
                    dir('.') {
                      sh 'mvn surefire-report:report'
                    }
                  }
                }
                stage('Build Package') {
                  steps {
                    dir('.') {
                      sh 'mvn package'
                      echo 'Build completed'
                    }
                  }
                }

                stage('Login') {
                  steps {
                    sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                    echo 'Login Successful'
                  }
                }

                stage('Build Docker Image') {
                  steps {
                    dir('.') {
                      sh 'docker build -t team4/spring_image -f Dockerfile.deploy.prod .'
                      echo 'Build completed'
                    }
                  }
                }

                stage('Tag Docker Image') {
                  steps {
                    dir('.') {
                      sh 'docker image tag team4/spring_image igagankalra/prod-spring:${Release_Image_Tag}'
                      echo 'Image Tagged'
                    }
                  }
                }

                stage('Push Docker Image') {
                  steps {
                    dir('.') {
                      sh 'docker push igagankalra/prod-spring:${Release_Image_Tag}'
                      sh 'docker rmi igagankalra/prod-spring:${Release_Image_Tag}'
                      echo 'Image pushed to Docker Hub'
                    }
                  }
                }
            }

        }

        stage('Deployment On Dev') {
            when {
                expression { params.Environment == 'dev' }
            }
            stages {
                stage('Deploy App (Recreate)') {
                    steps {
                        sh "cd K8/dev && /usr/local/bin/kubectl apply -f spring-dev.yml"
                    }
                }
                stage('Apply Load Balancer') {
                    steps {
                        sh "cd K8/dev && /usr/local/bin/kubectl apply -f spring-dev-service-LB.yml"
                    }
                }
                    stage('Application IP') {
                    steps {
                      script{
                          sh '''
                           IP=$(tmpvar=$(/usr/local/bin/kubectl describe service spring-dev | grep -A3 "LoadBalancer Ingress:" | awk "{print \$3}" | tr "\\n" ":"| awk '{print $1":"$4}' FS=':' | tr "/\\TCP" " "); echo ${tmpvar%/*});
                           echo "Spring3Hibernet Application can be accessible on Development Environment at ${IP}"
                           '''
                      }

                    }
                    }
            }
        }

        stage('Deployment On Prod') {
                    when {
                        expression { params.Environment == 'prod' }
                    }
                    stages {
                        stage('Add Image Tag') {
                            steps {
                                sh "cd K8/prod && sed -i s/\\<image_tag\\>/${Release_Image_Tag}/ spring-prod.yml"
                            }
                        }
                        stage('Deploy App (Rolling)') {
                            steps {
                                sh "cd K8/prod && /usr/local/bin/kubectl apply -f spring-prod.yml"
                            }
                        }
                        stage('Apply Load Balancer') {
                            steps {
                                sh "cd K8/prod && /usr/local/bin/kubectl apply -f spring-prod-service-LB.yml"
                            }
                        }
                        stage('Application IP') {
                            steps {
                              script{
                                  sh '''
                                  IP=$(tmpvar=$(/usr/local/bin/kubectl describe service spring-dev | grep -A3 "LoadBalancer Ingress:" | awk "{print \$3}" | tr "\\n" ":"| awk '{print $1":"$4}' FS=':' | tr "/\\TCP" " "); echo ${tmpvar%/*});
                                  echo "Spring3Hibernet Application can be accessible on Production Environment at ${IP}"
                                  '''
                              }

                            }
                        }
                    }
        }
  }

  post {
    always {
      step([$class: 'CoberturaPublisher', autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: '**/coverage.xml', failUnhealthy: false, failUnstable: false, maxNumberOfBuilds: 0, onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false])
      junit testResults: '**/target/surefire-reports/TEST-*.xml'
      recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc()]
      recordIssues enabledForFailure: true, tool: findBugs()
      recordIssues enabledForFailure: true, tool: pmdParser(pattern: '**/target/pmd.xml ')
      publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'target/dependency-check-report', reportFiles: 'dependency-check-report.html', reportName: 'OWASP Dependency-Check Report', reportTitles: 'OWASP Dependency-Check Report'])
    }
  }

}



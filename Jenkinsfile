pipeline {
    agent any 

    environment {
        DOCKER_IMAGE = "ustapi/fleet-fms:latest"
    }

    stages {
        stage("Checkout Code") {
            steps {
                git url: 'https://github.com/srmono/docker_spring_pipeline.git', branch: 'master'
            }
        }
        stage("Build Maven Project") {
            steps {
                sh 'mvn clean package -DskipTests' 
            }
        }
        stage("Build Docker Image") {
            steps {
                sh 'docker build -t $DOCKER_IMAGE .' 
            }
        }
        stage("Login into to docker hub") {
            steps {
                withDockerRegistry([credentialsId: 'docker-hub-credentials', url:'https://index.docker.io/v1']) {
                    sh 'echo "Logged into docker hub successfully"'
                }
            }
        }
        stage("Push to docker hub") {
            steps {
                withDockerRegistry([credentialsId: 'docker-hub-credentials', url:'https://index.docker.io/v1']) {
                    sh 'docker push $DOCKER_IMAGE'
                }
            }
        }
    }
}
Scenario where your Spring Boot application is trying to connect to a MySQL container that may take time to start and initialize, you can implement a **wait-for-it** strategy or a **retry mechanism** to ensure that the Spring Boot application container only attempts to connect to the database after the MySQL container is ready.

### **Approach 1: Using `wait-for-it` Script (Recommended)**
This is a common approach where the Spring Boot container will wait for the MySQL container to be available before starting. Here's how you can do it:

1. **Add `wait-for-it` script to your project**:
   - `wait-for-it` is a simple script that waits for a TCP connection to be available before proceeding. You can download the script from [wait-for-it GitHub repository](https://github.com/vishnubob/wait-for-it).

2. **Download `wait-for-it.sh`** and add it to your project directory or Docker image:
   - You can download it using `curl` or `wget` from the `wait-for-it` GitHub repository.
   - Place it in your Spring Boot Docker image so that it can be used before starting your Spring Boot application.

3. **Update Your Jenkinsfile with a Wait Strategy**:

   In the Jenkins `Deploy` stage, modify the Docker run command to ensure that it waits for the MySQL container to be available.

### Updated `Deploy to Local Server` Stage

```groovy
stage('Deploy to Local Server') {
    steps {
        script {
            // Start MySQL container if it's not running already
            sh '''
            docker stop mysql-container || true
            docker rm mysql-container || true
            docker run -d --name mysql-container -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=fleet mysql:latest
            '''

            // Wait for MySQL to be ready (with a 30-second timeout, modify as needed)
            sh '''
            docker exec fms-container /bin/bash -c "until curl --silent mysql-container:3306; do echo 'Waiting for MySQL...'; sleep 5; done"
            '''

            // Now, start the Spring Boot application
            sh '''
            docker stop fms-container || true
            docker rm fms-container || true
            docker run -d -p 8081:8081 --name fms-container \
                -e DB_URL=jdbc:mysql://mysql-container:3306/fleet \
                -e DB_USER=root \
                -e DB_PASSWORD=password \
                $DOCKER_IMAGE
            '''
        }
    }
}
```

### **Explanation:**

1. **Starting MySQL Container**:
   - We first ensure the MySQL container is not running (`docker stop` and `docker rm`), and then we start a fresh MySQL container with the necessary environment variables.

2. **Waiting for MySQL to be Ready**:
   - Using `docker exec`, we execute the `curl` command inside the `fms-container` to check if MySQL is up and running (`mysql-container:3306` is the MySQL container's hostname and port).
   - If MySQL is not yet available, it waits for 5 seconds and checks again (`sleep 5`), until it gets a successful connection.

3. **Start Spring Boot Application**:
   - Once MySQL is confirmed to be ready, we proceed to stop and remove the Spring Boot container (`docker stop` and `docker rm`), and then start the Spring Boot container (`docker run`).

### **Approach 2: Docker Compose (Recommended for Complex Deployments)**

If your deployment becomes more complex, you may want to use **Docker Compose** to manage multi-container applications like Spring Boot and MySQL. This way, Docker handles waiting for dependencies automatically.

1. **Create a `docker-compose.yml` file** in your project root:
   
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:latest
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: fleet
    ports:
      - "3306:3306"

  springboot:
    image: ustapi/fms:latest
    depends_on:
      - mysql
    environment:
      DB_URL: jdbc:mysql://mysql:3306/fleet
      DB_USER: root
      DB_PASSWORD: password
    ports:
      - "8081:8081"
```

2. **Update Jenkinsfile to use Docker Compose**:
   - You can replace the `docker run` commands with `docker-compose` commands to handle both the Spring Boot and MySQL containers.

Updated Jenkinsfile (with Docker Compose):

```groovy
stage('Deploy to Local Server') {
    steps {
        script {
            // Start the services using Docker Compose
            sh 'docker-compose -f docker-compose.yml up -d'
        }
    }
}
```

### **Explanation of Docker Compose Approach**:

- **`depends_on`**: The `springboot` service depends on the `mysql` service. Docker Compose will start the MySQL container first and wait for it to be ready before starting the Spring Boot container.
- **`docker-compose.yml`**: Docker Compose handles the configuration of both containers, including the database connection and environment variables. It also makes it easier to manage and scale your services.
- **`docker-compose up -d`**: This starts the containers in detached mode, allowing the Jenkins pipeline to continue without blocking.

---

### **Conclusion:**
1. **Using `wait-for-it`**: You can modify your Jenkinsfile to ensure that the Spring Boot container waits for MySQL to be ready before it starts.
2. **Using Docker Compose**: For easier management of multi-container applications, consider using Docker Compose, where Docker handles the waiting and dependency between containers automatically.

Both approaches will ensure that your Spring Boot application can successfully connect to the MySQL database, even if it takes time for MySQL to start.


---
## Credentials security

To securely handle database credentials in a `docker-compose.yml` file while deploying using Jenkins, you can use environment variables and Jenkins credentials in the following manner. Docker Compose allows you to define environment variables in multiple ways, including passing them at runtime, which is especially useful for sensitive information.

### Steps:

1. **Store Secrets in Jenkins Credentials**:
   - As we discussed earlier, store sensitive information like `MYSQL_ROOT_PASSWORD`, `DB_PASSWORD`, and `DB_URL` in Jenkins Credentials.

2. **Modify `docker-compose.yml`** to use environment variables:
   - You will modify your `docker-compose.yml` file to reference these environment variables that Jenkins will inject securely.

3. **Update Jenkinsfile** to use `withCredentials` to pass the secrets to the Docker Compose process.

### **Example**:

#### **1. Updated `docker-compose.yml`** to Use Environment Variables

In your `docker-compose.yml`, you will replace sensitive information (like passwords) with environment variables. These will be populated by Jenkins at runtime.

```yaml
version: '3.7'

services:
  mysql:
    image: mysql:latest
    container_name: mysql-container
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    networks:
      - app-network

  fms:
    image: ustapi/fms:latest
    container_name: fms-container
    environment:
      DB_URL: jdbc:mysql://mysql-container:3306/${MYSQL_DATABASE}
      DB_USER: root
      DB_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8081:8081"
    networks:
      - app-network
    depends_on:
      - mysql

networks:
  app-network:
    driver: bridge
```

- **Explanation**:
  - The MySQL service is configured to use `MYSQL_ROOT_PASSWORD` and `MYSQL_DATABASE` environment variables that will be passed at runtime.
  - The `fms` service (your Spring Boot application) is configured to use `DB_URL`, `DB_USER`, and `DB_PASSWORD` environment variables.
  - These values will be injected from Jenkins, ensuring that sensitive information is not exposed in the `docker-compose.yml` file.

#### **2. Modify the Jenkinsfile** to Pass Secrets to Docker Compose

Update your `Jenkinsfile` to inject the credentials using the `withCredentials` block before running the `docker-compose` command.

```groovy
pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'ustapi/fms:latest'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git credentialsId: 'github-credentials', url: 'https://github.com/your-repo.git', branch: 'main'
            }
        }

        stage('Build Maven Project') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE .'
            }
        }

        stage('Login to Docker Hub') {
            steps {
                withDockerRegistry([credentialsId: 'docker-hub-credentials', url: 'https://index.docker.io/v1/']) {
                    sh 'echo "Logged into Docker Hub successfully"'
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withDockerRegistry([credentialsId: 'docker-hub-credentials', url: 'https://index.docker.io/v1/']) {
                    sh 'docker push $DOCKER_IMAGE'
                }
            }
        }

        stage('Deploy to Local Server') {
            steps {
                withCredentials([
                    string(credentialsId: 'MYSQL_ROOT_PASSWORD', variable: 'MYSQL_ROOT_PASSWORD'),
                    string(credentialsId: 'MYSQL_DATABASE', variable: 'MYSQL_DATABASE'),
                    string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD')
                ]) {
                    script {
                        // Export credentials to environment variables for Docker Compose
                        sh """
                            export MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD
                            export MYSQL_DATABASE=$MYSQL_DATABASE
                            export DB_PASSWORD=$DB_PASSWORD
                            docker-compose -f docker-compose.yml up -d
                        """
                    }
                }
            }
        }
    }
}
```

#### **Explanation of Key Changes**:

1. **Using `withCredentials` to Securely Inject Secrets**:
   - The `withCredentials` block securely retrieves credentials like `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, and `DB_PASSWORD` from Jenkins and injects them into the environment for the `docker-compose` command.
   - This ensures that sensitive data is not exposed in the Jenkinsfile or logs.

2. **Exporting Environment Variables**:
   - Before running `docker-compose`, the environment variables are set using `export` so that they are available to the `docker-compose.yml` file at runtime.

3. **Running Docker Compose**:
   - The `docker-compose` command will now be able to reference these environment variables (like `MYSQL_ROOT_PASSWORD`, `DB_PASSWORD`, etc.) when creating the services, ensuring that your containers are configured securely with the proper credentials.

### **Final Setup**:
- **Jenkins Credentials**:
  - Store `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, and `DB_PASSWORD` securely in Jenkins (following the steps I provided in previous responses).
  
- **Jenkinsfile**:
  - The Jenkins pipeline securely injects the credentials at runtime using `withCredentials`, making it easy to handle sensitive information without exposing it.

- **Docker Compose**:
  - Docker Compose reads the environment variables and passes them to the containers, ensuring that the services are configured correctly.

This approach ensures that sensitive data such as passwords and database names are handled securely and not exposed in the Jenkinsfile or in the `docker-compose.yml`. 


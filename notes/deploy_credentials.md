Storing sensitive information like `MYSQL_ROOT_PASSWORD` and `DB_PASSWORD` directly in the Jenkinsfile is not recommended, as it can expose these secrets to anyone with access to the Jenkinsfile or logs.

To handle sensitive data securely in Jenkins, you can use **Jenkins Credentials** to store the secrets and pass them to  pipeline at runtime. This keeps  credentials secure and avoids exposing them in the Jenkinsfile.

Here’s how to secure manage and inject sensitive information (like database passwords) into  Jenkins pipeline:

### Steps to Handle Secrets Securely in Jenkins

#### **Step 1: Create Jenkins Credentials for MySQL Password**

1. **Go to Jenkins Dashboard**.
2. **Navigate to "Manage Jenkins"** → **"Manage Credentials"**.
3. **Select the appropriate credentials store** (like "Global credentials" or a specific domain).
4. **Click on "Add Credentials"**.
5. For **Kind**, choose `Secret text` if you’re storing a single password.
6. Enter your **MySQL root password** in the **Secret** field.
7. Give it a meaningful **ID** (e.g., `MYSQL_ROOT_PASSWORD`).
8. **Save** the credentials.

Repeat this process for other secrets, like the `DB_PASSWORD` and any other sensitive data you need to pass to the containers.

#### **Step 2: Use Jenkins Credentials in the Pipeline**

In your Jenkinsfile, instead of hardcoding the values, you can use the `withCredentials` block to inject these values securely at runtime. Here’s how you can modify your `Jenkinsfile`:

### Updated Jenkinsfile (Using Jenkins Credentials)

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
                withCredentials([string(credentialsId: 'MYSQL_ROOT_PASSWORD', variable: 'MYSQL_ROOT_PASSWORD'), 
                                 string(credentialsId: 'MYSQL_DATABASE', variable: 'MYSQL_DATABASE'),
                                 string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD')]) {
                    // Start MySQL container if it's not running already
                    sh '''
                    docker stop mysql-container || true
                    docker rm mysql-container || true
                    docker run -d --name mysql-container \
                        -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD \
                        -e MYSQL_DATABASE=$MYSQL_DATABASE \
                        mysql:latest
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
                        -e DB_PASSWORD=$DB_PASSWORD \
                        $DOCKER_IMAGE
                    '''
                }
            }
        }
    }
}
```

### **Explanation of Key Changes:**

1. **Credentials Injection (Secure Handling of Passwords)**:
   - We use the `withCredentials` block to securely inject the `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, and `DB_PASSWORD` environment variables into the `docker run` commands.
   - These secrets are now securely stored in Jenkins and injected into the pipeline only at runtime, avoiding hardcoding the credentials in the Jenkinsfile.
   - The credentials are accessible within the pipeline only when the `withCredentials` block is executed, and they are **not exposed in Jenkins logs** or the pipeline script.

2. **Avoid Hardcoding Secrets**:
   - The `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, and `DB_PASSWORD` values are securely pulled from Jenkins credentials and passed to the MySQL container environment variables during runtime.
   
3. **Using Jenkins Secrets for Database Credentials**:
   - Instead of writing the passwords in the `docker run` command directly, these are passed as environment variables from Jenkins.

---

### **Summary**:
By using **Jenkins Credentials**, you can securely manage sensitive information like database passwords and Docker Hub credentials. The `withCredentials` block ensures that these secrets are injected securely into your pipeline during runtime, without exposing them in the Jenkinsfile or logs.

If you need to store other secrets, like Docker Hub credentials or API keys, you can follow the same pattern.


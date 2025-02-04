---

## **How to Handle Docker Hub Credentials Securely in Jenkins?**
Follow these steps to use **Jenkins credentials securely**:

### **1. Add Docker Hub Credentials in Jenkins**
1. Open **Jenkins Dashboard**.
2. Go to **Manage Jenkins** → **Manage Credentials**.
3. Click on **(global) or create a new domain**.
4. Click on **Add Credentials**.
   - **Kind**: Username and password.
   - **Username**: `ustapi` (your Docker Hub username).
   - **Password**: Your Docker Hub password or **personal access token**.
   - **ID**: `docker-hub-credentials` (use this ID in your Jenkinsfile).
5. Click **Save**.

---

### **2. Modify Jenkinsfile to Use Credentials Securely**
Update your `Jenkinsfile` **without hardcoding credentials**:

```groovy
stage('Push to Docker Hub') {
    steps {
        withDockerRegistry([credentialsId: 'docker-hub-credentials', url: 'https://index.docker.io/v1/']) {
            sh 'docker push ustapi/fms:latest'
        }
    }
}
```

---

### **How Does This Work?**
- `withDockerRegistry([credentialsId: 'docker-hub-credentials'])`
  - Jenkins automatically fetches the **Docker username & password** securely.
  - No credentials are exposed in **GitHub** or in your **Jenkinsfile**.

---

### **Final Summary**
**No credentials in GitHub**.  
**Securely stored in Jenkins Credential Manager**.  
**Jenkins automatically injects credentials** when running `docker push`.  


---
## **How Deployment Works in Jenkins?**
After pushing the image to **Docker Hub**, you have **two deployment options**:
1. **Run it manually** on a server (by pulling from Docker Hub).  
2. **Automate deployment** using Jenkins (by running it on a specific deployment server).  

---

## **How Does My Jenkins Pipeline Work?**
### **Understanding This Stage**
```groovy
stage('Deploy') {
    steps {
        sh '''
        docker stop fms-container || true
        docker rm fms-container || true
        docker run -d -p 8081:8081 --name fms-container \
            -e DB_URL=$DB_URL -e DB_USER=$DB_USER -e DB_PASSWORD=$DB_PASSWORD \
            ustapi/fms:latest
        '''
    }
}
```
👉 **By default, this runs on the Jenkins server itself.**  
👉 If **Jenkins is running on a cloud VM**, it deploys **on the same VM**.  
👉 If **Jenkins is running locally**, it deploys **on your local machine**.  

---

## **Where Should the Deployment Happen?**
- If you **want to deploy on a different server**, you need to **SSH into that server** and execute these commands there.
- If **Jenkins is installed on your target server**, then this works **automatically**.

---

## **How to Deploy on a Remote Server?**
If your Jenkins server is **not the target deployment machine**, update the pipeline to **SSH into the deployment server** and run the deployment commands.

### **Updated Jenkinsfile**
Modify the `Deploy` stage like this:
```groovy
stage('Deploy to Server') {
    steps {
        sh '''
        ssh -i /path/to/your-key.pem ubuntu@your-server-ip << EOF
            docker stop fms-container || true
            docker rm fms-container || true
            docker pull ustapi/fms:latest
            docker run -d -p 8081:8081 --name fms-container \
                -e DB_URL=$DB_URL -e DB_USER=$DB_USER -e DB_PASSWORD=$DB_PASSWORD \
                ustapi/fms:latest
        EOF
        '''
    }
}
```

### **Explanation**
1. **SSH into the deployment server** (replace `/path/to/your-key.pem` and `your-server-ip`).
2. **Pull the latest Docker image** from Docker Hub.
3. **Stop & remove any running container** with the same name.
4. **Run the new container** with environment variables.

---

## **Do I Need to Manually Pull and Run?**
- If you **don't use Jenkins for deployment**, you need to **SSH into the server** and pull the image manually.
- If you **use Jenkins**, it **automates the pull and run process** on the target server.

---

## **Final Workflow**
1. **Code Push to GitHub** → Triggers Jenkins build.  
2. **Jenkins builds & pushes Docker image** to Docker Hub.  
3. **Jenkins SSHs into the deployment server** and runs `docker pull & run`.  
4. **Application is live on the deployment server**.  

---

## **Summary**
- If **Jenkins is running on the deployment server**, it deploys **automatically**.  
- If **Jenkins is separate**, you need to **SSH into the target server** from Jenkins.  
- Without Jenkins, **you pull & run manually** on your deployment server.  


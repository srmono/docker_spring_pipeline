## **ClusterIP vs. NodePort in Kubernetes**

Kubernetes provides different types of services to expose your application inside or outside the cluster.

### **1️⃣ ClusterIP (Default)**
- **What it does:** Exposes the service **inside** the Kubernetes cluster.
- **Use case:** Internal communication between microservices.
- **Access:** **Only within the cluster** (not accessible from your host machine).
- **Example:**
  ```yaml
  apiVersion: v1
  kind: Service
  metadata:
    name: mysql-service
  spec:
    selector:
      app: mysql
    ports:
      - protocol: TCP
        port: 3306  # Service Port
        targetPort: 3306  # Container Port
    clusterIP: None  # Headless Service (Used for StatefulSets)
  ```

**How to access a ClusterIP service?**
- From inside a pod:
  ```sh
  mysql -h mysql-service -u root -p
  ```
- From another pod using environment variables:
  ```sh
  echo $MYSQL_SERVICE_HOST
  echo $MYSQL_SERVICE_PORT
  ```

---

### **2️⃣ NodePort (Exposes Outside Cluster)**
- **What it does:** Exposes the service on **each** Kubernetes node’s IP at a static port (range: `30000-32767`).
- **Use case:** When you want to access your application **from your local machine**.
- **Access:** `http://<MINIKUBE_IP>:<NodePort>`
- **Example:**
  ```yaml
  apiVersion: v1
  kind: Service
  metadata:
    name: fms-service
  spec:
    selector:
      app: fms
    ports:
      - protocol: TCP
        port: 80  # Inside Cluster
        targetPort: 8081  # Spring Boot App Port
        nodePort: 30007  # Exposes outside cluster
    type: NodePort
  ```

**How to access a NodePort service?**
1. Get Minikube IP:
   ```sh
   minikube ip
   ```
   Example output: `192.168.49.2`
   
2. Find NodePort:
   ```sh
   kubectl get svc fms-service
   ```
   Example output:
   ```
   NAME         TYPE       CLUSTER-IP     EXTERNAL-IP   PORT(S)        AGE
   fms-service NodePort   10.100.12.34   <none>        80:30007/TCP   10m
   ```
   🔹 Here, the **NodePort** is `30007`.

3. Access the service in a browser:
   ```
   http://192.168.49.2:30007
   ```

---

### **When to Use What?**
| Feature   | ClusterIP (Default) | NodePort |
|-----------|--------------------|----------|
| **Use Case** | Internal service communication | External access from a local machine |
| **Access** | Only within Kubernetes | Available on Node’s IP & Port |
| **Example** | MySQL Database, Internal APIs | Web apps, REST APIs exposed to users |
| **Security** | Safer (not exposed outside) | Less secure, publicly accessible |

---

### **3️⃣ LoadBalancer (For Cloud Services)**
In cloud environments like AWS, GCP, and Azure, we use **LoadBalancer** services to expose an app externally.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: fms-service
spec:
  selector:
    app: fms
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8081
  type: LoadBalancer
```
**Note:** In Minikube, LoadBalancer won’t work by default. Instead, you can use:
```sh
minikube tunnel
```

---

## **🔹 Summary**
- **ClusterIP:** Internal access only (e.g., MySQL).
- **NodePort:** Exposes a service on a fixed port (e.g., Spring Boot App).
- **LoadBalancer:** Used in cloud environments.


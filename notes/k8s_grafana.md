### **Deep Dive: Kubernetes Monitoring with Prometheus & Grafana**   

In this lab, we will:  
 Set up **Prometheus** for monitoring Kubernetes metrics  
 Set up **Grafana** for visualizing metrics  
 Configure **Dashboards** for CPU, memory, and pod health  

---

## **🔹 Step 1: Enable Kubernetes Metrics Server**  
Prometheus relies on the **metrics-server** to collect cluster metrics.

1️⃣ Enable the metrics server in Minikube:  
```sh
minikube addons enable metrics-server
```

2️⃣ Verify that the metrics server is running:  
```sh
kubectl get pods -n kube-system | grep metrics-server
```

---

## **🔹 Step 2: Install Prometheus & Grafana using Helm**  
**Helm** simplifies Kubernetes package management.

1️⃣ Add the Helm repository:  
```sh
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```

2️⃣ Install **Prometheus** and **Grafana** in the `monitoring` namespace:  
```sh
kubectl create namespace monitoring
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring
```

3️⃣ Verify installation:  
```sh
kubectl get pods -n monitoring
```
Look for **prometheus-server** and **grafana** pods.

---

## **🔹 Step 3: Access Prometheus & Grafana**
### **🔹 Access Prometheus**
```sh
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090
```
Open **http://localhost:9090** in your browser.  

Go to **Status > Targets** to see active data sources.

### **🔹 Access Grafana**
1️⃣ Get Grafana’s admin password:  
```sh
kubectl get secret --namespace monitoring prometheus-grafana -o jsonpath="{.data.admin-password}" | base64 --decode
```

2️⃣ Forward Grafana’s port:  
```sh
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
```

3️⃣ Open **http://localhost:3000** in your browser.  
   - **Username:** `admin`  
   - **Password:** Use the decoded password from Step 1.  

---

## **🔹 Step 4: Set Up Kubernetes Dashboards in Grafana**
### **🔹 Add Prometheus as a Data Source**
1️⃣ In Grafana, go to **Configuration > Data Sources**  
2️⃣ Click **Add Data Source**  
3️⃣ Select **Prometheus**  
4️⃣ Set the URL to:  
```
http://prometheus-kube-prometheus-prometheus.monitoring.svc.cluster.local:9090
```
5️⃣ Click **Save & Test**   

### **🔹 Import Prebuilt Kubernetes Dashboards**
1️⃣ Go to **Dashboards > Import**  
2️⃣ Use dashboard IDs from [Grafana's dashboard library](https://grafana.com/grafana/dashboards/):  
   - **Cluster Monitoring:** `3119`  
   - **Pod/Container Metrics:** `6417`  
3️⃣ Click **Import** 🎉  

---

## **🔹 Step 5: Query & Monitor Kubernetes Metrics**
### ** Basic Prometheus Queries**
- **Check CPU usage per node:**  
  ```sh
  sum(rate(node_cpu_seconds_total{mode!="idle"}[5m])) by (instance)
  ```
- **Check memory usage per pod:**  
  ```sh
  sum(container_memory_usage_bytes{container!=""}) by (pod)
  ```
- **Check pod restarts:**  
  ```sh
  sum(kube_pod_container_status_restarts_total) by (pod)
  ```

### ** Grafana Alerts (Optional)**
Set up alerts for **high CPU usage**:  
1️⃣ Go to **Alerting > New Alert**  
2️⃣ Create an alert with this query:  
```sh
sum(rate(container_cpu_usage_seconds_total[5m])) by (pod) > 0.5
```
3️⃣ Configure **Slack, Email, or PagerDuty** notifications.

---

## **Summary of What We Did**
 Installed **Prometheus & Grafana** with Helm  
 Configured **data sources & dashboards**  
 Used **PromQL queries** for real-time monitoring  
 Set up **alerts** for high CPU usage  



---

Set up **Grafana** on **Kubernetes** using your existing setup. We will be using **Minikube** to test locally, and I'll walk you through each step, including the necessary commands and configurations. 

We'll install **Grafana** as a **Kubernetes deployment**, expose it via a **Service**, and make sure it’s connected to your **Prometheus** instance for monitoring (if you are using Prometheus, which is common with Grafana).

### **Prerequisites:**
1. **Minikube** installed and running.
2. **kubectl** configured to interact with your Minikube cluster.
3. **Helm** (optional but recommended for Grafana) installed for easier deployment.

### **Step-by-Step Guide to Setup Grafana on Kubernetes**

---

### **1. Start Minikube**

If you haven’t already started Minikube, do so with the following command:

```sh
minikube start
```

This will set up a local Kubernetes cluster on your machine. Ensure that `kubectl` is configured to communicate with your Minikube cluster:

```sh
kubectl config use-context minikube
```

---

### **2. Install Grafana with Helm**

Using Helm is the easiest way to deploy Grafana. If you don’t have Helm installed, you can install it using:

```sh
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash
```

Now, let’s add the **Grafana Helm chart repository** and install Grafana:

```sh
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
```

### **3. Create a Namespace (Optional)**

It's a good practice to create a specific namespace for Grafana to isolate it from other applications:

```sh
kubectl create namespace monitoring
```

---

### **4. Deploy Grafana**

Now, use Helm to deploy Grafana in the **monitoring** namespace:

```sh
helm install grafana grafana/grafana --namespace monitoring
```

This command will:
- Install Grafana.
- Automatically set up default configurations.

### **5. Check Grafana Pods**

Once the installation is complete, check the Grafana pods running in the `monitoring` namespace:

```sh
kubectl get pods -n monitoring
```

You should see a pod running for Grafana, something like:

```
NAME                                    READY   STATUS    RESTARTS   AGE
grafana-xxxxxxxxxx-xxxxx                1/1     Running   0          2m
```

---

### **6. Expose Grafana with a Service**

Next, expose Grafana to access it via a browser.

We’ll expose it using a **LoadBalancer** service, but since Minikube doesn’t support LoadBalancer services natively, we’ll use `kubectl port-forward` to expose Grafana on a local port (default is port 3000).

Run the following command:

```sh
kubectl port-forward -n monitoring service/grafana 3000:80
```

This will forward traffic from `localhost:3000` to the Grafana instance inside Kubernetes.

### **7. Access Grafana Dashboard**

Now, open your browser and go to:

```
http://localhost:3000
```

#### Default Credentials:
- **Username**: `admin`
- **Password**: `admin`

You will be prompted to change the password after logging in for the first time.

---

### **8. Add Prometheus as a Data Source (Optional)**

If you are using **Prometheus** to collect metrics, you can configure Grafana to pull data from it.

#### **Step 1: Add Prometheus Data Source in Grafana**

1. Log in to the Grafana dashboard.
2. Click on the **gear icon** (⚙️) on the left sidebar to go to **Configuration**.
3. Click on **Data Sources**.
4. Select **Prometheus** from the list of available data sources.
5. Set the URL of your Prometheus instance (if you have Prometheus deployed in the same cluster, use its service name):
   - URL: `http://prometheus-service:9090` (replace `prometheus-service` with your actual service name).
6. Click **Save & Test** to verify the connection.

---

### **9. Access Grafana Dashboards**

After configuring the data source, you can now start adding dashboards:

1. On the left sidebar, click on **+** and select **Import**.
2. You can either:
   - Enter a **dashboard ID** (from the [Grafana Dashboard repository](https://grafana.com/grafana/dashboards)).
   - Upload a **JSON file** for a custom dashboard.

---

### **10. Clean Up (Optional)**

Once you're done testing, you can clean up your Grafana deployment using Helm:

```sh
helm uninstall grafana -n monitoring
```

Or you can delete the entire **monitoring** namespace if you no longer need it:

```sh
kubectl delete namespace monitoring
```

---

### **Conclusion**

With the above steps, you now have **Grafana** running in your **Minikube** Kubernetes cluster! You’ve deployed it with **Helm**, exposed it locally via port-forwarding, and connected it to a **Prometheus** data source (if applicable). This allows you to monitor your applications and services directly from Grafana’s dashboards.


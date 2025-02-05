Kubernetes (K8s) is an open-source container orchestration platform that automates the deployment, scaling, and management of containerized applications. Here are the fundamental concepts you need to understand:

---

## **1. Kubernetes Architecture**
Kubernetes follows a **Master-Worker** architecture.

### **Master Node (Control Plane)**
Manages the cluster and ensures the desired state of applications. It consists of:
- **API Server**: The main entry point for managing the cluster.
- **Scheduler**: Assigns workloads (Pods) to worker nodes.
- **Controller Manager**: Maintains cluster state (e.g., scaling, node monitoring).
- **etcd**: A key-value store that keeps cluster configuration and state.

### **Worker Node**
Runs application workloads. It consists of:
- **Kubelet**: Communicates with the API server and manages Pods on the node.
- **Container Runtime**: Runs containers (Docker, containerd, CRI-O, etc.).
- **Kube Proxy**: Manages network communication between Pods.

---

## **2. Kubernetes Core Components**
### **Pods**
- The smallest deployable unit in Kubernetes.
- Can contain one or more containers.

### **Deployments**
- Ensures desired number of Pods are running.
- Provides rolling updates and rollbacks.

### **Services**
- Exposes Pods to network (internal or external).
- Types:
  - **ClusterIP**: Internal communication.
  - **NodePort**: Exposes service on a static port.
  - **LoadBalancer**: Exposes service to the internet.
  - **ExternalName**: Maps service to an external domain.

### **ConfigMaps & Secrets**
- **ConfigMaps**: Store configuration data.
- **Secrets**: Store sensitive information (e.g., passwords, API keys).

### **Persistent Volumes (PVs) & Persistent Volume Claims (PVCs)**
- Provides persistent storage for stateful applications.

### **Namespaces**
- Logical partitions in a Kubernetes cluster for organizing resources.

---

## **3. Kubernetes Networking**
- Every Pod gets a unique IP.
- Containers in the same Pod share a network.
- Communication between Pods is handled by **Cluster Networking**.
- **Ingress**: Manages external access to services (e.g., HTTP/HTTPS).

---

## **4. Basic kubectl Commands**
- `kubectl get pods` – List all running Pods.
- `kubectl get nodes` – View cluster nodes.
- `kubectl create -f <filename>.yaml` – Deploy a resource from a YAML file.
- `kubectl apply -f <filename>.yaml` – Update resources from a YAML file.
- `kubectl delete pod <pod-name>` – Delete a Pod.

---

## **5. Writing YAML Files**
Example **Deployment** YAML:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
    spec:
      containers:
      - name: my-container
        image: nginx
        ports:
        - containerPort: 80
```

---

## **Next Steps**
1. **Practice on Minikube** (a local K8s cluster).
2. **Deploy a simple app** using `kubectl`.
3. **Explore Helm Charts** for easier deployments.
4. **Learn about K8s security & monitoring** (RBAC, Prometheus, Grafana).


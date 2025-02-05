 **Hands-on lab** focusing on **Security & Reliability** in Kubernetes.  

We will:  
 Secure pods using **RBAC, Network Policies, and Security Context**  
 Improve reliability with **Liveness & Readiness Probes**  
 Ensure high availability with **Autoscaling**  

---

## **🔹 Step 1: Setup Minikube (if not already done)**  
Ensure **Minikube** is running:  
```sh
minikube start
kubectl cluster-info
```

---

## **🔹 Step 2: Apply Role-Based Access Control (RBAC)**
### **🔹 Create a Read-Only Role**
1️⃣ Create a file **`rbac.yaml`**:  
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: default
  name: pod-reader
rules:
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "list"]
```

2️⃣ Apply it:  
```sh
kubectl apply -f rbac.yaml
```

### **🔹 Bind the Role to a User (ServiceAccount)**
Create **rolebinding.yaml**:  
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: read-only-binding
  namespace: default
subjects:
- kind: ServiceAccount
  name: default
  namespace: default
roleRef:
  kind: Role
  name: pod-reader
  apiGroup: rbac.authorization.k8s.io
```

Apply it:  
```sh
kubectl apply -f rolebinding.yaml
```

 **Now, users can only list pods but cannot delete or modify them!**

---

## **🔹 Step 3: Apply Network Policies**
Network Policies restrict communication between pods.

### **🔹 Deny All Traffic by Default**
Create **network-policy.yaml**:  
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: deny-all
  namespace: default
spec:
  podSelector: {}
  policyTypes:
  - Ingress
```

Apply it:  
```sh
kubectl apply -f network-policy.yaml
```

 **Now, no pod can communicate unless explicitly allowed!**

---

## **🔹 Step 4: Use Security Context (Restrict Root Access)**
### **🔹 Create a Secure Deployment**
Create **secure-deployment.yaml**:  
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: secure-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: secure-app
  template:
    metadata:
      labels:
        app: secure-app
    spec:
      securityContext:
        runAsUser: 1000
        runAsNonRoot: true
      containers:
      - name: app
        image: nginx
        securityContext:
          readOnlyRootFilesystem: true
          allowPrivilegeEscalation: false
        ports:
        - containerPort: 80
```

Apply it:  
```sh
kubectl apply -f secure-deployment.yaml
```

 **Now, pods cannot run as root or modify the filesystem!**

---

## **🔹 Step 5: Improve Reliability with Liveness & Readiness Probes**
Modify **secure-deployment.yaml**:  
```yaml
livenessProbe:
  httpGet:
    path: /
    port: 80
  initialDelaySeconds: 3
  periodSeconds: 5

readinessProbe:
  httpGet:
    path: /
    port: 80
  initialDelaySeconds: 3
  periodSeconds: 5
```

Apply changes:  
```sh
kubectl apply -f secure-deployment.yaml
```

 **Now, Kubernetes restarts the pod if it becomes unhealthy!**

---

## **🔹 Step 6: Ensure High Availability with Autoscaling**
Enable the **metrics server** (required for autoscaling):  
```sh
minikube addons enable metrics-server
```

### **🔹 Create an HPA (Horizontal Pod Autoscaler)**
```sh
kubectl autoscale deployment secure-app --cpu-percent=50 --min=2 --max=5
```

Check autoscaler status:  
```sh
kubectl get hpa
```

 **Now, pods scale based on CPU usage!**

---

## **🔹 Step 7: Test & Verify**
1️⃣ **Check running pods:**  
```sh
kubectl get pods
```

2️⃣ **Test Network Policy (No Traffic Allowed):**  
```sh
kubectl run test --rm -it --image=busybox -- sh
wget secure-app
```
❌ It should fail because of **NetworkPolicy**.

3️⃣ **Force Autoscaling by increasing CPU load:**  
```sh
kubectl run cpu-load --image=busybox --requests=cpu=500m -- /bin/sh -c "while true; do :; done"
kubectl get hpa
```
You should see more pods scaling up!

---

## **Summary of Best Practices We Implemented**
 **RBAC:** Restricts user permissions.  
 **Network Policies:** Controls pod-to-pod communication.  
 **Security Context:** Prevents root access & privilege escalation.  
 **Liveness & Readiness Probes:** Ensures healthy pods.  
 **Autoscaling:** Keeps apps running under high load.  

---
 


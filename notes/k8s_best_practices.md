# **Kubernetes (K8s) Best Practices**  

To ensure **scalability, security, and reliability**, follow these best practices when working with Kubernetes.

---

## **1. General Cluster Management**
###  **Use Namespaces for Organization**
- Separate environments (e.g., `dev`, `staging`, `prod`) using **Namespaces**.
- Example:
  ```sh
  kubectl create namespace dev
  ```

###  **Use Labels & Annotations**
- Labels help group resources (e.g., `app=myapp`, `env=prod`).
- Example:
  ```yaml
  metadata:
    labels:
      app: myapp
      env: prod
  ```

###  **Monitor Resource Usage**
- Use **Prometheus**, **Grafana**, or **Kubernetes Metrics Server**.
  ```sh
  kubectl top pods
  ```

---

## **2. Pod & Deployment Best Practices**
###  **Use Resource Requests & Limits**
- Prevent a single pod from consuming all cluster resources.
- Example:
  ```yaml
  resources:
    requests:
      cpu: "250m"
      memory: "64Mi"
    limits:
      cpu: "500m"
      memory: "128Mi"
  ```

###  **Always Define a Liveness & Readiness Probe**
- Ensure pods are healthy before sending traffic.
- Example:
  ```yaml
  livenessProbe:
    httpGet:
      path: /
      port: 80
    initialDelaySeconds: 3
    periodSeconds: 5
  ```

###  **Use Rolling Updates & Avoid Recreating Pods**
- Update deployments **without downtime**:
  ```sh
  kubectl set image deployment/myapp mycontainer=myimage:v2
  ```

###  **Use Init Containers for Pre-Tasks**
- Example: Waiting for a database before starting an app.
  ```yaml
  initContainers:
  - name: check-db
    image: busybox
    command: ['sh', '-c', 'until nc -z db-service 5432; do sleep 2; done']
  ```

---

## **3. Security Best Practices**
###  **Use RBAC (Role-Based Access Control)**
- Example: Read-only access for a user in a namespace.
  ```yaml
  kind: Role
  apiVersion: rbac.authorization.k8s.io/v1
  metadata:
    namespace: dev
    name: read-only-role
  rules:
  - apiGroups: [""]
    resources: ["pods"]
    verbs: ["get", "list"]
  ```

###  **Run Containers as Non-Root User**
- Security best practice to avoid privilege escalation.
  ```yaml
  securityContext:
    runAsUser: 1000
    runAsNonRoot: true
  ```

###  **Use Network Policies**
- Restrict pod-to-pod communication.
  ```yaml
  kind: NetworkPolicy
  apiVersion: networking.k8s.io/v1
  metadata:
    name: deny-all
  spec:
    podSelector: {}
    policyTypes:
    - Ingress
  ```

###  **Store Secrets Securely**
- **Never hardcode secrets** in YAML or environment variables.
- Use **Kubernetes Secrets**:
  ```sh
  kubectl create secret generic my-secret --from-literal=password=mysecurepassword
  ```
- Use **KMS (Key Management System)** or **Sealed Secrets**.

---

## **4. Scalability & Performance**
###  **Use Horizontal Pod Autoscaler (HPA)**
- Scale pods automatically based on CPU/memory.
  ```sh
  kubectl autoscale deployment myapp --cpu-percent=50 --min=2 --max=10
  ```

###  **Use Cluster Autoscaler**
- Automatically add/remove worker nodes.
  ```yaml
  apiVersion: autoscaling/v2beta1
  kind: HorizontalPodAutoscaler
  metadata:
    name: myapp-hpa
  spec:
    scaleTargetRef:
      apiVersion: apps/v1
      kind: Deployment
      name: myapp
    minReplicas: 2
    maxReplicas: 10
    metrics:
    - type: Resource
      resource:
        name: cpu
        targetAverageUtilization: 50
  ```

###  **Use Readiness Probes for Load Balancing**
- Prevent traffic from reaching unhealthy pods.

---

## **5. Networking & Service Management**
###  **Use Ingress for External Access**
- Instead of exposing multiple NodePorts.
  ```yaml
  apiVersion: networking.k8s.io/v1
  kind: Ingress
  metadata:
    name: myapp-ingress
  spec:
    rules:
    - host: myapp.example.com
      http:
        paths:
        - path: /
          pathType: Prefix
          backend:
            service:
              name: myapp-service
              port:
                number: 80
  ```

###  **Use Service Mesh (Istio/Linkerd) for Microservices**
- Improves security, observability, and traffic control.

---

## **6. Logging & Debugging**
###  **Use Centralized Logging (EFK, Loki, Fluentd)**
- Example: View logs of a specific pod.
  ```sh
  kubectl logs my-pod
  ```

###  **Enable Debugging Tools**
- Run an ephemeral pod for debugging.
  ```sh
  kubectl run debug --rm -it --image=busybox -- /bin/sh
  ```

---

## **7. CI/CD Best Practices**
###  **Use GitOps (ArgoCD, Flux)**
- Deploy from Git instead of manual `kubectl apply`.

###  **Use Helm for Managing Applications**
- Example: Install an app using Helm.
  ```sh
  helm install myapp ./mychart
  ```

###  **Use Kubernetes Operators for Complex Apps**
- Automate stateful applications like databases.

---

## **Final Thoughts**
 **Do**:
- Use **Namespaces, RBAC, and Network Policies**.
- Use **Probes, Autoscaling, and Rolling Updates**.
- Secure credentials with **Secrets**.
- Monitor with **Prometheus, Grafana**.

**Don't**:
- Run containers as **root**.
- Hardcode **secrets** or **IPs**.
- Use **default namespace** for everything.




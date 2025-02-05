Start with a **hands-on lab** using **Minikube**, a lightweight Kubernetes cluster that runs locally.

---

## **Step 1: Install Kubernetes Tools**
You'll need the following installed:

1. **Kubectl** (CLI for interacting with Kubernetes)
   - Install it: [https://kubernetes.io/docs/tasks/tools/install-kubectl/](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

2. **Minikube** (Local Kubernetes cluster)
   - Install it: [https://minikube.sigs.k8s.io/docs/start/](https://minikube.sigs.k8s.io/docs/start/)

3. **Docker** (Container runtime for Minikube)
   - Install it: [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)

---

## **Step 2: Start Minikube**
Open a terminal and run:

```sh
minikube start
```

This sets up a single-node Kubernetes cluster locally.

---

## **Step 3: Verify the Cluster**
Check the cluster status:

```sh
kubectl cluster-info
```

Check running nodes:

```sh
kubectl get nodes
```

---

## **Step 4: Deploy a Simple Application**
Create a basic **Nginx Deployment**.

```sh
kubectl create deployment my-nginx --image=nginx
```

Verify the deployment:

```sh
kubectl get deployments
```

List the running Pods:

```sh
kubectl get pods
```

---

## **Step 5: Expose the Application**
Expose the **Nginx** deployment using a NodePort service:

```sh
kubectl expose deployment my-nginx --type=NodePort --port=80
```

Check the service:

```sh
kubectl get services
```

Find the port where it's exposed:

```sh
minikube service my-nginx --url
```

Open the given URL in a browser to see your running Nginx application!

---

## **Step 6: Scale the Application**
Increase the number of replicas:

```sh
kubectl scale deployment my-nginx --replicas=3
```

Check the updated Pods:

```sh
kubectl get pods
```

---

## **Step 7: Clean Up**
To delete everything:

```sh
kubectl delete service my-nginx
kubectl delete deployment my-nginx
minikube stop
```

---

###  **Next Steps**
- **Deploy an App using YAML** instead of `kubectl create`.
- **Use ConfigMaps & Secrets** for environment variables.
- **Learn Helm** for managing applications.
- **Explore Monitoring** with Prometheus & Grafana.


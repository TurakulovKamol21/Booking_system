# Kubernetes Deployment

This folder contains Kubernetes manifests for the full stack:
- `autoguide-frontend` (Vue + Nginx reverse proxy)
- `autoguide-backend` (Spring Boot)
- `autoguide-postgres`
- `autoguide-mongo`
- `autoguide-redis`
- `autoguide-keycloak` (realm imported automatically)

## 1) Build Docker images

For local clusters (like Minikube), build images into the cluster Docker daemon:

```bash
eval "$(minikube docker-env)"
docker build -t autoguide-backend:latest .
docker build -t autoguide-frontend:latest ./frontend
```

If you use a remote cluster, push images to your registry and update image names in:
- `k8s/backend.yaml`
- `k8s/frontend.yaml`

## 2) Deploy

```bash
kubectl apply -k k8s
```

Check status:

```bash
kubectl -n autoguide get pods
kubectl -n autoguide get svc
```

## 3) Open application

Recommended (auth flow is most stable with this):

```bash
kubectl -n autoguide port-forward svc/autoguide-frontend 8080:80
```

Then open:
- `http://localhost:8080`

Swagger via frontend proxy:
- `http://localhost:8080/swagger-ui.html`

Keycloak via frontend proxy:
- `http://localhost:8080/keycloak`

Alternative (NodePort):
- `http://<node-ip>:30080`

## Notes

- Default namespace: `autoguide`
- Default DB and Keycloak credentials are in `k8s/secrets.yaml` (change before production).
- Uploaded room images are stored in PVC `autoguide-uploads-pvc`.
- Backend waits for all dependencies via init container before startup.
- `kubectl` must be installed and connected to your cluster context.

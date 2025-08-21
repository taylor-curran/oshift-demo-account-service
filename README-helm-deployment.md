# Account Service Helm Chart Deployment

## Prerequisites
- Docker
- Kind
- Helm 3.x
- kubectl

## Quick Start

1. Build and load the container image:
```bash
mvn clean package -DskipTests
docker build -t account-service:1.2.0 .
kind load docker-image account-service:1.2.0 --name openshift-demo
```

2. Deploy with Helm:
```bash
helm install account-service ./helm/account-service --namespace demo-apps --create-namespace
```

3. Verify deployment:
```bash
kubectl get pods -n demo-apps
curl -H "Host: account-service.localhost" http://localhost/api/v1/accounts/health
```

## Configuration

The chart supports customization via values.yaml. Key parameters include:
- `replicaCount`: Number of pod replicas (default: 3)
- `image.tag`: Container image tag (default: 1.2.0)
- `resources`: CPU and memory limits/requests
- `ingress.hosts`: Ingress hostnames for external access

## Windsurf Compliance

This Helm chart follows all Windsurf compliance rules:
- **Rule 01**: Resource requests and limits enforced
- **Rule 02**: Pod security baseline with non-root user, seccomp profile, read-only filesystem
- **Rule 03**: Pinned image tags (no :latest)
- **Rule 04**: Proper naming conventions and mandatory labels

## Health Endpoints

The application provides health endpoints for monitoring:
- `/actuator/health` - General health check (port 8081)
- `/actuator/health/readiness` - Readiness probe (port 8081)
- `/api/v1/accounts/health` - Application-specific health check (port 8080)

## Local Testing with Kind

1. Create Kind cluster:
```bash
kind create cluster --config=kind-config.yaml
```

2. Install NGINX Ingress Controller:
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=90s
```

3. Deploy the application:
```bash
helm install account-service ./helm/account-service --namespace demo-apps --create-namespace
```

4. Test the deployment:
```bash
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=account-service -n demo-apps --timeout=300s
kubectl port-forward svc/account-service 8081:8081 -n demo-apps &
curl http://localhost:8081/actuator/health
```

## Troubleshooting

- Check pod logs: `kubectl logs -l app.kubernetes.io/name=account-service -n demo-apps`
- Verify resources: `kubectl get all -n demo-apps`
- Test connectivity: `kubectl port-forward svc/account-service 8080:80 -n demo-apps`

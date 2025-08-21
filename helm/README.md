# Account Service Helm Chart

This Helm chart deploys the Account Service microservice to Kubernetes with enterprise-grade configuration following Windsurf compliance standards.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- Docker (for building images)
- Kind (for local testing)

## Quick Start

### 1. Build the Application Image

```bash
# Build the Spring Boot application
mvn clean package

# Build Docker image
docker build -t account-service:1.2.0 .
```

### 2. Set up Kind Cluster for Local Testing

```bash
# Create Kind cluster
kind create cluster --name account-service-test

# Load the image into Kind
kind load docker-image account-service:1.2.0 --name account-service-test
```

### 3. Deploy with Helm

```bash
# Install the chart
helm install account-service-dev ./helm/account-service \
  --set image.repository=account-service \
  --set image.tag=1.2.0 \
  --set image.pullPolicy=Never

# Check deployment status
kubectl get pods -n demo-apps

# Test health endpoint
kubectl port-forward -n demo-apps svc/account-service-dev 8081:8081
curl http://localhost:8081/actuator/health
```

## Configuration

### Key Values

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `3` |
| `image.repository` | Image repository | `registry.bank.internal/account-service` |
| `image.tag` | Image tag | `1.2.0` |
| `service.port` | Application port | `8080` |
| `service.managementPort` | Management/actuator port | `8081` |
| `namespace.name` | Target namespace | `demo-apps` |
| `app.environment` | Environment label | `dev` |

### Windsurf Compliance

This chart implements all required Windsurf compliance rules:

- **Naming & Labels**: Mandatory labels including `app.kubernetes.io/name`, `app.kubernetes.io/version`, `app.kubernetes.io/part-of`, `environment`, and `managed-by`
- **Resource Limits**: CPU and memory requests/limits configured
- **Security Context**: Runs as non-root with read-only filesystem and dropped capabilities
- **Image Provenance**: Uses pinned image tags (no `:latest`)

### Health Checks

The application exposes Spring Boot Actuator endpoints:

- **Liveness Probe**: `/actuator/health` on port 8081
- **Readiness Probe**: `/actuator/health/readiness` on port 8081
- **Metrics**: `/actuator/prometheus` on port 8081

## Testing

### Lint the Chart

```bash
helm lint ./helm/account-service
```

### Template Rendering

```bash
helm template account-service-dev ./helm/account-service
```

### Verify Deployment

```bash
# Check all resources
kubectl get pods,svc,ingress -n demo-apps

# Test application endpoints
kubectl port-forward -n demo-apps svc/account-service-dev 8080:80
curl http://localhost:8080/api/v1/accounts/health

# Test management endpoints
kubectl port-forward -n demo-apps svc/account-service-dev 8081:8081
curl http://localhost:8081/actuator/health
```

## Cleanup

```bash
# Uninstall the chart
helm uninstall account-service-dev

# Delete Kind cluster
kind delete cluster --name account-service-test
```

## Production Deployment

For production deployment, ensure:

1. **Secrets**: Create the required secret with database credentials:
   ```bash
   kubectl create secret generic account-service-secrets \
     --namespace=demo-apps \
     --from-literal=DATABASE_URL="jdbc:postgresql://your-postgres:5432/accountdb" \
     --from-literal=DATABASE_USERNAME="your-username" \
     --from-literal=DATABASE_PASSWORD="your-password"
   ```

2. **Image Registry**: Use the proper image repository:
   ```bash
   helm install account-service-prod ./helm/account-service \
     --set image.repository=registry.bank.internal/account-service \
     --set image.tag=1.2.0 \
     --set app.environment=prod
   ```

3. **Ingress**: Enable ingress for external access:
   ```bash
   helm install account-service-prod ./helm/account-service \
     --set ingress.enabled=true \
     --set ingress.hosts[0].host=account-service.yourdomain.com
   ```

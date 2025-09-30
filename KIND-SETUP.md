# Local Kubernetes Environment Setup with Kind

This guide provides step-by-step instructions for setting up a local Kubernetes environment using Kind (Kubernetes IN Docker) to test the migration of our Spring Boot application from Cloud Foundry to Kubernetes.

## Overview

This setup creates a local Kubernetes environment that mirrors the Cloud Foundry deployment configuration from `manifest.yml`:
- 3 application instances (replicas)
- 1024MB memory allocation (1Gi limit in K8s)
- All environment variables from CF manifest
- Health check endpoints on management port 8081
- Service networking for internal communication

## Prerequisites

Ensure you have the following tools installed:
- **Docker** (version 20.10 or later)
- **Java 17** (OpenJDK 17)
- **Maven** (version 3.6 or later)
- **kubectl** (Kubernetes command-line tool)
- **Kind** (will be installed in Step 1)

Verify installations:
```bash
docker --version
java -version
mvn --version
kubectl version --client
```

## Step-by-Step Setup

### Step 1: Install Kind

Install Kind (Kubernetes IN Docker) on your system:

```bash
# Download Kind binary
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.24.0/kind-linux-amd64

# Make it executable
chmod +x ./kind

# Move to system path
sudo mv ./kind /usr/local/bin/kind

# Verify installation
kind version
```

Expected output: `kind v0.24.0 go1.x.x linux/amd64`

### Step 2: Build the Application

Build the Spring Boot application JAR:

```bash
# Navigate to project directory
cd ~/repos/oshift-demo-account-service

# Clean and build the application
mvn clean package -DskipTests
```

This creates `target/account-service-1.2.0.jar`.

### Step 3: Build Docker Image

Build the Docker image using the existing Dockerfile:

```bash
docker build -t account-service:1.2.0 .
```

The Dockerfile uses Red Hat UBI 8 with OpenJDK 17, matching the Cloud Foundry Java buildpack configuration.

### Step 4: Create Kind Cluster

Create a local Kubernetes cluster:

```bash
kind create cluster --name account-service-demo
```

Verify the cluster is running:

```bash
kubectl cluster-info --context kind-account-service-demo
kubectl get nodes
```

### Step 5: Load Docker Image into Kind

Load the built image into the Kind cluster:

```bash
kind load docker-image account-service:1.2.0 --name account-service-demo
```

Verify the image is loaded:

```bash
docker exec -it account-service-demo-control-plane crictl images | grep account-service
```

### Step 6: Deploy to Kubernetes

Apply the Kubernetes manifests in the correct order:

```bash
# Apply ConfigMap (environment variables)
kubectl apply -f k8s-local/configmap.yaml

# Apply Deployment (3 replicas)
kubectl apply -f k8s-local/deployment.yaml

# Apply Service (networking)
kubectl apply -f k8s-local/service.yaml
```

### Step 7: Verify Deployment

Check that all pods are running:

```bash
# Watch pod creation
kubectl get pods -l app=account-service -w

# Wait for all pods to be ready (Ctrl+C to exit watch)
kubectl wait --for=condition=ready pod -l app=account-service --timeout=300s

# Check deployment status
kubectl get deployments
kubectl get pods -l app=account-service
```

Expected output: 3 pods in `Running` state with `READY 1/1`.

View application logs:

```bash
# View logs from all pods
kubectl logs -l app=account-service --tail=50

# Follow logs from a specific pod
kubectl logs -f <pod-name>
```

### Step 8: Test the Application

Test the health endpoint using port-forwarding:

```bash
# Port forward to management port
kubectl port-forward deployment/account-service 8081:8081 &

# Test health endpoint
curl http://localhost:8081/actuator/health

# Expected response: {"status":"UP"}

# Test application port (optional)
kubectl port-forward deployment/account-service 8080:8080 &
curl http://localhost:8080/
```

To stop port-forwarding:

```bash
# Find the process ID
ps aux | grep "port-forward"

# Kill the process
kill <PID>
```

## Configuration Details

### Cloud Foundry to Kubernetes Mapping

| Cloud Foundry | Kubernetes | Notes |
|---------------|------------|-------|
| `instances: 3` | `replicas: 3` | Deployment replicas |
| `memory: 1024M` | `limits.memory: 1Gi` | Memory limit |
| `env:` section | ConfigMap | Environment variables |
| `health-check-http-endpoint` | livenessProbe | Health check endpoint |
| `services:` section | Not implemented | Using H2 database for local testing |
| `routes:` section | Service (ClusterIP) | Internal networking only |

### Simplified for Local Testing

This local Kind setup is simplified compared to the production CF deployment:

- **Database**: Uses H2 in-memory database (configured in `application.properties`) instead of PostgreSQL
- **Cache**: Redis not required for local testing
- **Service Discovery**: Eureka client disabled in ConfigMap
- **Monitoring**: New Relic and DataDog disabled in ConfigMap
- **Namespace**: Uses `default` namespace instead of `demo-apps`
- **Ingress**: No external routing (use port-forward for access)

### Resource Allocation

The deployment specifies resource requests and limits:
- **Memory**: 512Mi request, 1Gi limit (matches CF 1024M)
- **CPU**: 250m request, 500m limit

### Health Checks

- **Liveness Probe**: `/actuator/health` on port 8081 (checks if app needs restart)
- **Readiness Probe**: `/actuator/health` on port 8081 (checks if app can receive traffic)

## Troubleshooting

### Pods Not Starting

Check pod status and events:

```bash
kubectl describe pod <pod-name>
kubectl get events --sort-by=.metadata.creationTimestamp
```

### Image Pull Errors

If you see `ImagePullBackOff` or `ErrImagePull`:

```bash
# Verify image is loaded in Kind
docker exec -it account-service-demo-control-plane crictl images

# Reload image if needed
kind load docker-image account-service:1.2.0 --name account-service-demo
```

### Application Crashes

Check application logs:

```bash
kubectl logs <pod-name> --previous  # View logs from crashed container
kubectl logs <pod-name> -f          # Follow current logs
```

Common issues:
- Missing environment variables
- Database connection issues (check H2 configuration)
- Port conflicts
- Insufficient memory

### Service Not Accessible

Verify service and endpoints:

```bash
kubectl get svc account-service
kubectl get endpoints account-service
kubectl describe svc account-service
```

### Health Check Failures

Test health endpoint directly:

```bash
kubectl exec -it <pod-name> -- curl http://localhost:8081/actuator/health
```

## Cleanup

### Delete Kubernetes Resources

```bash
kubectl delete -f k8s-local/service.yaml
kubectl delete -f k8s-local/deployment.yaml
kubectl delete -f k8s-local/configmap.yaml
```

### Delete Kind Cluster

```bash
kind delete cluster --name account-service-demo
```

### Remove Docker Image

```bash
docker rmi account-service:1.2.0
```

## Next Steps

This local Kind setup demonstrates the basic Cloud Foundry to Kubernetes migration. For production deployment:

1. **External Services**: Configure PostgreSQL, Redis, and Eureka
2. **Secrets Management**: Use Kubernetes Secrets for sensitive data
3. **Ingress**: Set up ingress controller for external routing
4. **Monitoring**: Enable New Relic and DataDog integrations
5. **Helm Charts**: Consider using Helm for templated deployments (see `helm/` directory)
6. **Namespace Isolation**: Deploy to dedicated namespace (e.g., `demo-apps`)
7. **Security**: Apply pod security policies and network policies

## Reference

- **Cloud Foundry Config**: `manifest.yml`
- **Docker Configuration**: `Dockerfile`
- **Application Config**: `src/main/resources/application.properties`
- **Production K8s Manifests**: See branches with helm chart implementations

## Additional Resources

- [Kind Documentation](https://kind.sigs.k8s.io/)
- [Kubernetes Documentation](https://kubernetes.io/docs/home/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [Cloud Foundry to Kubernetes Migration Guide](https://www.cloudfoundry.org/blog/cloud-foundry-to-kubernetes-migration/)

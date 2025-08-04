# OpenShift Deployment Configuration

This directory contains Kubernetes manifests for deploying the account service to OpenShift.

## Files

- `namespace.yaml` - Creates the demo-apps namespace
- `configmap.yaml` - Non-sensitive configuration values from CF manifest
- `deployment.yaml` - Application deployment with 3 replicas matching CF instances
- `service.yaml` - Service definition for internal communication
- `ingress.yaml` - Ingress configuration for external access

## Secret Configuration

The application requires a Secret named `account-service-secrets` with the following keys:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: account-service-secrets
  namespace: demo-apps
type: Opaque
stringData:
  DATABASE_URL: "jdbc:postgresql://your-postgres-host:5432/accountdb"
  DATABASE_USERNAME: "your-db-username"
  DATABASE_PASSWORD: "your-db-password"
  REDIS_URL: "redis://your-redis-host:6379"
  EUREKA_SERVICE_URL: "http://your-eureka-service:8761/eureka"
  CONFIG_SERVER_URL: "http://your-config-server:8888"
```

Create this secret before deploying the application:

```bash
kubectl create secret generic account-service-secrets \
  --namespace=demo-apps \
  --from-literal=DATABASE_URL="jdbc:postgresql://your-postgres-host:5432/accountdb" \
  --from-literal=DATABASE_USERNAME="your-db-username" \
  --from-literal=DATABASE_PASSWORD="your-db-password" \
  --from-literal=REDIS_URL="redis://your-redis-host:6379" \
  --from-literal=EUREKA_SERVICE_URL="http://your-eureka-service:8761/eureka" \
  --from-literal=CONFIG_SERVER_URL="http://your-config-server:8888"
```

## Deployment

1. Create the namespace: `kubectl apply -f namespace.yaml`
2. Create the secret (see above)
3. Apply the configuration: `kubectl apply -f configmap.yaml`
4. Deploy the application: `kubectl apply -f deployment.yaml`
5. Create the service: `kubectl apply -f service.yaml`
6. Create the ingress: `kubectl apply -f ingress.yaml`

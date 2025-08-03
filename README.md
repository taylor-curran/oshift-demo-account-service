# Account Service

## Artifact Design Thinking

**Platform**: Traditional Cloud Foundry  
**Complexity**: High

### Design Rationale
This represents a core banking microservice with enterprise-grade complexity. The artifacts demonstrate:

- **Traditional CF manifest.yml** with comprehensive application configuration
- **High-availability setup** with 3 instances and proper resource allocation
- **Multiple service bindings** (databases, caches, service registry)
- **Enterprise integration patterns** (Eureka, circuit breakers, config server)
- **Multiple routes** for internal/external API access
- **Production JVM tuning** with specific garbage collection settings

### Key Complexity Features
- Multi-database architecture (primary + cache)
- Service mesh integration (Eureka service discovery)
- Circuit breaker pattern for resilience
- Comprehensive health checks
- Enterprise monitoring (New Relic, DataDog)

## Running and Testing

### Prerequisites
- Java 17 (required by Spring Boot 2.7.8)
- Maven 3.6+

### Environment Setup
```bash
# Ensure Java 17 is installed and set as default
java -version  # Should show version 17.x.x

# If using SDKMAN
sdk install java 17-open
sdk use java 17-open
```

### Build and Test
```bash
# Install dependencies
mvn clean install

# Run tests
mvn test

# Build application
mvn clean package

# Run locally (requires PostgreSQL or will use H2 in-memory database)
mvn spring-boot:run
```

### Test Configuration
The application includes a basic test that verifies the Spring context loads correctly. Tests use an in-memory H2 database and disabled service discovery for isolated testing.

### Cloud Foundry Deployment
```bash
cf push
```

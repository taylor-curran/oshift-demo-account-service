# Account Service

## Artifact Design Thinking

**Platform**: Traditional Cloud Foundry | **Complexity**: High

Core banking microservice demonstrating enterprise-grade CF deployment patterns:

- **Traditional CF manifest.yml** - comprehensive app configuration with HA setup (3 instances)
- **Multiple service bindings** - databases, caches, service registry integration
- **Enterprise patterns** - Eureka discovery, circuit breakers, config server
- **Production JVM tuning** - specific GC settings and resource allocation

### Key Features
- Multi-database architecture (primary + cache)
- Service mesh integration and circuit breaker resilience
- Enterprise monitoring (New Relic, DataDog)

## Quick Start

### Prerequisites
- Java 17, Maven 3.6+

### Run
```bash
# Install dependencies
mvn clean install

# Run tests
mvn test

# Run locally (uses H2 in-memory DB)
mvn spring-boot:run
```

### Deploy
```bash
cf push
```

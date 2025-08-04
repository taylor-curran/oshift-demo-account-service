FROM registry.access.redhat.com/ubi8/openjdk-17:latest

# Set working directory
WORKDIR /deployments

# Copy the jar file
COPY target/account-service-*.jar app.jar

# Expose ports
EXPOSE 8080 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

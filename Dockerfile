FROM registry.access.redhat.com/ubi8/openjdk-17

COPY target/account-service-1.2.0.jar /deployments/app.jar

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "/deployments/app.jar"]

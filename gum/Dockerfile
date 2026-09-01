# Gazelle User Management (Quarkus) 5.0.1 — built from source
FROM eclipse-temurin:25-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
RUN useradd -m -u 1001 gum
COPY --chown=gum quarkus-app/lib/ /deployments/lib/
COPY --chown=gum quarkus-app/*.jar /deployments/
COPY --chown=gum quarkus-app/app/ /deployments/app/
COPY --chown=gum quarkus-app/quarkus/ /deployments/quarkus/
USER gum
EXPOSE 8081
ENTRYPOINT ["java","-Dquarkus.http.host=0.0.0.0","-jar","/deployments/quarkus-run.jar"]

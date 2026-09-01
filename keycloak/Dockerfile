# Keycloak 26.6.4 distribution on a Java 25 JVM (gazelle providers are built for Java 25)
FROM eclipse-temurin:25-jdk-noble
RUN apt-get update && apt-get install -y --no-install-recommends jq curl postgresql-client && rm -rf /var/lib/apt/lists/*
COPY --from=quay.io/keycloak/keycloak:26.6.4 /opt/keycloak /opt/keycloak
ENV KC_DB=postgres
EXPOSE 8180
ENTRYPOINT ["/opt/startup-scripts/entrypoint.sh"]

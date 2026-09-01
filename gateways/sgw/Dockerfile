# Generic Quarkus fast-jar runner (Java 25) — used by service-registry / gateways
FROM eclipse-temurin:25-jre
ARG APPDIR
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
RUN useradd -m -u 1001 quarkus
COPY --chown=quarkus ${APPDIR}/lib/ /deployments/lib/
COPY --chown=quarkus ${APPDIR}/*.jar /deployments/
COPY --chown=quarkus ${APPDIR}/app/ /deployments/app/
COPY --chown=quarkus ${APPDIR}/quarkus/ /deployments/quarkus/
USER quarkus
ENTRYPOINT ["java","-Dquarkus.http.host=0.0.0.0","-jar","/deployments/quarkus-run.jar"]

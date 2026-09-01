# Gazelle Test Management 12.1.0 on JBoss AS 7.2.0.Final (EAP 6.1.0.Alpha) / Java 7 (Zulu)
FROM debian:bullseye-slim

RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip curl graphviz fontconfig procps \
      fonts-arphic-bkai00mp fonts-arphic-bsmi00lp fonts-arphic-gbsn00lp fonts-arphic-gkai00mp fonts-wqy-zenhei \
 && fc-cache -f \
 && rm -rf /var/lib/apt/lists/*

# Azul Zulu OpenJDK 7 (linux x64)
COPY downloads/zulu7.56.0.11-ca-jdk7.0.352-linux_x64.tar.gz /tmp/zulu7.tar.gz
RUN mkdir -p /usr/local/java \
 && tar xzf /tmp/zulu7.tar.gz -C /usr/local/java --strip-components=1 \
 && rm /tmp/zulu7.tar.gz
ENV JAVA_HOME=/usr/local/java
ENV PATH=${JAVA_HOME}/bin:${PATH}

ENV JBOSS_HOME=/usr/local/jboss7

# JBoss AS 7.2.0.Final binary (published by Red Hat as EAP 6.1.0.Alpha)
COPY downloads/jboss-eap-6.1.0.Alpha.zip /tmp/jboss.zip
RUN unzip -q /tmp/jboss.zip -d /usr/local \
 && ln -s /usr/local/jboss-eap-6.1 ${JBOSS_HOME} \
 && rm /tmp/jboss.zip

# PostgreSQL JDBC driver as a static module (what Gazelle's packaged JBoss provided)
COPY downloads/postgresql-42.2.1.jre7.jar ${JBOSS_HOME}/modules/org/postgresql/main/
COPY jboss/pg-module.xml ${JBOSS_HOME}/modules/org/postgresql/main/module.xml

# JavaMail 1.4.5 -> 1.6.2 (same javax.mail API, last release supporting Java 7):
# 1.4.x's SocketFetcher enables every SUPPORTED TLS protocol (incl. policy-disabled
# SSLv3/TLSv1) which breaks STARTTLS on 7u352 with "No appropriate protocol";
# 1.6.2 keeps the JVM defaults (TLSv1.2) and works against modern SMTP servers.
COPY downloads/javax.mail-1.6.2.jar ${JBOSS_HOME}/modules/system/layers/base/javax/mail/api/main/
RUN sed -i 's|<resource-root path="mail-1.4.5.jar"/>|<resource-root path="javax.mail-1.6.2.jar"/>|' \
      ${JBOSS_HOME}/modules/system/layers/base/javax/mail/api/main/module.xml \
 && rm ${JBOSS_HOME}/modules/system/layers/base/javax/mail/api/main/mail-1.4.5.jar

# Configure standalone.xml: postgresql driver, TestManagementDS datasource, scanner timeout
COPY jboss/configure-standalone.sh /tmp/configure-standalone.sh
RUN bash /tmp/configure-standalone.sh ${JBOSS_HOME}/standalone/configuration/standalone.xml

# /opt/gazelle runtime tree (reports, fonts, xsl from the -dist.zip)
COPY downloads/gazelle-tm-ear-12.1.0-dist.zip /tmp/dist.zip
RUN unzip -q -o /tmp/dist.zip -d /tmp/dist \
 && mkdir -p /opt/gazelle \
 && cp -r /tmp/dist/opt/gazelle/* /opt/gazelle/ \
 && mkdir -p /opt/gazelle/cas /opt/gazelle/cert /opt/gazelle/fileCache /opt/gazelle/tmp \
             /opt/gazelle/backup /opt/gazelle/xsd /opt/gazelle/data \
 && rm -rf /tmp/dist /tmp/dist.zip
COPY jboss/gazelle-tm.properties /opt/gazelle/cas/gazelle-tm.properties
# Keep a pristine copy: /opt/gazelle is a volume, entrypoint seeds it on first run
RUN cp -r /opt/gazelle /opt/gazelle-dist

# Gazelle logo served from the welcome root (used as testing-session logo)
COPY jboss/GAZELLE_LOGO.gif /usr/local/jboss-eap-6.1/welcome-content/GAZELLE_LOGO.gif

# The application EAR
# Patched EAR: initialize-in-order + war last — fixes a Seam-init vs EJB-start
# deadlock that a fast database (native PG17) makes deterministic
COPY downloads/gazelle-tm-ear-12.1.0-patched.ear ${JBOSS_HOME}/standalone/deployments/gazelle-tm.ear

COPY jboss/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080 9990
ENTRYPOINT ["/entrypoint.sh"]

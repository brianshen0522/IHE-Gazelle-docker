# Service Registry Client

This library is intended to be used in any Gazelle application or service that needs to register
itself in the Service Registry, such as Simulation Service or Validation Service that wants to
integrate in Gazelle.

<!-- TOC -->
* [Service Registry Client](#service-registry-client)
  * [Requirements](#requirements)
  * [Installation](#installation)
  * [Service Registration](#service-registration)
    * [Service Metadata](#service-metadata)
    * [Registration Job](#registration-job)
  * [Service Lookup](#service-lookup)
<!-- TOC -->

## Requirements

This library uses JDK 21+, Jakarta EE 10, and Microprofile 5.0 APIs. It is intended to be used with
Quarkus 3.15+.

## Installation

To use this library, add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>com.gazelle</groupId>
    <artifactId>service-registry-client</artifactId>
    <version>${serviceregistry.version}</version>
</dependency>
```

It also requires the following Gazelle dependencies:

```xml

<dependencies>
    <dependency>
        <groupId>net.ihe.gazelle</groupId>
        <artifactId>lang</artifactId>
        <!-- version 2.0.0 or higher -->
    </dependency>
    <dependency>
        <groupId>net.ihe.gazelle</groupId>
        <artifactId>service-metadata-jaxrs-api</artifactId>
        <!-- use framework bom 2.0.1 for version management or higher -->
    </dependency>
    <dependency>
        <groupId>net.ihe.gazelle</groupId>
        <artifactId>search-client</artifactId>
        <!-- version 1.1.0 or higher -->
    </dependency>
</dependencies>
```

It will also requires implementation of the following Jakarta and Microprofile APIs:

```xml

<dependencies>
    <dependency>
        <groupId>jakarta.websocket</groupId>
        <artifactId>jakarta.websocket-client-api</artifactId>
        <scope>provided</scope>
        <version>2.1.1</version>
        <!-- websocket-client-api is not provided in Quarkus BOM, pay attention to align the version with jakarta.websocket-api -->
    </dependency>
    <dependency>
        <groupId>jakarta.ws.rs</groupId>
        <artifactId>jakarta.ws.rs-api</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.eclipse.microprofile.openapi</groupId>
        <artifactId>microprofile-openapi-api</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.eclipse.microprofile.config</groupId>
        <artifactId>microprofile-config-api</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-core</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Importing the following **Quarkus** extensions should be enough to cover the above dependencies:

```xml

<dependencies>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-websockets-client</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-arc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-rest-jackson</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-rest</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-smallrye-openapi</artifactId>
    </dependency>
</dependencies>
```

Import m2m client to ensure to authenticate your HTTP or WebSocket requests to the Service Registry:

```xml
<dependency>
    <groupId>net.ihe.gazelle</groupId>
    <artifactId>m2m-client</artifactId>
</dependency>
```  

## Service Registration

### Service Metadata

In order to perform the service registration, **Service Registry Client** requires your application
to provide metadata about it.

Please follow
the [Gazelle Service Metadata documentation](https://gitlab.inria.fr/gazelle/public/framework/framework/-/blob/master/service-metadata/README.md?ref_type=heads)
to implement the required metadata.

_Note that the
entire [chapter \$2](https://gitlab.inria.fr/gazelle/public/framework/framework/-/blob/master/service-metadata/README.md?ref_type=heads#2-using-the-default-implementation)
about default implementation must be done, and it is recommended to also expose the `/metadata`
endpoint as described
in [chapter \$4 and \$5](https://gitlab.inria.fr/gazelle/public/framework/framework/-/blob/master/service-metadata/README.md?ref_type=heads#4-service-metadata-rest-api)_

### Registration Job

The `RegistrationJob` will automatically register your service in the Service Registry at startup. It
requires the following configuration to run:

| Java property                                     | Env. variable                                     | Description                                                                                                | Default value |
|---------------------------------------------------|---------------------------------------------------|------------------------------------------------------------------------------------------------------------|---------------|
| `gzl.service.registry.enabled`                    | `GZL_SERVICE_REGISTRY_ENABLED`                    | Whether the service should register itself in the Service Registry.                                        | `true`        |
| `gzl.service.registry.url`                        | `GZL_SERVICE_REGISTRY_URL`                        | The URL of the Service Registry. Must be provided if registration enabled.                                 | -             |
| `gzl.service.registry.keepalive.interval.seconds` | `GZL_SERVICE_REGISTRY_KEEPALIVE_INTERVAL_SECONDS` | The interval in seconds between 2 registration messages for keeping the service registry connection alive. | `60`          |

Once started, the client will keep the websocket connection alive with service-registry by sending
registration message every `keepalive.interval.seconds` seconds. That allows **Service-Registry** to
monitor the health state of registered services.

_Note that the "Service Registration API" will automatically be added to the list of **consumed
interfaces** in your application metadata._

Websocket Registration API is protected by **m2m authentication**, so make sure to properly configure your application with following properties:

| Java Property                            | ENV Variable                             | Description                                             | Example Value              |
|------------------------------------------|------------------------------------------|---------------------------------------------------------|----------------------------|
| `gzl.sso.url`                            | `GZL_SSO_URL`                            | SSO Server URL                                          | ${SCHEME}://${FQDN}/auth   |
| `gzl.sso.admin.user`                     | `GZL_SSO_ADMIN_USER`                     | SSO Admin username                                      | gazelle-clients-admin      |
| `gzl.sso.admin.password`                 | `GZL_SSO_ADMIN_PASSWORD`                 | SSO Admin password                                      | changeit                   |
| `gzl.m2m.client.secret`                  | `GZL_M2M_CLIENT_SECRET`                  | Secret of registered client in SSO                      | secret                     |
| `gzl.service.k8s.id`                     | `GZL_SERVICE_K8S_ID`                     | Unique identifier for client name-instanceId-repliquaId | service-registry-00001-001 |
| `gzl.m2m.registration.anonymous.enabled` | `GZL_M2M_REGISTRATION_ANONYMOUS_ENABLED` | Enable anonymous registration                           | false                      |

For more information, refer to [Framework -> Security Parent -> m2m-client documentation](https://gitlab.inria.fr/gazelle/public/framework/framework/-/blob/master/security-parent/m2m-client/README.md)

## Service Lookup

Query to the Service Registry can be done using the `ServiceLookup` class. Here is an example:

```java
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.search.api.Sort.Order;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.client.business.ServiceLookupClient;
import net.ihe.gazelle.serviceregistry.client.technical.rest.ServiceLookupClientImpl;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.AVAILABLE;
import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.UNKNOWN;

public class LookupExample {

   @ConfigProperty(name = "gzl.service.registry.url", defaultValue = "http://localhost:8097/service-registry")
   String serviceRegistryUrl;

   public void lookupService() throws InterruptedException {
      ServiceLookupClient serviceLookup = new ServiceLookupClientImpl(serviceRegistryUrl);

      // Search for the 10th first services providing the Validation API and order by name.
      SearchResult<DeployedService> searchResult = serviceLookup.search(new SearchQuery<>(
            new ServiceSearchCriteria()
                  .setProvidedInterface("Gazelle Validation API")
                  .setStatus(AVAILABLE, UNKNOWN),
            new Range(0, 10),
            List.of(new Sort("name", Order.ASCENDING))
      ));

      // Process the results
      for (DeployedService service : searchResult.objects()) {
         System.out.println("Found service: " + service.getName());
      }
   }

}
```

API are protected some ensure to configure your application with proper authentication properties to get a valid token.

Note that you should add **Service Lookup API** to the list of **consumed interfaces** in your
application metadata.

To do so, create a concretion of the abstract class `AbstractConsumedServiceLookupIdentifier` and
declare whether it is required to operate:

```java
import net.ihe.gazelle.serviceregistry.client.technical.rest.AbstractConsumedServiceLookupIdentifier;

public class ConsumedServiceLookupIdentifier extends AbstractConsumedServiceLookupIdentifier {

   public ConsumedServiceLookupIdentifier() {
      // true if the service lookup is required by your application to operate, false if it is optional.
      super(true);
   }

}
```

Then, register it as an SPI service in the file
`META-INF/services/net.ihe.gazelle.servicemetadata.api.technical.ConsumedInterfaceIdentifier`

with the following content:

```
your.package.ConsumedServiceLookupIdentifier
```

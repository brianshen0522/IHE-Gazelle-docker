# Keycloak-provider

<!-- TOC -->
* [Keycloak-provider](#keycloak-provider)
  * [Introduction](#introduction)
  * [Migration of realm configurations](#migration-of-realm-configurations)
  * [Gazelle user storage provider](#gazelle-user-storage-provider)
  * [Event listeners](#event-listeners)
  * [External Identity Provider](#external-identity-provider)
    * [Authenticator & mappers](#authenticator--mappers)
  * [Upgrade Keycloak version](#upgrade-keycloak-version)
  * [References](#references)
<!-- TOC -->

## Introduction

In this module, you will find the Keycloak provider implementation.

## Migration of realm configurations

Gazelle doesn't manage natively migration of realm configurations. As we need to maintain different instances of Gazelle in potential different versions of GUM. 
We need to be able to migrate realm configuration from one version of GUM to another.

To do this, we implemented our own migration system based on **Keycloak client CLI**.

For more information, see the [Realm configuration migration](./realm-migration/README.md).

## Gazelle user storage provider

If your users are not managed by Keycloak, you have to implement a user storage provider. For that, you will need a factory 
and a provider.

In this project, our factory implements the *UserStorageProviderFactory\<GazelleUserStorageProvider>*. In this class, we
create the configuration metadata. For any service or DAO needed, we initiate them in the **init** method as this method
called only once.

The **getId** method is used for the display of the provider name in the Keycloak administration console.

For the provider, you need to implement the *UserStorageProvider* interface, and after you can implement other interfaces
depending on your needs. For example, implementing the *UserLookupProvider* interface will allow users outside keycloak
to log in. For more information, see the 
[Keycloak documentation](https://www.keycloak.org/docs/latest/server_development/index.html#_provider_capability_interfaces).

The user storage provider uses the UserModel class that is a representation of a user in Keycloak. We create an adapter
that extends the *AbstractUserAdapterFederatedStorage* class. In this adapter, we use a model that is the representation
from our point of view and map its attributes and methods to the adapter to make it compatible with keycloak.

## Event listeners

If you need to do specific actions after an event is fired, you will need to implement a new Event Listener.

To do that, you will need to create a factory and a provider. 

* For the factory, you should create a class that extends [*GazelleEventListenerProviderFactory*](./src/main/java/net/ihe/gazelle/keycloak/provider/interlay/events/GazelleEventListenerProviderFactory.java).
This class implements *EventListenerProviderFactory* with empty methods.
For your custom factory, you must at least override the **create** method, which will create your *CustomEventListenerProvider*,
and the **getId** method, which must return a String that is the name displayed in Keycloak.

* Then for the provider, you should create a class that extends [*GazelleEventListenerProvider*](./src/main/java/net/ihe/gazelle/keycloak/provider/interlay/events/GazelleEventListenerProvider.java ).
This class implements *EventListenerProvider* with two more methods to get the *KeycloakSession* and the *RealmModel*.
These classes can be useful if you want to retrieve the user that fired the event. 
The provider has two **onEvent** methods, one for basic event (ex login, logout, login error, ...) and one for admin event, which are 
events fired in the administration console (ex create, delete, ...). 
Your provider should at least override one **onEvent** method.

Finally, remember to add your Event Factory in the [org.keycloak.events.EventListenerProviderFactory](src/main/resources/META-INF/services/org.keycloak.events.EventListenerProviderFactory) file.

## External Identity Provider

It's possible to add an external identity provider to Keycloak. For that, you need to do it from the Keycloak administration console.

After you have added the identity provider, you can add a mapper to map the attributes from the external identity provider to the Keycloak user.

You can also customize some authentication flow with custom authenticators.

Finally, you can export the idp configurations as a JSON and save it to be able to create specific Keycloak migrations.

> [!warning] :warning: Warning
> For the moment there is no protection mechanism to block external users. So the external idp are completely trusted. 
> Avoid adding open idp like Google or Facebook where anyone can create an account. This would give access to Gazelle to anyone.

### Authenticator & mappers

For authentication delegation, we implemented custom authenticators. These authenticators are added to the following authentication flow : 

- **Browser** : The authenticator calls at each browser login
- **First broker login** : The authenticator calls at the first broker login
- **Reset credentials** : The authenticator calls at the reset credentials

Same for attribute mapping, there are custom available mappers. These mappers must be added manually to the identity provider.

For more information, see the [Delegation configuration](../../docs/installation-manual/delegation-configuration.md).

## Upgrade Keycloak version

> [!warning] :warning: Warning
> Upgrade Keycloak version is a critical operation. It must be done carefully and tested before being deployed in production.

We depend on other projects that need to follow the Keycloak version :

- **Keycloak-protocol-cas** : https://github.com/jacekkow/keycloak-protocol-cas
- **Keycloak-config-cli** : https://github.com/adorsys/keycloak-config-cli
- **Keycloak-admin-client** : (Integrated in Keycloak repo): https://github.com/keycloak/keycloak/tree/main/js/libs/keycloak-admin-client
- **Keycloak-themes** : (Integrated in Keycloak repo): https://github.com/keycloak/keycloak/tree/main/themes

To upgrade Keycloak's version used by the project, you must follow the following steps :

- Update the keycloak.version property in the pom.xml file
- Upload manually the released jar file in the nexus repository (https://nexus.ihe-europe.net/nexus/repository/maven-releases/)

Example of command executed to manually publish the jar file in the nexus repository :
```bash
export ARTIFACT_RELEASE_REPOSITORY_USER=username
export ARTIFACT_RELEASE_REPOSITORY_PASS=password
export KEYCLOAK_PROTOCOL_CAS_VERSION=26.6.3
wget https://github.com/jacekkow/keycloak-protocol-cas/releases/download/${KEYCLOAK_PROTOCOL_CAS_VERSION}/keycloak-protocol-cas-${KEYCLOAK_PROTOCOL_CAS_VERSION}.jar
mvn -s ./settings.xml deploy:deploy-file -DgroupId=org.keycloak -DartifactId=keycloak-protocol-cas -Dversion=${KEYCLOAK_PROTOCOL_CAS_VERSION} -Dpackaging=jar -Dfile=./keycloak-protocol-cas-${KEYCLOAK_PROTOCOL_CAS_VERSION}.jar -DgeneratePom=true  -DrepositoryId=nexus-releases -Durl=https://nexus.ihe-catalyst.net/repository/releases/
```

> [!warning] :warning: Warning
> This step is deprecated since Keycloak version 26.X.X

For our custom themes, refresh the custom ftl page (login, reset-password, etc.) with the new version from keycloak-sources

To do that, download the Keycloak jar:

```shell
wget https://github.com/keycloak/keycloak/releases/download/$KEYCLOAK_VERSION/keycloak-$KEYCLOAK_VERSION.tar.gz
```

Open the tar.gz file and go to lib/lib/main/org.keycloak-admin-ui-$KEYCLOAK_VERSION.jar open it, open index.ftl and find imports
It should look like this:

```html
    <script type="module" crossorigin src="${resourceUrl}/assets/index-<INDEX_ID>.js"></script>
    <link rel="stylesheet" crossorigin href="${resourceUrl}/assets/index-<CSS_ID>.css">
```

Copy INDEX_ID and CSS_ID and replace them in the file [index.ftl](../keycloak-theme/src/main/resources/theme/gazelle/admin/index.ftl)

Update the based docker image in the Dockerfile of the keycloak-provider (FROM rg.fr-par.scw.cloud/tools/keycloak:${keycloak.version})

## References

[Keycloak User Storage SPI](https://www.keycloak.org/docs/latest/server_development/index.html#_user-storage-spi)



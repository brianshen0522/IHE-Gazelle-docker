# GUM Keycloak Resources

Some Keycloak resources are specific for a context / a customer.

These resources are separated by context and they are in another repository. 

These projects will generate jars that will be provided to Keycloak at runtime. This allows us to respond to customer specific needs without release a new version of GUM.

See GUM documentation about [External Jar Provider](https://gitlab.inria.fr/gazelle/applications/user-management).

## List of existing resources

Here you can find a list of external resources that are available :

- **ans-keycloak-resources** [https://gitlab.inria.fr/gazelle/private/ans/ans-keycloak-resources](https://gitlab.inria.fr/gazelle/private/ans/ans-keycloak-resources)
- **italian-keycloak-resources** [https://gitlab.inria.fr/gazelle/private/pnt/italian-keycloak-resources](https://gitlab.inria.fr/gazelle/private/pnt/italian-keycloak-resources)

## Add a new project

If you need to create a new project. You can copy the following files from an existing project :

- pom.xml
- .gitignore
- .gitlab-ci.yml
- settings.xml

> :warning: **WARNING** Do not forget to rename the occurrences of ans-keycloak-resources to xxxx-keycloak-resources.

Then, put your custom implementations in the `src/main/java/net/ihe/gazelle/...`.

## Overload Keycloak themes

This theme is an overload of the theme of gazelle implemented in the [gazelle user management project](https://gitlab.inria.fr/gazelle/applications/user-management).

Currently, the theme is overloaded by adding all the keycloak-theme jar from gum in the shaded jar for italy (see `maven-shade-plugin`).

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>${maven.shade.plugin.version}</version>
    <executions>
        <execution>
            [..]
            <configuration>
                <artifactSet>
                    <includes>
                        <include>net.ihe.gazelle:italian-keycloak-resources</include>
                        <include>net.ihe.gazelle:keycloak-theme</include>
                    </includes>
                </artifactSet>
                <createDependencyReducedPom>false</createDependencyReducedPom>
                [..]
            </configuration>
        </execution>
    </executions>
</plugin>
```

Then, when keycloak is starting, the theme is loaded from the first jar in the classpath. The jar `ìtalian-keycloak-resources` is alphabetically before the `keycloak-provider-shaded` jar so this is its content that is loaded.

## Potential issues

There are some risks of problem if the name of the jars changed or if the order of the jars in the classpath changed.

Also, it's not possible to have 2 different external IDPs with custom themes because themes are realm scoped and not idp scoped. So all the idp are sharing the same theme.

It's working for one custom idp because we include all the gazelle theme in the jar.

We import `italian.css` directly in the `.ftl` file in order to avoid copying all the theme.properties from the gazelle theme.

```html
<link href="${url.resourcesPath}/css/italian.css" rel="stylesheet" />
```

Regarding translations, we use maven-shade-plugin to merge all the `messages_XX.properties` files in the jar with a transformer, example below :

```xml
<transformer implementation="org.apache.maven.plugins.shade.resource.properties.PropertiesTransformer">
    <resource>theme/gazelle/login/messages/messages_en.properties</resource>
    <ordinalKey>ordinal</ordinalKey>
</transformer>
```
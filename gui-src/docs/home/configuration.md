# Home configuration

Gazelle-home is a highly configurable module. There a 2 point of configuration that are important to check.

## Environment variables

| Variable name                 | Description                                                                | Example                          |
|-------------------------------|----------------------------------------------------------------------------|----------------------------------|
| GZL_HOME_CONFIGURATION_FOLDER | Folder where the configuration file config.json must be present at runtime | /opt/gazelle-user-interface/home |

## Homepage configuration

To work properly, the application need a configuration file named `config.json`.

This configuration must be placed at the `${GZL_HOME_CONFIGURATION_FOLDER}/config.json`. If no configuration file is found at startup, a default one is retrieved.

You can find an **example** of this config file in resources of the source project [`/resources/home/gazelle-home-configuration.json`](https://gitlab.inria.fr/gazelle/private/kereval/gazelle-user-interface/-/blob/master/resources/home/gazelle-home-configuration.json).

## Navigation bar configuration

The navigation bar can be configured with a json file named `navigation-bar-configuration.json`.

This configuration must be placed at the `${GZL_HOME_CONFIGURATION_FOLDER}/navigation-bar-configuration.json`. If no configuration file is found at startup, a default one is retrieved.

You can find an **example** of this config file in resources of the source project [`/resources/home/navigation-bar-configuration.json`](https://gitlab.inria.fr/gazelle/private/kereval/gazelle-user-interface/-/blob/master/resources/home/navigation-bar-configuration.json).
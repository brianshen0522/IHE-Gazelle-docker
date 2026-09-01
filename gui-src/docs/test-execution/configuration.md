# Test execution configuration

## Environment variables

| Variable name                    | Description                           | Example                                               |
|----------------------------------|---------------------------------------|-------------------------------------------------------|
| GZL_TEST_CASE_FOLDER             | Path to JSON test cases               | /opt/gazelle-user-interface/test-execution/test-case/ |
| GZL_TEST_EXECUTION_URL           | Url to the REST API of test execution | https://gazelle.ihe.net/test-execution                |
| GZL_TEST_EXECUTION_WEBSOCKET_URL | Websocket endpoint                    | wss://preprod.ihe-europe.net/test-execution           |
| GZL_MAESTRO_URL                  | Maestro http url                      | https://gazelle.ihe.net/maestro                       |
| GZL_MAESTRO_WEBSOCKET_URL        | Maestro websocket endpoint            | wss://gazelle.ihe.net/maestro                         |
| GZL_TEST_MODEL_REPOSITORY_URL    | Test model repository endpoint        | https://gazelle.ihe.net/test-model-repository         |

## Test case configuration

Test execution needs preloaded test cases to work properly. The test cases are described in JSON files. 

The path to the folder where the test cases are stored is defined by the `GZL_TEST_CASE_FOLDER` environment variable.

You can find an **example** of this config file in resources of the source project [`/resources/test-execution/test-validation-example.json`](https://gitlab.inria.fr/gazelle/private/kereval/gazelle-user-interface/-/blob/epic/SimulationAndExecution/resources//test-execution/test-validation-example.json).
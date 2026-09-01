# Maestro

Maestro is the Gazelle test execution engine. It runs test suites and test runs by orchestrating step implementations (validation, simulation, ITB, assertion, user interaction), then returns a consolidated test report.

This README focuses on the missing operational documentation needed to run Maestro correctly with ITB and TM.

## Prerequisites

- Java 21
- Maven 3.8+
- Reachable dependencies: SSO, Service Registry, Datahouse (optional), ITB platform

## Build and Run

```bash
./mvnw clean verify
./mvnw -pl maestro-quarkus quarkus:dev
```

Default local URL:

- Swagger UI: `http://localhost:8150/maestro/swagger-ui`
- OpenAPI: `http://localhost:8150/maestro/openapi`

## REST TestRun Examples by Step

All examples below use:

- Endpoint: `POST {GZL_MAESTRO_URL}/v1/test/run`
- Content-Type: `application/json`
- Authorization: Bearer token from your configured SSO

Example command template:

```bash
curl -X POST "http://localhost:8150/maestro/v1/test/run?persist=false" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  --data @testrun.json
```

### Assertion Step (`ASSERT_EQUALS`)

Use this step when you want to check strict equality between two strings (`expected` and `actual`).

```json
{
  "testRunId": "assert-equals-run",
  "systemsUnderTest": [],
  "test": {
    "id": "assert-equals-test",
    "name": "Assert Equals Example",
    "steps": [
      {
        "id": "assert-equals-step",
        "name": "Assert equals step",
        "type": "ASSERT_EQUALS",
        "properties": [
          { "name": "expected", "type": "STRING", "value": "OK" },
          { "name": "actual", "type": "STRING", "value": "OK" }
        ]
      }
    ]
  }
}
```

### Assertion Step (`ASSERT_CONTAINS`)

Use this step when you want to check that `actual` contains `expected` as a substring.

```json
{
  "testRunId": "assert-contains-run",
  "systemsUnderTest": [],
  "test": {
    "id": "assert-contains-test",
    "name": "Assert Contains Example",
    "steps": [
      {
        "id": "assert-contains-step",
        "name": "Assert contains step",
        "type": "ASSERT_CONTAINS",
        "properties": [
          { "name": "expected", "type": "STRING", "value": "IHE" },
          { "name": "actual", "type": "STRING", "value": "IHE Gazelle Maestro" }
        ]
      }
    ]
  }
}
```

### User Interaction Step (`USER_INTERACTION`)

Use this step when a manual confirmation is needed during the execution flow.

```json
{
  "testRunId": "user-interaction-run",
  "systemsUnderTest": [],
  "test": {
    "id": "user-interaction-test",
    "name": "User Interaction Example",
    "steps": [
      {
        "id": "user-interaction-step",
        "name": "User interaction step",
        "type": "USER_INTERACTION",
        "timeout": 30000,
        "properties": [
          { "name": "interactionTitle", "type": "STRING", "value": "Manual verification required" },
          { "name": "message", "type": "STRING", "value": "Please verify patient demographics on UI and acknowledge." }
        ]
      }
    ]
  }
}
```

### Validation Step (`VALIDATION`)

Use this step to call a configured validation service with a profile and a payload to validate.

```json
{
  "testRunId": "validation-run",
  "systemsUnderTest": [],
  "test": {
    "id": "validation-test",
    "name": "Validation Example",
    "steps": [
      {
        "id": "validation-step",
        "name": "Validation step",
        "type": "VALIDATION",
        "properties": [
          { "name": "validationService", "type": "STRING", "value": "Matchbox" },
          { "name": "validationProfile", "type": "STRING", "value": "sample-profile-id" },
          { "name": "contentToValidate", "type": "BYTE_ARRAY", "value": "${inputXml}" }
        ]
      }
    ]
  },
  "inputs": [
    {
      "name": "inputXml",
      "type": "BYTE_ARRAY",
      "value": "PHJvb3Q+T0s8L3Jvb3Q+"
    }
  ]
}
```

### Simulation Step (`SIMULATION`)

Use this step to execute a sequence on a simulation service.

```json
{
  "testRunId": "simulation-run",
  "systemsUnderTest": [],
  "test": {
    "id": "simulation-test",
    "name": "Simulation Example",
    "steps": [
      {
        "id": "simulation-step",
        "name": "Simulation step",
        "type": "SIMULATION",
        "timeout": 60000,
        "properties": [
          { "name": "simulationService", "type": "STRING", "value": "MySimulationService" },
          { "name": "sequenceId", "type": "STRING", "value": "MySequenceId" }
        ]
      }
    ]
  }
}
```

If your selected simulation sequence defines required parameters, add them as additional step properties with the expected types.

## ITB Section

Use ITB when the test case must be executed on an external ITB platform.

- Required properties in the step:
- `testCase`
- `actorID`
- `systemID`
- Optional properties:
- `executionMode`
- `itbPayloadMode`

### ITB step properties

- `testCase`: ITB test case identifier to execute.
- `actorID`: ITB actor identifier.
- `systemID`: ITB system identifier.
- `executionMode`: execution strategy for the ITB call.
  Accepted values: `ASYNC` and `SYNC`.
  Default: `ASYNC`.
- `itbPayloadMode`: how Maestro maps business inputs to the ITB `inputMapping`.
  Accepted values: `FLAT` and `USER_INPUT_MAP`.
  Default: `FLAT`.

### Synchronous ITB execution

Set `executionMode` to `SYNC` when Maestro must wait for the ITB execution to complete during the step execution.

- In `SYNC`, Maestro sends `waitForCompletion=true` and `maximumWaitTime=<step timeout>` to ITB.
- The step `timeout` must be greater than `0`, otherwise the ITB step fails immediately.
- The step completes with the ITB report, logs and PDF report enrichment collected during the same execution.
- The Maestro `/itb/report` callback is not used to complete the step in this mode.

Use `ASYNC` when ITB should start the session and complete it later through the callback flow.

### ITB payload mapping modes

All ITB step properties other than `testCase`, `actorID`, `systemID`, `executionMode` and `itbPayloadMode` are sent to ITB as business inputs.

- `FLAT`: one ITB `inputMapping` entry is created per business property.
- `USER_INPUT_MAP`: Maestro creates a single ITB input named `userInput` with type `map`, and places the business properties inside `userInput.item[]`.

Use `USER_INPUT_MAP` for ITB test sessions that expect a grouped `userInput` object instead of flat top-level inputs.

Example `TestRun` payload for ITB:

```json
{
  "testRunId": "itb-run",
  "systemsUnderTest": [],
  "test": {
    "id": "itb-test",
    "name": "ITB Example",
    "steps": [
      {
        "id": "itb-step",
        "name": "ITB step",
        "type": "ITB",
        "timeout": 120000,
        "properties": [
          { "name": "testCase", "type": "STRING", "value": "018_01_01" },
          { "name": "actorID", "type": "STRING", "value": "CE72C162X6307X41A8X8AD4X9498BDAE7220" },
          { "name": "systemID", "type": "STRING", "value": "69461385X812CX4CFDX83A0X3D6D54F70FB2" },
          { "name": "document", "type": "BYTE_ARRAY", "value": "${inputFile}" },
          { "name": "documentName", "type": "STRING", "value": "sample.xml" }
        ]
      }
    ]
  },
  "inputs": [
    {
      "name": "inputFile",
      "type": "BYTE_ARRAY",
      "value": "PHNhbXBsZT5vazwvc2FtcGxlPg=="
    }
  ]
}
```

### Example with `executionMode=SYNC` and `itbPayloadMode=USER_INPUT_MAP`

Use this combination when the ITB test must finish during the Maestro step and expects one grouped `userInput` map.

```json
{
  "testRunId": "itb-sync-run",
  "systemsUnderTest": [],
  "test": {
    "id": "itb-sync-test",
    "name": "ITB Sync Example",
    "steps": [
      {
        "id": "itb-sync-step",
        "name": "ITB synchronous step",
        "type": "ITB",
        "timeout": 120000,
        "properties": [
          { "name": "testCase", "type": "STRING", "value": "testCase_uploadLabReport" },
          { "name": "actorID", "type": "STRING", "value": "37A313E9X1378X4380X9A7BXBEE7846B8A7B" },
          { "name": "systemID", "type": "STRING", "value": "F0B93C0FX8D8DX48FCXBAE0X0084CDC34200" },
          { "name": "executionMode", "type": "STRING", "value": "SYNC" },
          { "name": "itbPayloadMode", "type": "STRING", "value": "USER_INPUT_MAP" },
          { "name": "content", "type": "BYTE_ARRAY", "value": "${inputFile}" },
          { "name": "uploadedFileName", "type": "STRING", "value": "1-1234-W7.json" }
        ]
      }
    ]
  }
}
```

With this mode, Maestro builds the ITB `inputMapping` like this:

```json
[
  {
    "testCase": ["testCase_uploadLabReport"],
    "input": {
      "name": "userInput",
      "type": "map",
      "item": [
        {
          "name": "content",
          "type": "binary",
          "embeddingMethod": "BASE64",
          "value": "<base64 content>"
        },
        {
          "name": "uploadedFileName",
          "value": "1-1234-W7.json"
        }
      ]
    }
  }
]
```

ITB callback expected by Maestro:

- `POST {GZL_MAESTRO_URL}/itb/report`
- Local example: `POST http://localhost:8150/maestro/itb/report`
- Required for `executionMode=ASYNC`
- Not used to complete the step for `executionMode=SYNC`

## Configuration Checklist for ITB + TM

Use this order to avoid integration issues.

### 1) Add ITB organization key

Maestro uses property `itb.api.key` and sends it as HTTP header `ITB_API_KEY` to ITB.

- Recommended env var: `ITB_API_KEY` (maps to `itb.api.key`)
- If your deployment naming uses `ITB_API_ORGANIZATION_KEY`, map it to `itb.api.key` in your deployment config.
- Also set `ITB_API_URL` (maps to `itb.api.url`)

### 2) Add services in Service Registry

For ITB specifically:

- Service name: `ITB`
- Consumed interface name: `ITB API`
- Interface version: `1.0.0`
- Binding type: HTTP REST
- Service URL: your ITB base URL

### 3) Configure ITB platform

- Configure ITB to reach Maestro callback endpoint:
- `POST {GZL_MAESTRO_URL}/itb/report`

### 4) Configure TM to call Maestro

- `POST {GZL_MAESTRO_URL}/v1/test-suite/run`
- `POST {GZL_MAESTRO_URL}/v1/test/run`
- API is protected (OIDC/JWT), so TM must use a valid token.

### 5) Configure Maestro runtime variables

```bash
export GZL_MAESTRO_URL="http://localhost:8150/maestro"
export GZL_SERVICE_REGISTRY_URL="http://localhost:8088/service-registry"
export GZL_SERVICE_REGISTRY_ENABLED="true"

export ITB_API_URL="https://your-itb-host"
export ITB_API_KEY="your-itb-organization-key"

export GZL_SSO_URL="http://localhost/auth"
export GZL_SSO_REALM="gazelle"
export GZL_SSO_ADMIN_USER="gazelle-clients-admin"
export GZL_SSO_ADMIN_PASSWORD="gazelle"
export GZL_M2M_CLIENT_SECRET="change-me"

export DATAHOUSE_URL="http://localhost/datahouse/rest/v1"
```

## Minimal End-to-End Validation

1. Start Maestro.
2. Check Swagger at `/maestro/swagger-ui`.
3. Execute one test suite through `/v1/test-suite/run`.
4. Run one ITB step with `executionMode=SYNC` and confirm the ITB report is returned in the step outputs.
5. Run one ITB step with `executionMode=ASYNC` and verify the callback arrives on `/maestro/itb/report`.
6. If the target ITB session expects grouped business inputs, use `itbPayloadMode=USER_INPUT_MAP`; otherwise keep the default `FLAT`.

## Additional Documentation

- [Features](documentation/features.md)
- [Architecture](ARCHITECTURE.md)

## License

```
Copyright 2022-2026 IHE International

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

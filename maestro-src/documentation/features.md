# Features Maestro 1.0.0

## Description

Maestro is an Engine for execution of automated step from a test.
When we receive a request following the syntax described bellow, Maestro will start its execution sequentially.
When a step execution end, it looks for next step in step list.
If we are at the last step from step list, it goes for next testRun.
If we are at the last testRun, Maestro build a testReport containing testRun Report and step report.
At the end of execution, Maestro return a TestReport containing sub report.

## Input data

The Maestro rest API endpoint is at https://FQDN/maestro/testRun/run?callback=callbackUri
This endpoint can run Synchronously or Asynchronously depending on the presence of a callback URI as a query parameter :
* If the callback query parameter is not set, this will run synchronously and directly send the Test report as a response.
* If the callback query parameter is set, this will run asynchronously and will send the Test report to the specified callback URI later on.

It consumes a JsonArray for inputs following this syntax :

```json
[
  {
    "testSetId": "",
    "test": {
      "id": "1",
      "name": "name1",
      "steps": [
        {
          "type": "VALIDATION",
          "name": "step1",
          "properties": [
            {
              "name": "validationService",
              "value": "matchbox",
              "type": "string"
            },
            {
              "name": "validationProfile",
              "value": "parameters",
              "type": "string"
            },
            {
              "name": "contentToValidate",
              "value": "${inputFile1}",
              "type": "string"
            }
          ]
        }
      ]
    },
    "inputs": [
      {
        "name": "inputFile1",
        "value": "ihfeorhqifrghqpervgh",
        "type": "byteArray"
      }
    ]
  }
]
```
There should be at least one TestRun in the Array. 
Each TestRun contains at least one step.
Each step has a unique name, a step type and a property list (empty or not) depending on step definition.
Each TestRun contains a list (empty or not) of input
Entries from properties list and input list contains 3 field :

- name : name of the property/input
- value : the value of the property or a reference to an entry of inputs
- type : type of the property (string when it's a reference)
- Type can be :
- string
- integer
- boolean
- double
- byteArray
- date

A reference is a string field following this syntax : ${INPUT_NAME}. An entry from inputs list will contains INPUT_NAME as name.

### Implemented step types
**ValidationStep**

A validation step will send inputs to validate to a validation service which return a validation report.

A validation step contains :
- A unique id
- A unique name
- A list of 3 properties
- validationService : name of the validation service (String property)
- validationProfile : Validation Profile tu use io*n the validation service (String property)
- contentToValidate : The content maestro will send to validationService (ByteArray property)

**ITBStep**

An ITB step will send inputs to an external ITB service which return a validation report.

An ITB step contains :
- A unique id
- A unique name
- A list of 6 properties
### Inputs

When we send data to Maestro, we can store it in TestRun inputs instead of step's properties. 
That way, inputs can be access at different step in the same TestRun.

Reference in step's properties should be string type, but referenced input type must be the same as expected in step.

## Reports

In all Test Report, date shall be to format ISO_8601 : "yyyy-MM-dd'T'HH:mm:ss.SSSX"
The returned report is produced in JSON format
The root contains :
- A report version
- A unique identifier UUID
- The date when it was generated
- The overall result of the execution (PASSED, FAILED, UNDEFINED)
- The list of unexpected errors if any
- Test counters containing the number of total test runs, passed test runs, 
failed test runs, undefined test runs, and number of unexpected errors.
- The report can either contain a list of sub reports or a list of test run reports
  - Sub reports are used for multiple execution aggregation
  - Test run reports are mainly used for describing the result of the test run list received as input
- Each test run report contains :
  - A unique id
  - The date when is was run
  - The overall result of the test run (PASSED, FAILED, UNDEFINED)
  - The list of unexpected errors if any
  - The list of step run reports
    - Each step run reports contains :
      - A unique identifier
      - The step type (ex : VALIDATION)
      - The step result (PASSED, FAILED, DONE, UNDEFINED)
      - The list if outputs returned by the step execution (validation report from validation report API)
      - The list of unexpected errors if any

### Report Result computation

#### Step run result
- If there is if at least one unexpected error, the result is UNDEFINED
- If no unexpected error, the step run result is computed from the result of the validation report inside the list of outputs

#### WARNING
There is a slight difference between Step possible Result (PASSED, FAILED, UNDEFINED, DONE) and Test possible Result (PASSED, FAILED, UNDEFINED).
This is because a Step in the Maestro context can end with an output report that does not contain any PASSED or FAILED global result.
For example a step that does not trigger validation service but only some task that doesn't return any report. 
These steps, when finished, will have a DONE status to tell that it has been done without any problem.
The Step Result DONE status is not taken in account in the Test Result computation except all Steps in Test are with DONE status.
In that case, as described below, the Test Result will be set to UNDEFINED as we cannot conclude to a PASSED or FAILED Result.

#### Test run result
- If there are no step is the Test run, the test run Result is UNDEFINED
- If there is if at least one unexpected error, the test run Result is UNDEFINED
- If there is at least one step run with Result UNDEFINED, the test run Result is UNDEFINED
- If all step run Result are DONE, the test run Result is UNDEFINED
- If no unexpected error, and 0 step run Result to DONE/UNDEFINED
  - If at least one step run Result is FAILED, the test run Result is FAILED
  - If no error, 0 UNDEFINED, 0 FAILED, and at least 1 PASSED, the test run Result is PASSED

#### Test report result
- If there are no test run is the Test report, the test report Result is UNDEFINED
- If there is if at least one unexpected error, the test report Result is UNDEFINED
- If there is at least one test run with Result UNDEFINED, the test report Result is UNDEFINED
- If there is at least one sub report with Result UNDEFINED, the test report Result is UNDEFINED
- If no unexpected error, and 0 test run Result to UNDEFINED
  - If at least one test run Result is FAILED, the test report Result is FAILED
  - If at least one sub report Result is FAILED, the test report Result is FAILED
  - If no error, 0 UNDEFINED, 0 FAILED, and at least 1 PASSED, the test report Result is PASSED

#### Sub report
The result of each sub report is computed following the Test report result rules.



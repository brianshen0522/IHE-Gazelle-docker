/*
 * Copyright 2025-2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.maestro.quarkus.ws;

/**
 * Defines constants used for open API examples
 */
public class Constants {

   private Constants() {
   }

   /**
    * Test suite run basic example
    */
   static final String TEST_SUITE_RUN = """
{
   "testSuite": {
     "id": "testSuite1",
     "name": "test suite name",
     "testReferences": [
       {
         "testId": "test1"
       }
     ]
   },
   "accessControlList": {
     "owners": [
       "gazelle",
       "tm-integration"
     ],
     "isPublic": true
   },
   "tests": [
     {
       "id": "test1",
       "name": "test name",
       "steps": [
         {
           "name": "step1",
           "type": "ASSERT_EQUALS",
           "properties": [
             {
               "name": "expected",
               "value": "test",
               "type": "STRING"
             },
             {
               "name": "actual",
               "value": "test",
               "type": "STRING"
             }
           ]
         }
       ]
     }
   ]
 }
""";

   /**
    * Test suite run 2 validations example
    */
   static final String TEST_SUITE_TWO_VALIDATIONS = """
{
  "testSuite": {
    "id": "testSuiteId",
    "name": "test suite name with 2 validations",
    "testReferences": [
      {
        "testId": "Validation number one",
        "properties": [
          {
            "name": "InputValidationOne",
            "type": "BYTE_ARRAY",
            "value": "${inputFile1}"
          }
        ]
      },
      {
        "testId": "Validation number two",
        "properties": [
          {
            "name": "InputValidationTwo",
            "type": "BYTE_ARRAY",
            "value": "${inputFile1}"
          }
        ]
      }
    ]
  },
  "accessControlList": {
    "owners": [
      "gazelle",
      "tm-integration"
    ],
    "isPublic": true
  },
  "tests": [
    {
      "id": "Validation number one",
      "name": "Test for validation number one",
      "steps": [
        {
          "name": "Validation Step",
          "type": "VALIDATION",
          "properties": [
            {
              "name": "validationService",
              "type": "STRING",
              "value": "My Validator"
            },
            {
              "name": "validationProfile",
              "type": "STRING",
              "value": "My Validation Profile"
            },
            {
              "name": "contentToValidate",
              "type": "BYTE_ARRAY",
              "value": "${InputValidationOne}"
            }
          ]
        }
      ]
    },
    {
      "id": "Validation number two",
      "name": "Test for validation number two",
      "steps": [
        {
          "name": "Validation Step",
          "type": "VALIDATION",
          "properties": [
            {
              "name": "validationService",
              "type": "STRING",
              "value": "My Validator"
            },
            {
              "name": "validationProfile",
              "type": "STRING",
              "value": "My Validation Profile"
            },
            {
              "name": "contentToValidate",
              "type": "BYTE_ARRAY",
              "value": "${InputValidationTwo}"
            }
          ]
        }
      ]
    }
  ],
  "inputs": [
    {
      "name": "inputFile1",
      "type": "BYTE_ARRAY",
      "value": "MyDocumentToValidateEncodedinBase64"
    }
  ]
}
""";
   /**
    * Test suite run multiple tests example
    */
   static final String TEST_SUITE_MULTIPLE_TESTS = """
{
  "testSuite": {
    "id": "testSuiteId",
    "name": "test suite name multiple tests",
    "testReferences": [
      {
        "testId": "validation_test",
        "properties": [
          {
            "name": "ValidationTestInput",
            "type": "BYTE_ARRAY",
            "value": "${inputFile1}"
          }
        ]
      }
    ]
  },
  "accessControlList": {
     "isPublic": true
  },
  "tests": [
    {
      "id": "simulation_test",
      "name": "Test for simulation",
      "steps": [
        {
          "name": "Simulation step",
          "type": "SIMULATION",
          "properties": [
            {
              "name": "simulationService",
              "value": "MySimulationService",
              "type": "STRING"
            },
            {
              "name": "sequenceId",
              "value": "MySimulationSequence",
              "type": "STRING"
            }
          ]
        },
        {
          "name": "assert equals step",
          "type": "ASSERT_EQUALS",
          "properties": [
            {
              "name": "expected",
              "value": "test",
              "type": "STRING"
            },
            {
              "name": "actual",
              "value": "test",
              "type": "STRING"
            }
          ]
        }
      ]
    },
    {
      "id": "validation_test",
      "name": "Test for validation",
      "steps": [
        {
          "name": "Validation Step",
          "type": "VALIDATION",
          "properties": [
            {
              "name": "validationService",
              "type": "STRING",
              "value": "MyValidator"
            },
            {
              "name": "validationProfile",
              "type": "STRING",
              "value": "MyValidationProfile"
            },
            {
              "name": "contentToValidate",
              "type": "BYTE_ARRAY",
              "value": "${ValidationTestInput}"
            }
          ]
        }
      ]
    }
  ],
  "inputs": [
    {
      "name": "inputFile1",
      "type": "BYTE_ARRAY",
      "value": "MyDocumentToValidateEncodedinBase64"
    }
  ]
}
""";

   /**
    * Test run assert example
    */
   static final String TEST_RUN_ASSERT = """
{
  "test": {
    "id": "test1",
    "name": "test name",
    "steps": [
      {
        "name": "step1",
        "type": "ASSERT_EQUALS",
        "properties": [
          {
            "name": "expected",
            "value": "test",
            "type": "STRING"
          },
          {
            "name": "actual",
            "value": "test",
            "type": "STRING"
          }
        ]
      }
    ]
  },
  "accessControlList": {
    "isPublic": true
  }
}
""";

   /**
    * Test run validation example
    */
   static final String TEST_RUN_VALIDATION = """
{
  "test": {
    "id": "validationTest",
    "name": "Validation Test Name",
    "steps": [
      {
        "name": "Validation Step",
        "type": "VALIDATION",
        "properties": [
          {
            "name": "validationService",
            "type": "STRING",
            "value": "MyValidator"
          },
          {
            "name": "validationProfile",
            "type": "STRING",
            "value": "MyValidationProfile"
          },
          {
            "name": "contentToValidate",
            "type": "BYTE_ARRAY",
            "value": "${inputFile1}"
          }
        ]
      }
    ]
  },
  "accessControlList": {
    "isPublic": true
  },
   "inputs": [
      {
         "name": "inputFile1",
         "type": "byteArray",
         "value": "MyDocumentToValidateEncodedinBase64"
      }
   ]
}
""";
}

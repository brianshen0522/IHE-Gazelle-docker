/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.itb.gateway.business;

import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStartRequest;

/**
 * Client abstraction for calling ITB APIs.
 */
public interface ItbClient {
   /**
    * Method to execute test request
    *
    * @param startRequest request to send
    * @return SessionId of the started ITB test session
   */
   String startTest(ItbStartRequest startRequest);

   /**
    * Starts an ITB test and waits for completion in a single ITB call.
    *
    * @param startRequest request to send
    * @param timeoutMs timeout in milliseconds used both for ITB wait and client request timeout
    * @return ITB reporting payload containing at least session identifier and raw test report
    */
   ItbReporting startTestAndWait(ItbStartRequest startRequest, long timeoutMs);

   /**
    * Method to retrieve ITB test session logs
    *
    * @param sessionID ITB test session identifier
    * @return ITB test session logs
    */
   String getTestLogs(String sessionID);

   /**
    * Retrieves ITB PDF report for a test session.
    *
    * @param sessionID ITB test session identifier
    * @param testCaseName optional test case identifier (implementation dependent)
    * @return ITB PDF report bytes
    */
   byte[] requestPDFReport(String sessionID, String testCaseName);
}

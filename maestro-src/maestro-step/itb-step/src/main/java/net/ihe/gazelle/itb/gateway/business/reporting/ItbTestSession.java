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

package net.ihe.gazelle.itb.gateway.business.reporting;

import java.util.Objects;

/**
 * ITB callback session metadata.
 */
public class ItbTestSession {

    private String testSuiteIdentifier;
    private String testCaseIdentifier;
    private String testSessionIdentifier;

    /**
     * Creates an empty test session descriptor.
     */
    public ItbTestSession() {
    }

    /**
     * Creates a fully initialized test session descriptor.
     *
     * @param testSuiteIdentifier test suite identifier
     * @param testCaseIdentifier test case identifier
     * @param testSessionIdentifier ITB session identifier
     */
    public ItbTestSession(String testSuiteIdentifier, String testCaseIdentifier, String testSessionIdentifier) {
        this.testSuiteIdentifier = testSuiteIdentifier;
        this.testCaseIdentifier = testCaseIdentifier;
        this.testSessionIdentifier = testSessionIdentifier;
    }

    /**
     * Returns test suite identifier.
     *
     * @return test suite identifier
     */
    public String getTestSuiteIdentifier() {
        return testSuiteIdentifier;
    }

    /**
     * Sets test suite identifier.
     *
     * @param testSuiteIdentifier test suite identifier
     */
    public void setTestSuiteIdentifier(String testSuiteIdentifier) {
        this.testSuiteIdentifier = testSuiteIdentifier;
    }

    /**
     * Returns test case identifier.
     *
     * @return test case identifier
     */
    public String getTestCaseIdentifier() {
        return testCaseIdentifier;
    }

    /**
     * Sets test case identifier.
     *
     * @param testCaseIdentifier test case identifier
     */
    public void setTestCaseIdentifier(String testCaseIdentifier) {
        this.testCaseIdentifier = testCaseIdentifier;
    }

    /**
     * Returns ITB session identifier.
     *
     * @return session identifier
     */
    public String getTestSessionIdentifier() {
        return testSessionIdentifier;
    }

    /**
     * Sets ITB session identifier.
     *
     * @param testSessionIdentifier session identifier
     */
    public void setTestSessionIdentifier(String testSessionIdentifier) {
        this.testSessionIdentifier = testSessionIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItbTestSession that = (ItbTestSession) o;
        return Objects.equals(testSuiteIdentifier, that.testSuiteIdentifier) &&
                Objects.equals(testCaseIdentifier, that.testCaseIdentifier) &&
                Objects.equals(testSessionIdentifier, that.testSessionIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testSuiteIdentifier, testCaseIdentifier, testSessionIdentifier);
    }

    @Override
    public String toString() {
        return "TestSessionDTO{" +
                "testSuiteIdentifier='" + testSuiteIdentifier + '\'' +
                ", testCaseIdentifier='" + testCaseIdentifier + '\'' +
                ", testSessionIdentifier='" + testSessionIdentifier + '\'' +
                '}';
    }
}

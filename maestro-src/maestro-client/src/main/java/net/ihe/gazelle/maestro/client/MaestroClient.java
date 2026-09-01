package net.ihe.gazelle.maestro.client;

import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;

import java.net.URI;

/**
 * The MaestroClient interface defines methods for interacting with Maestro testing services.
 */
public interface MaestroClient {

   /**
    * Executes a single test run based on the provided configuration.
    *
    * @param testRun the {@link TestRun} object containing the configuration and parameters for the test run
    * @param persist a boolean flag indicating whether the test results should be persisted
    * @return a {@link TestReport} object containing detailed results of the executed test
    */
   TestReport executeTest(TestRun testRun, boolean persist);

   /**
    * Executes a test suite based on the provided configuration.
    *
    * @param testSuiteRun the {@link TestSuiteRun} object containing the configuration
    *                     and parameters for the test suite execution
    * @param persist      a boolean flag indicating whether the test suite results
    *                     should be persisted
    * @return a {@link TestReport} object containing detailed results of the executed test suite
    */
   TestReport executeTestSuite(TestSuiteRun testSuiteRun, boolean persist);

   /**
    * Asynchronously executes the specified test run using the provided configuration.
    * The results of the execution can be persisted and a callback URI can be specified
    * for notifying the caller upon test completion.
    *
    * @param testRun the {@link TestRun} object containing the configuration and parameters
    *                for the test run to be executed
    * @param persist a boolean flag indicating whether the test results should be persisted
    * @param callback the {@link URI} to which a notification will be sent upon completion
    *                 of the test execution
    */
   void executeTestAsync(TestRun testRun, boolean persist, URI callback);

   /**
    * Asynchronously executes the specified test suite using the provided configuration.
    * The results of the execution can be persisted, and a callback URI can be specified
    * for notifying the caller upon test suite completion.
    *
    * @param testSuiteRun the {@link TestSuiteRun} object containing the configuration
    *                     and parameters for the test suite execution
    * @param persist      a boolean flag indicating whether the test suite results should be persisted
    * @param callback     the {@link URI} to which a notification will be sent upon completion
    *                     of the test suite execution
    */
   void executeTestSuiteAsync(TestSuiteRun testSuiteRun, boolean persist, URI callback);

}

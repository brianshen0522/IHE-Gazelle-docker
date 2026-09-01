package net.ihe.gazelle.validation.gateway.evs.business.service;

import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;

public interface ValidationExecutionGateway {

    TestReport executeValidation(TestRun testRun);
}

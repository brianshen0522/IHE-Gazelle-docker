package net.ihe.gazelle.validation.gateway.evs.technical.service;

import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.client.MaestroClient;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationExecutionGateway;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MaestroValidationExecutionGateway implements ValidationExecutionGateway {

    private final MaestroClient maestroClient;

    public MaestroValidationExecutionGateway(MaestroClient maestroClient) {
        this.maestroClient = maestroClient;
    }

    @Override
    public TestReport executeValidation(TestRun testRun) {
        return maestroClient.executeTest(testRun, true);
    }
}

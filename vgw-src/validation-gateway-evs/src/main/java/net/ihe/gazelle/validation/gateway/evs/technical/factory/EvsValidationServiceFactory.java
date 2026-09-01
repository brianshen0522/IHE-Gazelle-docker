package net.ihe.gazelle.validation.gateway.evs.technical.factory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import net.ihe.gazelle.maestro.client.MaestroClient;
import net.ihe.gazelle.maestro.client.MaestroClientFactory;
import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.technical.acl.ReadAccessKeyGeneratorImpl;
import net.ihe.gazelle.validation.gateway.business.ProfileReadId;
import net.ihe.gazelle.validation.gateway.evs.business.service.*;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EvsValidationServiceFactory {

    @Produces
    @ApplicationScoped
    public EvsValidationService evsValidationService(ValidationExecutionService executionService,
                                                     ValidationProfileService profileCatalog) {
        return new EvsValidationService(executionService, profileCatalog);
    }

    @Produces
    @ApplicationScoped
    public ValidationExecutionService validationExecutionService(ValidationAccessPolicy accessPolicy,
                                                                 ValidationPresentation presentation,
                                                                 TestRunMapper runMapper,
                                                                 ValidationExecutionGateway executionGateway,
                                                                 ValidationReportService reportService,
                                                                 AsyncReportState asyncReportState,
                                                                 ReadService<ProfileReadId, ValidationProfile> readProfileService) {
        return new ValidationExecutionService(accessPolicy, presentation, runMapper, executionGateway,
              reportService, asyncReportState, readProfileService);
    }

    @Produces
    @ApplicationScoped
    public ValidationAccessPolicy validationAccessPolicy(
          Authz authz,
          @ConfigProperty(name = "evs.api.read-access-key-length", defaultValue = "32") int readAccessKeyLength) {
        return new ValidationAccessPolicy(readAccessKeyLength, new ReadAccessKeyGeneratorImpl(), authz);
    }

    @Produces
    @ApplicationScoped
    public ValidationPresentation validationPresentation(
          @ConfigProperty(name = "datahouse.url") String datahouseBaseUrl,
          @ConfigProperty(name = "validation.portal.base-url", defaultValue = "") String validationPortalBaseUrl) {
        return new ValidationPresentation(datahouseBaseUrl, validationPortalBaseUrl);
    }

    @Produces
    @ApplicationScoped
    public MaestroClient maestroClient(
          @ConfigProperty(name = "maestro.base-url") String baseUrl,
          @ConfigProperty(name = "maestro.m2m.k8s-id-variable-name") String k8sIdVariableName,
          @ConfigProperty(name = "maestro.connect-timeout-ms", defaultValue = "10000") int connectTimeout,
          @ConfigProperty(name = "maestro.read-timeout-ms", defaultValue = "120000") int readTimeout) {
        return MaestroClientFactory.createClient(
                baseUrl,
                k8sIdVariableName,
                connectTimeout,
                readTimeout
        );
    }

    @Produces
    @ApplicationScoped
    public TestRunMapper validationRunMapper() {
        return new TestRunMapper();
    }

    @Produces
    @ApplicationScoped
    public AsyncReportState asyncReportState() {
        return new AsyncReportState();
    }

    @Produces
    @ApplicationScoped
    public ItemTransformationService itemTransformationService() {
        return new ItemTransformationService();
    }

    @Produces
    @ApplicationScoped
    public ValidationLookupService validationLookupService(AsyncReportState asyncReportState,
                                                           ValidationReportService reportService) {
        return new ValidationLookupService(asyncReportState, reportService);
    }


}

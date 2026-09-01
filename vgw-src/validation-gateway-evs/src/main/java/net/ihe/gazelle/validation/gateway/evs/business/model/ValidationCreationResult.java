package net.ihe.gazelle.validation.gateway.evs.business.model;

public class ValidationCreationResult {

    private final String oid;
    private final String validationUrl;
    private final String reportLocation;
    private final String privacyKey;
    private final boolean async;

    public ValidationCreationResult(String oid,
                                    String validationUrl,
                                    String reportLocation,
                                    String privacyKey,
                                    boolean async) {
        this.oid = oid;
        this.validationUrl = validationUrl;
        this.reportLocation = reportLocation;
        this.privacyKey = privacyKey;
        this.async = async;
    }

    public String getOid() {
        return oid;
    }

    public String getValidationUrl() {
        return validationUrl;
    }

    public String getReportLocation() {
        return reportLocation;
    }

    public String getPrivacyKey() {
        return privacyKey;
    }

    public boolean isAsync() {
        return async;
    }
}

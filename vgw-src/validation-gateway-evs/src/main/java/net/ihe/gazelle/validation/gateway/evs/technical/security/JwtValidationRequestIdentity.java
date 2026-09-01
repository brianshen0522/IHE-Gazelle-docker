package net.ihe.gazelle.validation.gateway.evs.technical.security;

import net.ihe.gazelle.oidc.common.technical.OIDCIdentity;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationRequestIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class JwtValidationRequestIdentity extends OIDCIdentity implements ValidationRequestIdentity {

    private static final String AUTHN_METHOD_CLAIM = "authn_method";
    private static final String OIDC_AUTHN_METHOD = "OIDC";

    private final boolean machineToMachine;

    public JwtValidationRequestIdentity(JsonWebToken jwt) {
        super(jwt);
        String authnMethod = jwt.getClaim(AUTHN_METHOD_CLAIM);
        this.machineToMachine = authnMethod != null && !OIDC_AUTHN_METHOD.equalsIgnoreCase(authnMethod);
    }

    @Override
    public boolean isMachineToMachine() {
        return machineToMachine;
    }
}

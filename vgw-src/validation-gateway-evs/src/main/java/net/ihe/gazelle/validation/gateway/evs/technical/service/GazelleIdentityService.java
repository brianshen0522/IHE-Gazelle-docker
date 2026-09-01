package net.ihe.gazelle.validation.gateway.evs.technical.service;

import net.ihe.gazelle.security.business.BaseGazelleIdentity;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.gateway.evs.technical.security.JwtValidationRequestIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class GazelleIdentityService {

    public GazelleIdentity resolveIdentity(String authorizationHeader, JsonWebToken jwt) {
        if (authorizationHeader == null || authorizationHeader.isBlank()
                || !authorizationHeader.startsWith("Bearer")) {
            return BaseGazelleIdentity.unauthenticatedIdentity();
        }
        if (jwt == null) {
            return BaseGazelleIdentity.unauthenticatedIdentity();
        }
        String raw = jwt.getRawToken();
        if (raw == null || raw.isBlank()) {
            return BaseGazelleIdentity.unauthenticatedIdentity();
        }
        return new JwtValidationRequestIdentity(jwt);
    }
}

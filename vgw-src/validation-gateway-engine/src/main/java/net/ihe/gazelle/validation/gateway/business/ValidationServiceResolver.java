package net.ihe.gazelle.validation.gateway.business;

import net.ihe.gazelle.security.business.GazelleIdentity;
import java.util.List;

public interface ValidationServiceResolver {

   List<ResolvedValidationService> resolve(ProfileSearchCriteria criteria, GazelleIdentity identity);
}

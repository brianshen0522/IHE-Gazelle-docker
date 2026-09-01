package net.ihe.gazelle.validation.gateway.technical.override;

import net.ihe.gazelle.validation.gateway.technical.service.support.FakeValidationService;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OverridableValidationServiceTest {

   @Test
   void getValidationProfilesReturnsOverrideWhenConfigured() {
      List<ValidationProfile> overrideProfiles = List.of(new ValidationProfile().setProfileID("ID-1"));
      ValidationProfilesOverride override = serviceName -> Optional.of(overrideProfiles);
      CountingValidationService delegate = new CountingValidationService();

      OverridableValidationService service = new OverridableValidationService("svc-1", delegate, override);

      List<ValidationProfile> profiles = service.getValidationProfiles();

      assertThat(profiles, is(overrideProfiles));
      assertThat("delegate should not be called", delegate.profileCalls.get(), is(0));
   }

   @Test
   void getValidationProfilesDelegatesWhenNoOverride() {
      List<ValidationProfile> delegateProfiles = List.of(new ValidationProfile().setProfileID("ID-2"));
      ValidationService delegate = new FakeValidationService(delegateProfiles);
      ValidationProfilesOverride override = serviceName -> Optional.empty();

      OverridableValidationService service = new OverridableValidationService("svc-1", delegate, override);

      List<ValidationProfile> profiles = service.getValidationProfiles();

      assertThat(profiles, is(delegateProfiles));
   }

   private static final class CountingValidationService implements ValidationService {

      private final AtomicInteger profileCalls = new AtomicInteger();

      @Override
      public ValidationReport validate(ValidationRequest validationRequest) {
         throw new UnsupportedOperationException("Not used in this test.");
      }

      @Override
      public List<ValidationProfile> getValidationProfiles() {
         profileCalls.incrementAndGet();
         return List.of(new ValidationProfile().setProfileID("ID-3"));
      }
   }
}

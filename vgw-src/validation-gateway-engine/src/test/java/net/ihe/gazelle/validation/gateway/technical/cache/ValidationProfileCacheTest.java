package net.ihe.gazelle.validation.gateway.technical.cache;

import net.ihe.gazelle.validation.gateway.technical.override.OverridableValidationService;
import net.ihe.gazelle.validation.gateway.technical.override.ValidationProfilesOverride;
import net.ihe.gazelle.validation.gateway.technical.service.FetchCapableValidationServiceAdapter;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.client.ValidationServiceHttpClient;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationProfileCacheTest {

   @Test
   void cachesProfilesForNonHttpClient() {
      ValidationProfileCache cache = new ValidationProfileCache(10, Duration.ofMinutes(5));
      CountingValidationService service = new CountingValidationService(List.of(profile("P1")));

      List<ValidationProfile> first = cache.getProfiles("svc", service);
      List<ValidationProfile> second = cache.getProfiles("svc", service);

      assertThat(first, sameInstance(second));
      assertThat(service.callCount.get(), is(1));
   }

   @Test
   void usesEtagAndReturnsCachedProfilesOnNotModified() {
      ValidationProfileCache cache = new ValidationProfileCache(10, Duration.ofMinutes(5));
      ValidationProfile profile = profile("P1");
      StubHttpClient client = new StubHttpClient();
      ValidationService service = new FetchCapableValidationServiceAdapter(client);
      client.enqueue(new ValidationServiceHttpClient.ProfilesResponse(200, "etag-1", List.of(profile)));
      client.enqueue(new ValidationServiceHttpClient.ProfilesResponse(304, "etag-1", null));

      List<ValidationProfile> first = cache.getProfiles("svc", service);
      List<ValidationProfile> second = cache.getProfiles("svc", service);

      assertThat(first, sameInstance(second));
      assertThat(client.lastIfNoneMatch, is("etag-1"));
   }

   @Test
   void returnsCachedProfilesWhenHttpClientFails() {
      ValidationProfileCache cache = new ValidationProfileCache(10, Duration.ofMinutes(5));
      StubHttpClient client = new StubHttpClient();
      ValidationService service = new FetchCapableValidationServiceAdapter(client);
      List<ValidationProfile> profiles = List.of(profile("P1"));
      client.enqueue(new ValidationServiceHttpClient.ProfilesResponse(200, "etag-1", profiles));

      List<ValidationProfile> first = cache.getProfiles("svc", service);

      client.failNext(new RuntimeException("boom"));
      List<ValidationProfile> second = cache.getProfiles("svc", service);

      assertThat(first, sameInstance(second));
   }

   @Test
   void skipsRefreshAfterFailureDuringCooldown() {
      ValidationProfileCache cache = new ValidationProfileCache(10, Duration.ofMinutes(5), Duration.ofSeconds(60));
      StubHttpClient client = new StubHttpClient();
      ValidationService service = new FetchCapableValidationServiceAdapter(client);
      List<ValidationProfile> profiles = List.of(profile("P1"));
      client.enqueue(new ValidationServiceHttpClient.ProfilesResponse(200, "etag-1", profiles));

      cache.getProfiles("svc", service);

      client.failNext(new RuntimeException("boom"));
      cache.getProfiles("svc", service);
      int callsAfterFailure = client.callCount;

      cache.getProfiles("svc", service);

      assertThat(client.callCount, is(callsAfterFailure));
   }

   @Test
   void returnsEmptyListWhenHttpClientReturnsNullProfiles() {
      ValidationProfileCache cache = new ValidationProfileCache(10, Duration.ofMinutes(5));
      StubHttpClient client = new StubHttpClient();
      ValidationService service = new FetchCapableValidationServiceAdapter(client);
      client.enqueue(new ValidationServiceHttpClient.ProfilesResponse(200, "etag-1", null));

      List<ValidationProfile> result = cache.getProfiles("svc", service);

      assertThat(result.isEmpty(), is(true));
   }

   @Test
   void throwsWhenNoCachedProfilesAndHttpClientFails() {
      ValidationProfileCache cache = new ValidationProfileCache(10, Duration.ofMinutes(5));
      StubHttpClient client = new StubHttpClient();
      ValidationService service = new FetchCapableValidationServiceAdapter(client);
      client.failNext(new RuntimeException("boom"));

      assertThrows(RuntimeException.class, () -> cache.getProfiles("svc", service));
   }

   @Test
   void wrappedHttpClientKeepsExpiredCacheAndSkipsRepeatedFailuresDuringCooldown() throws Exception {
      ValidationProfileCache cache = new ValidationProfileCache(10, Duration.ofMillis(1), Duration.ofSeconds(60));
      StubHttpClient client = new StubHttpClient();
      client.enqueue(new ValidationServiceHttpClient.ProfilesResponse(200, "etag-1", List.of(profile("P1"))));
      ValidationService service = new CachingValidationService(
            "svc",
            new OverridableValidationService(
                  "svc",
                  new FetchCapableValidationServiceAdapter(client),
                  ValidationProfilesOverride.none()),
            cache
      );

      List<ValidationProfile> warmup = service.getValidationProfiles();
      assertThat(warmup.size(), is(1));
      assertThat(client.callCount, is(1));

      Thread.sleep(25);

      client.failNext(new RuntimeException("boom-1"));
      List<ValidationProfile> firstFailureResult = service.getValidationProfiles();

      client.failNext(new RuntimeException("boom-2"));
      List<ValidationProfile> secondFailureResult = service.getValidationProfiles();

      assertThat(firstFailureResult, sameInstance(warmup));
      assertThat(secondFailureResult, sameInstance(warmup));
      assertThat(client.callCount, is(2));
   }

   private static ValidationProfile profile(String id) {
      return new ValidationProfile().setProfileID(id);
   }

   private static final class CountingValidationService implements ValidationService {
      private final List<ValidationProfile> profiles;
      private final AtomicInteger callCount = new AtomicInteger();

      private CountingValidationService(List<ValidationProfile> profiles) {
         this.profiles = profiles;
      }

      @Override
      public net.ihe.gazelle.validation.v2.api.business.report.ValidationReport validate(
            net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest validationRequest) {
         throw new UnsupportedOperationException("Not needed for these tests.");
      }

      @Override
      public List<ValidationProfile> getValidationProfiles() {
         callCount.incrementAndGet();
         return profiles;
      }
   }

   private static final class StubHttpClient extends ValidationServiceHttpClient {
      private final ArrayDeque<ProfilesResponse> responses = new ArrayDeque<>();
      private RuntimeException failure;
      private String lastIfNoneMatch;
      private int callCount;

      private StubHttpClient() {
         super(URI.create("http://localhost"));
      }

      private void enqueue(ProfilesResponse response) {
         responses.add(response);
      }

      private void failNext(RuntimeException exception) {
         this.failure = exception;
      }

      @Override
      public ProfilesResponse fetchProfiles(String ifNoneMatch) {
         callCount++;
         lastIfNoneMatch = ifNoneMatch;
         if (failure != null) {
            RuntimeException next = failure;
            failure = null;
            throw next;
         }
         ProfilesResponse response = responses.poll();
         if (response == null) {
            throw new IllegalStateException("No response configured");
         }
         return response;
      }
   }
}

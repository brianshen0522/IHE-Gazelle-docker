package net.ihe.gazelle.validation.gateway.quarkus.override;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationProfilesFileOverrideTest {

   @Test
   void resolveReturnsEmptyWhenServiceNameNullOrBlank() throws Exception {
      Path indexFile = Files.createTempFile("override-index", ".json");
      Files.writeString(indexFile, "{}");
      ValidationProfilesFileOverride override = new ValidationProfilesFileOverride(new ObjectMapper(), indexFile);

      Optional<List<ValidationProfile>> nullResolved = override.resolve(null);
      Optional<List<ValidationProfile>> blankResolved = override.resolve("   ");

      assertThat(nullResolved.isEmpty(), is(true));
      assertThat(blankResolved.isEmpty(), is(true));
   }

   @Test
   void resolveReadsProfilesFromIndexMappedPath() throws Exception {
      Path profilesFile = Files.createTempFile("profiles", ".json");
      Files.writeString(profilesFile, "[{\"profileID\":\"ID-1\"}]");
      Path indexFile = Files.createTempFile("override-index", ".json");
      Files.writeString(indexFile, "{\"svc-1\":\"" + escapeJson(profilesFile.toString()) + "\"}");
      ValidationProfilesFileOverride override = new ValidationProfilesFileOverride(new ObjectMapper(), indexFile);

      Optional<List<ValidationProfile>> resolved = override.resolve("svc-1");

      assertThat(resolved.isPresent(), is(true));
      assertThat(resolved.get().getFirst().getProfileID(), is("ID-1"));
   }

   @Test
   void resolveUsesExactServiceNameKey() throws Exception {
      Path profilesFile = Files.createTempFile("profiles", ".json");
      Files.writeString(profilesFile, "[{\"profileID\":\"ID-2\"}]");
      Path indexFile = Files.createTempFile("override-index", ".json");
      Files.writeString(indexFile, "{\"My_Service-Name\":\"" + escapeJson(profilesFile.toString()) + "\"}");
      ValidationProfilesFileOverride override = new ValidationProfilesFileOverride(new ObjectMapper(), indexFile);

      Optional<List<ValidationProfile>> resolved = override.resolve("My_Service-Name");

      assertThat(resolved.isPresent(), is(true));
      assertThat(resolved.get().getFirst().getProfileID(), is("ID-2"));
   }

   @Test
   void resolveDoesNotNormalizeServiceNameKey() throws Exception {
      Path profilesFile = Files.createTempFile("profiles", ".json");
      Files.writeString(profilesFile, "[{\"profileID\":\"ID-3\"}]");
      Path indexFile = Files.createTempFile("override-index", ".json");
      Files.writeString(indexFile, "{\"my.service.name\":\"" + escapeJson(profilesFile.toString()) + "\"}");
      ValidationProfilesFileOverride override = new ValidationProfilesFileOverride(new ObjectMapper(), indexFile);

      Optional<List<ValidationProfile>> resolved = override.resolve("My_Service-Name");

      assertThat(resolved.isEmpty(), is(true));
   }

   @Test
   void resolveReturnsEmptyWhenNoIndexKeyMatch() throws Exception {
      Path indexFile = Files.createTempFile("override-index", ".json");
      Files.writeString(indexFile, "{\"other.service\":\"/tmp/profiles.json\"}");
      ValidationProfilesFileOverride override = new ValidationProfilesFileOverride(new ObjectMapper(), indexFile);

      Optional<List<ValidationProfile>> resolved = override.resolve("svc-2");

      assertThat(resolved.isEmpty(), is(true));
   }

   @Test
   void resolveReturnsEmptyWhenIndexFileMissing() {
      Path missingIndex = Path.of("/tmp/override-index-" + System.nanoTime() + ".json");
      ValidationProfilesFileOverride override = new ValidationProfilesFileOverride(new ObjectMapper(), missingIndex);

      Optional<List<ValidationProfile>> resolved = override.resolve("svc-1");

      assertThat(resolved.isEmpty(), is(true));
   }

   @Test
   void resolveFailsWhenIndexJsonInvalid() throws Exception {
      Path invalidIndex = Files.createTempFile("override-index", ".json");
      Files.writeString(invalidIndex, "{invalid}");
      ValidationProfilesFileOverride override = new ValidationProfilesFileOverride(new ObjectMapper(), invalidIndex);

      IllegalStateException exception = assertThrows(IllegalStateException.class, () -> override.resolve("svc-1"));

      assertThat(exception.getMessage(), containsString("Failed to read validation profiles override index file"));
   }

   @Test
   void resolveFailsWhenMappedProfilesFileMissing() throws Exception {
      Path indexFile = Files.createTempFile("override-index", ".json");
      Path missingProfiles = Path.of("/tmp/profiles-" + System.nanoTime() + ".json");
      Files.writeString(indexFile, "{\"svc-1\":\"" + escapeJson(missingProfiles.toString()) + "\"}");
      ValidationProfilesFileOverride override = new ValidationProfilesFileOverride(new ObjectMapper(), indexFile);

      IllegalStateException exception = assertThrows(IllegalStateException.class, () -> override.resolve("svc-1"));

      assertThat(exception.getMessage(), containsString("file does not exist"));
   }

   @Test
   void resolveIgnoresLegacyPropertiesConfig() throws Exception {
      Path indexFile = Files.createTempFile("override-index", ".json");
      Files.writeString(indexFile, "{}");
      String legacyKey = "gzl.validation.profiles.override.svc.1";
      System.setProperty(legacyKey, "/tmp/legacy-path.json");
      try {
         ValidationProfilesFileOverride override = new ValidationProfilesFileOverride(new ObjectMapper(), indexFile);

         Optional<List<ValidationProfile>> resolved = override.resolve("svc-1");

         assertThat(resolved.isEmpty(), is(true));
      } finally {
         System.clearProperty(legacyKey);
      }
   }

   private static String escapeJson(String value) {
      return value.replace("\\", "\\\\");
   }
}

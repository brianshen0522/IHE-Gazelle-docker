package net.ihe.gazelle.validation.gateway.migration.mapper;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsValidationSourceRow;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class MigrationItemMapperTest {

   private final MigrationItemMapper mapper = new MigrationItemMapper("super-admin");

   @Test
   void mapAclSetsSuperAdminAndPublicForAnonymousOwner() {
      EvsValidationSourceRow row = sourceRow(null, null, true, "read-key");

      AccessControlList acl = mapper.mapAcl(row);

      assertThat(acl.getOwners(), contains("super-admin"));
      assertThat(acl.isPublic(), is(true));
      assertThat(acl.getReadAccessKey(), equalTo("read-key"));
      assertThat(acl.getReaders(), equalTo(Set.of()));
   }

   @Test
    void mapAclPreservesPrivateFlagForKnownOwner() {
      EvsValidationSourceRow row = sourceRow("alice", "acme", true, "secret");

      AccessControlList acl = mapper.mapAcl(row);

      assertThat(acl.getOwners(), contains("alice"));
      assertThat(acl.isPublic(), is(false));
      assertThat(acl.getReadAccessKey(), equalTo("secret"));
      assertThat(acl.getReaders(), equalTo(Set.of(
            Groups.ROLE_MONITOR,
            Groups.ROLE_TESTING_SESSION_MANAGER,
            Groups.PREFIX_ORGANIZATION + "acme"
      )));
   }

   @Test
   void mapAclLeavesReadersEmptyForPublicOwnedValidation() {
      EvsValidationSourceRow row = sourceRow("alice", "acme", false, null);

      AccessControlList acl = mapper.mapAcl(row);

      assertThat(acl.isPublic(), is(true));
      assertThat(acl.getReaders(), equalTo(Set.of()));
   }

   @Test
   void mapStatusMapsKnownValuesAndDefaultsToUndefined() {
      assertThat(mapper.mapStatus("DONE_PASSED"), is(ValidationTestResult.PASSED));
      assertThat(mapper.mapStatus("DONE_FAILED"), is(ValidationTestResult.FAILED));
      assertThat(mapper.mapStatus("PENDING"), is(ValidationTestResult.UNDEFINED));
      assertThat(mapper.mapStatus(null), is(ValidationTestResult.UNDEFINED));
   }

   @Test
   void mapAddsEvsParametersAndValidationType() {
      EvsValidationSourceRow row = sourceRow("alice", "acme", false, null);

      Item item = mapper.map(row, null);

      assertThat(item.getType(), is("VALIDATION_REPORT"));
      assertThat(item.getAdditionalParameter("evs_oid"), is("oid-1"));
      assertThat(item.getAdditionalParameter("evs_validation_type"), is("CDA"));
      assertThat(item.getAdditionalParameter("evs_entry_point"), is("GUI"));
   }

   private EvsValidationSourceRow sourceRow(String owner, String organization, Boolean isPrivate, String privacyKey) {
      return new EvsValidationSourceRow(
            1,
            "oid-1",
            Instant.parse("2026-01-01T10:15:30Z"),
            "DONE_PASSED",
            "CDA",
            "validator-service",
            "1.0.0",
            "profile-id",
            "2.0.0",
            owner,
            organization,
            isPrivate,
            privacyKey,
            "GUI",
            "/tmp/validation.zip"
      );
   }
}

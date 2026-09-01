package net.ihe.gazelle.validation.gateway.migration.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsValidationSourceRow;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationMethod;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@ApplicationScoped
public class MigrationItemMapper {

   static final String ITEM_TYPE = "VALIDATION_REPORT";

   private final String superAdminUsername;
   private final ObjectMapper objectMapper;

   public MigrationItemMapper(
         @ConfigProperty(name = "migration.super-admin.username", defaultValue = "gazelle-super-admin")
         String superAdminUsername
   ) {
      this.superAdminUsername = superAdminUsername;
      this.objectMapper = new ObjectMapper();
   }

   public Item map(EvsValidationSourceRow row, ValidationReport parsedReport) {
      ValidationReport report = buildReport(row, parsedReport);
      String reportJson = serialize(report);

      return new Item()
            .setType(ITEM_TYPE)
            .setDate(row.validationDate() == null ? new Date() : Date.from(row.validationDate()))
            .setContent(reportJson)
            .setAccessControlList(mapAcl(row))
            .addAdditionalParameter("evs_oid", row.oid())
            .addAdditionalParameter("evs_validation_type", safeValue(row.validationType()))
            .addAdditionalParameter("evs_entry_point", safeValue(row.entryPoint()))
            .addAdditionalParameter("migration_version", "1.0")
            .addAdditionalParameter("migrated_at", Instant.now().toString());
   }

   AccessControlList mapAcl(EvsValidationSourceRow row) {
      AccessControlList acl = new AccessControlList();
      boolean isPrivate = Boolean.TRUE.equals(row.privateValidation());
      String username = row.ownerUsername();
      if (username == null || username.isBlank()) {
         acl.setOwners(Set.of(superAdminUsername));
         acl.setPublic(true);
      } else {
         acl.setOwners(Set.of(username));
         acl.setPublic(!isPrivate);
      }
      if (row.privacyKey() != null && !row.privacyKey().isBlank()) {
         acl.setReadAccessKey(row.privacyKey());
      }
      acl.setReaders(!acl.isPublic() ? resolveReaders(row) : Set.of());
      acl.setEditors(Set.of());
      return acl;
   }

   private Set<String> resolveReaders(EvsValidationSourceRow row) {
      LinkedHashSet<String> readers = new LinkedHashSet<>();
      readers.add(Groups.ROLE_MONITOR);
      readers.add(Groups.ROLE_TESTING_SESSION_MANAGER);
      addOwnerOrganizationReader(readers, row);
      return Set.copyOf(readers);
   }

   private void addOwnerOrganizationReader(Set<String> readers, EvsValidationSourceRow row) {
      String username = row.ownerUsername();
      if (username == null || username.isBlank()) {
         return;
      }
      String organization = row.ownerOrganization();
      if (organization == null || organization.isBlank()) {
         return;
      }
      readers.add(Groups.PREFIX_ORGANIZATION + organization);
   }

   private ValidationReport buildReport(EvsValidationSourceRow row, ValidationReport parsedReport) {
      ValidationReport report = parsedReport == null ? new ValidationReport() : new ValidationReport(parsedReport);
      report.setUuid(row.oid());
      report.setDateTime(row.validationDate() == null ? new Date() : Date.from(row.validationDate()));
      report.setOverallResult(mapStatus(row.status()));

      ValidationMethod method = report.getValidationMethod() == null ? new ValidationMethod() : report.getValidationMethod();
      if (row.validationService() != null && !row.validationService().isBlank()) {
         method.setValidationServiceName(row.validationService());
      }
      if (row.validationServiceVersion() != null && !row.validationServiceVersion().isBlank()) {
         method.setValidationServiceVersion(row.validationServiceVersion());
      }
      if (row.validatorKeyword() != null && !row.validatorKeyword().isBlank()) {
         method.setValidationProfileID(row.validatorKeyword());
      }
      if (row.validatorVersion() != null && !row.validatorVersion().isBlank()) {
         method.setValidationProfileVersion(row.validatorVersion());
      }
      report.setValidationMethod(method);
      return report;
   }

   public ValidationTestResult mapStatus(String status) {
      if (status == null) {
         return ValidationTestResult.UNDEFINED;
      }
      return switch (status) {
         case "DONE_PASSED" -> ValidationTestResult.PASSED;
         case "DONE_FAILED" -> ValidationTestResult.FAILED;
         default -> ValidationTestResult.UNDEFINED;
      };
   }

   private String serialize(ValidationReport report) {
      try {
         return objectMapper.writeValueAsString(new ValidationReportDTO(report));
      } catch (Exception e) {
         throw new IllegalStateException("Failed to serialize migrated validation report", e);
      }
   }

   private String safeValue(String value) {
      return value == null ? "" : value;
   }
}

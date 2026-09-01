package net.ihe.gazelle.validation.gateway.migration.evs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.ihe.gazelle.validation.v2.api.business.report.AssertionReport;
import net.ihe.gazelle.validation.v2.api.business.report.Metadata;
import net.ihe.gazelle.validation.v2.api.business.report.RequirementPriority;
import net.ihe.gazelle.validation.v2.api.business.report.SeverityLevel;
import net.ihe.gazelle.validation.v2.api.business.report.SubjectLocation;
import net.ihe.gazelle.validation.v2.api.business.report.UnexpectedError;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationMethod;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationSubReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.ZipInputStream;

@ApplicationScoped
public class LegacyReportParser {
   private static final String SUB_REPORTS = "subReports";
   private static final String SUB_REPORT = "subReport";
   private static final String VALUE = "value";

   private final ObjectMapper jsonMapper;
   private final XmlMapper xmlMapper;

   public LegacyReportParser() {
      this.jsonMapper = new ObjectMapper();
      this.jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      this.xmlMapper = new XmlMapper();
      this.xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public Optional<ValidationReport> parseValidationReport(String archivePath) {
      if (archivePath == null || archivePath.isBlank()) {
         return Optional.empty();
      }
      try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archivePath))) {
         java.util.zip.ZipEntry entry;
         while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
               continue;
            }
            Optional<ValidationReport> parsed = parseValidationReport(zis.readAllBytes());
            if (parsed.isPresent()) {
               return parsed;
            }
         }
         return Optional.empty();
      } catch (IOException e) {
         return Optional.empty();
      }
   }

   Optional<ValidationReport> parseValidationReport(byte[] payload) {
      if (payload == null || payload.length == 0) {
         return Optional.empty();
      }
      Optional<ValidationReport> legacyXml = parseLegacyXml(payload);
      if (legacyXml.isPresent()) {
         return legacyXml;
      }
      Optional<ValidationReport> legacyJson = parseLegacyJson(payload);
      if (legacyJson.isPresent()) {
         return legacyJson;
      }
      Optional<ValidationReport> v2Xml = parseV2Xml(payload);
      if (v2Xml.isPresent()) {
         return v2Xml;
      }
      return parseV2Json(payload);
   }

   private Optional<ValidationReport> parseLegacyXml(byte[] payload) {
      try {
         JsonNode xmlTree = xmlMapper.readTree(new ByteArrayInputStream(payload));
         JsonNode reportNode = findByLocalName(xmlTree, "validationReport");
         if (reportNode == null && isLegacyNode(xmlTree)) {
            reportNode = xmlTree;
         }
         if (!isLegacyNode(reportNode)) {
            return Optional.empty();
         }
         return Optional.of(mapLegacyToV2(reportNode));
      } catch (Exception ignored) {
         return Optional.empty();
      }
   }

   private Optional<ValidationReport> parseLegacyJson(byte[] payload) {
      try {
         JsonNode root = jsonMapper.readTree(payload);
         if (!isLegacyNode(root)) {
            return Optional.empty();
         }
         return Optional.of(mapLegacyToV2(root));
      } catch (Exception ignored) {
         return Optional.empty();
      }
   }

   private Optional<ValidationReport> parseV2Xml(byte[] payload) {
      try {
         ValidationReportDTO dto = xmlMapper.readValue(new ByteArrayInputStream(payload), ValidationReportDTO.class);
         return Optional.of(dto.getBusinessObject());
      } catch (Exception ignored) {
         return Optional.empty();
      }
   }

   private Optional<ValidationReport> parseV2Json(byte[] payload) {
      try {
         ValidationReportDTO dto = jsonMapper.readValue(payload, ValidationReportDTO.class);
         return Optional.of(dto.getBusinessObject());
      } catch (Exception ignored) {
         return Optional.empty();
      }
   }

   private boolean isLegacyNode(JsonNode node) {
      return node != null && (findByLocalName(node, "validationOverview") != null
            || findByLocalName(node, SUB_REPORTS) != null
            || findByLocalName(node, SUB_REPORT) != null);
   }

   private ValidationReport mapLegacyToV2(JsonNode rootNode) {
      ValidationReport report = new ValidationReport();
      report.setUuid(stringField(rootNode, "uuid"));

      JsonNode overviewNode = findByLocalName(rootNode, "validationOverview");
      report.setDateTime(parseDate(stringField(overviewNode, "validationDateTime")));
      report.setDisclaimer(stringField(overviewNode, "disclaimer"));
      ValidationMethod method = new ValidationMethod();
      method.setValidationServiceName(stringField(overviewNode, "validationServiceName"));
      method.setValidationServiceVersion(stringField(overviewNode, "validationServiceVersion"));
      method.setValidationProfileID(stringField(overviewNode, "validatorID"));
      method.setValidationProfileName(stringField(overviewNode, "validatorName"));
      method.setValidationProfileVersion(stringField(overviewNode, "validatorVersion"));
      report.setValidationMethod(method);
      report.setOverallResult(parseResult(
            stringField(rootNode, "result"),
            stringField(overviewNode, "validationOverallResult")
      ));

      for (JsonNode metadataNode : arrayField(overviewNode, "additionalMetadata")) {
         Metadata metadata = new Metadata();
         metadata.setName(stringField(metadataNode, "name"));
         metadata.setValue(metadataNode.isTextual() ? metadataNode.asText() : stringField(metadataNode, VALUE));
         if (metadata.getName() != null || metadata.getValue() != null) {
            report.addAdditionalMetadata(metadata);
         }
      }

      JsonNode subReportsNode = findByLocalName(rootNode, SUB_REPORTS);
      if (subReportsNode == null) {
         subReportsNode = findByLocalName(rootNode, SUB_REPORT);
      }
      for (JsonNode subNode : asArray(subReportsNode)) {
         report.addValidationSubReport(mapSubReport(subNode));
      }

      report.computeCounters();
      report.computeOverallResult();
      return report;
   }

   private ValidationSubReport mapSubReport(JsonNode subNode) {
      ValidationSubReport subReport = new ValidationSubReport();
      subReport.setName(stringField(subNode, "name"));
      subReport.setSubReportResult(parseResult(stringField(subNode, "subReportResult")));
      subReport.setStandards(toStringList(findByLocalName(subNode, "standards")));
      for (JsonNode errorNode : arrayField(subNode, "unexpectedErrors")) {
         subReport.addUnexpectedError(mapUnexpectedError(errorNode));
      }

      JsonNode nestedSubNode = findByLocalName(subNode, SUB_REPORTS);
      if (nestedSubNode == null) {
         nestedSubNode = findByLocalName(subNode, SUB_REPORT);
      }
      for (JsonNode childSubNode : asArray(nestedSubNode)) {
         subReport.addSubReport(mapSubReport(childSubNode));
      }

      JsonNode constraintsNode = findByLocalName(subNode, "constraints");
      if (constraintsNode == null) {
         constraintsNode = findByLocalName(subNode, "constraint");
      }
      for (JsonNode constraintNode : asArray(constraintsNode)) {
         subReport.addAssertionReport(mapAssertion(constraintNode));
      }

      subReport.computeCounters();
      subReport.computeResult();
      return subReport;
   }

   private AssertionReport mapAssertion(JsonNode constraintNode) {
      AssertionReport assertion = new AssertionReport();
      assertion.setAssertionID(stringField(constraintNode, "constraintID"));
      assertion.setAssertionType(stringField(constraintNode, "constraintType"));
      assertion.setDescription(stringField(constraintNode, "constraintDescription"));
      assertion.setFormalExpression(stringField(constraintNode, "formalExpression"));
      assertion.setSubjectValue(stringField(constraintNode, "valueInValidatedObject"));
      assertion.setRequirementIDs(toStringList(findByLocalName(constraintNode, "assertionID"),
            findByLocalName(constraintNode, "assertionIDs")));
      assertion.setResult(parseResult(stringField(constraintNode, "testResult")));
      assertion.setPriority(parsePriority(stringField(constraintNode, "priority")));
      assertion.setSeverity(parseSeverity(stringField(constraintNode, "severity")));
      if (assertion.getSeverity() == null) {
         assertion.computeSeverity();
      }

      JsonNode subjectLocations = findByLocalName(constraintNode, "subjectLocations");
      if (subjectLocations != null) {
         for (JsonNode slNode : asArray(subjectLocations)) {
            SubjectLocation sl = new SubjectLocation();
            sl.setInputId(stringField(slNode, "inputId"));
            sl.setType(stringField(slNode, "type"));
            sl.setValue(stringField(slNode, VALUE));
            if (sl.getValue() != null) {
               assertion.addSubjectLocation(sl);
            }
         }
      } else {
         String legacyLocation = stringField(constraintNode, "locationInValidatedObject");
         if (legacyLocation != null) {
            assertion.addSubjectLocation(new SubjectLocation()
                  .setType("legacy-location")
                  .setValue(legacyLocation));
         }
      }

      for (JsonNode errorNode : arrayField(constraintNode, "unexpectedErrors")) {
         assertion.addUnexpectedError(mapUnexpectedError(errorNode));
      }

      return assertion;
   }

   private UnexpectedError mapUnexpectedError(JsonNode errorNode) {
      UnexpectedError error = new UnexpectedError();
      error.setName(stringField(errorNode, "name"));
      error.setMessage(stringField(errorNode, "message"));
      JsonNode causeNode = findByLocalName(errorNode, "cause");
      if (causeNode != null && !causeNode.isNull()) {
         error.setCause(mapUnexpectedError(causeNode));
      }
      return error;
   }

   private List<String> toStringList(JsonNode... nodes) {
      List<String> values = new ArrayList<>();
      for (JsonNode node : nodes) {
         for (JsonNode valueNode : asArray(node)) {
            if (valueNode == null || valueNode.isNull()) {
               continue;
            }
            String value = valueNode.isValueNode() ? valueNode.asText() : stringField(valueNode, VALUE);
            if (value != null && !value.isBlank() && !values.contains(value)) {
               values.add(value);
            }
         }
      }
      return values;
   }

   private List<JsonNode> asArray(JsonNode node) {
      List<JsonNode> values = new ArrayList<>();
      if (node == null || node.isNull()) {
         return values;
      }
      if (node.isArray()) {
         node.forEach(values::add);
      } else {
         values.add(node);
      }
      return values;
   }

   private List<JsonNode> arrayField(JsonNode node, String name) {
      return asArray(findByLocalName(node, name));
   }

   private String stringField(JsonNode node, String name) {
      if (node == null) {
         return null;
      }
      JsonNode valueNode = findByLocalName(node, name);
      if (valueNode == null || valueNode.isNull()) {
         return null;
      }
      String value = valueNode.asText();
      return value == null || value.isBlank() || "null".equalsIgnoreCase(value) ? null : value.trim();
   }

   private JsonNode findByLocalName(JsonNode node, String localName) {
      if (node == null || localName == null) {
         return null;
      }
      if (node.has(localName)) {
         return node.get(localName);
      }
      if (node.has("@" + localName)) {
         return node.get("@" + localName);
      }
      if (node.isObject()) {
         var fields = node.fields();
         while (fields.hasNext()) {
            var field = fields.next();
            String key = field.getKey();
            String normalizedKey = key;
            int colon = key.indexOf(':');
            if (colon >= 0 && colon + 1 < key.length()) {
               normalizedKey = key.substring(colon + 1);
            }
            if (localName.equals(normalizedKey) || ("@" + localName).equals(normalizedKey)) {
               return field.getValue();
            }
         }
      }
      return null;
   }

   private ValidationTestResult parseResult(String... candidates) {
      for (String candidate : candidates) {
         if (candidate == null || candidate.isBlank()) {
            continue;
         }
         try {
            return ValidationTestResult.valueOf(candidate.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException ignored) {
         }
      }
      return ValidationTestResult.UNDEFINED;
   }

   private RequirementPriority parsePriority(String rawValue) {
      if (rawValue == null || rawValue.isBlank()) {
         return RequirementPriority.MANDATORY;
      }
      try {
         return RequirementPriority.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ignored) {
         return RequirementPriority.MANDATORY;
      }
   }

   private SeverityLevel parseSeverity(String rawValue) {
      if (rawValue == null || rawValue.isBlank()) {
         return null;
      }
      try {
         return SeverityLevel.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ignored) {
         return null;
      }
   }

   private java.util.Date parseDate(String rawValue) {
      if (rawValue == null || rawValue.isBlank()) {
         return new java.util.Date();
      }
      String normalized = rawValue.trim();
      try {
         return java.util.Date.from(Instant.parse(normalized));
      } catch (Exception ignored) {
      }
      try {
         return java.util.Date.from(OffsetDateTime.parse(normalized).toInstant());
      } catch (Exception ignored) {
      }
      try {
         return java.util.Date.from(
               LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC)
         );
      } catch (Exception ignored) {
      }
      return new java.util.Date();
   }

}

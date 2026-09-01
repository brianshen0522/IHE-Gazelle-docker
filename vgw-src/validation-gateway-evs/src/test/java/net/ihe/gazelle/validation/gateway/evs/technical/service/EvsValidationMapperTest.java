package net.ihe.gazelle.validation.gateway.evs.technical.service;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.CallerMetadataDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.EntryPoint;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.HandledObjectDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.OwnerMetadataDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.SharingMetadataDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationStatus;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationType;
import net.ihe.gazelle.validation.gateway.evs.business.service.TestRunMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class EvsValidationMapperTest {

    private static final String PROPERTY_VALIDATION_SERVICE = "validationService";
    private static final String PROPERTY_VALIDATION_PROFILE = "validationProfile";
    private static final String PROPERTY_CONTENT_TO_VALIDATE = "contentToValidate";

    private final TestRunMapper mapper = new TestRunMapper();

    @Test
    @DisplayName("VAL-022 returns one byte-array property per object role")
    void val022_returnsOnePropertyPerObjectRole() {
        ValidationDTO request = new ValidationDTO();
        ValidationServiceDTO service = new ValidationServiceDTO();
        service.setName("validation-service");
        service.setValidator("profile-42");
        request.setValidationService(service);

        HandledObjectDTO primary = new HandledObjectDTO();
        primary.setRole("attachment");
        primary.setContent("payload".getBytes(StandardCharsets.UTF_8));
        primary.setOriginalFileName("payload.txt");

        HandledObjectDTO duplicate = new HandledObjectDTO();
        duplicate.setRole("attachment");
        duplicate.setContent("duplicate".getBytes(StandardCharsets.UTF_8));

        HandledObjectDTO defaultRole = new HandledObjectDTO();
        defaultRole.setRole("");
        defaultRole.setContent("default".getBytes(StandardCharsets.UTF_8));
        defaultRole.setOriginalFileName("default.txt");

        request.setObjects(List.of(primary, duplicate, defaultRole));
        AccessControlList acl = new AccessControlList();

        TestRun run = mapper.toTestRun("oid", request, acl);
        Step step = run.getTest().getSteps().get(0);

        assertThat(step.getProperties(), hasSize(4));
        Property serviceProperty = step.getProperty(PROPERTY_VALIDATION_SERVICE);
        Property profileProperty = step.getProperty(PROPERTY_VALIDATION_PROFILE);
        assertThat(serviceProperty.getValue(), equalTo("validation-service"));
        assertThat(profileProperty.getValue(), equalTo("profile-42"));

        // Step properties reference TestRun inputs by original filename while preserving byte-array type
        ByteArrayProperty attachment = (ByteArrayProperty) step.getProperty("attachment");
        assertThat(attachment.getValue(), is("${payload.txt}"));

        ByteArrayProperty defaultProperty = (ByteArrayProperty) step.getProperty(PROPERTY_CONTENT_TO_VALIDATE);
        assertThat(defaultProperty.getValue(), is("${default.txt}"));

        // TestRun inputs hold the physical content, named by original filename
        assertThat(run.getInputs(), hasSize(2));
        ByteArrayProperty inputPayload = (ByteArrayProperty) run.getInputs().stream()
              .filter(p -> "payload.txt".equals(p.getName()))
              .findFirst().orElseThrow();
        assertArrayEquals("payload".getBytes(StandardCharsets.UTF_8), (byte[]) inputPayload.getValue());

        ByteArrayProperty defaultInput = (ByteArrayProperty) run.getInputs().stream()
              .filter(p -> "default.txt".equals(p.getName()))
              .findFirst().orElseThrow();
        assertArrayEquals("default".getBytes(StandardCharsets.UTF_8), (byte[]) defaultInput.getValue());
    }

    @Test
    @DisplayName("VAL-022 generates a Maestro input when no original filename is provided")
    void val022_generatesInputWhenNoFilename() {
        ValidationDTO request = new ValidationDTO();
        ValidationServiceDTO service = new ValidationServiceDTO();
        service.setName("validation-service");
        service.setValidator("profile-42");
        request.setValidationService(service);

        HandledObjectDTO noFilename = new HandledObjectDTO();
        noFilename.setContent("payload".getBytes(StandardCharsets.UTF_8));

        request.setObjects(List.of(noFilename));
        AccessControlList acl = new AccessControlList();

        TestRun run = mapper.toTestRun("oid", request, acl);
        Step step = run.getTest().getSteps().get(0);

        // A synthetic input id keeps the payload available to Maestro fallback handling
        assertThat(run.getInputs(), hasSize(1));
        ByteArrayProperty generatedInput = (ByteArrayProperty) run.getInputs().getFirst();
        assertThat(generatedInput.getName(), is("inputFile"));
        assertArrayEquals("payload".getBytes(StandardCharsets.UTF_8), (byte[]) generatedInput.getValue());

        // Step property resolves against the generated TestRun input
        ByteArrayProperty content = (ByteArrayProperty) step.getProperty(PROPERTY_CONTENT_TO_VALIDATE);
        assertThat(content.getValue(), is("${inputFile}"));
    }

    @Test
    @DisplayName("VAL-022 generates distinct Maestro inputs for filename-less objects with different roles")
    void val022_generatesDistinctInputsWhenMultipleObjectsHaveNoFilename() {
        ValidationDTO request = new ValidationDTO();

        HandledObjectDTO document = new HandledObjectDTO();
        document.setRole("document");
        document.setContent("payload".getBytes(StandardCharsets.UTF_8));

        HandledObjectDTO metadata = new HandledObjectDTO();
        metadata.setRole("metadata");
        metadata.setContent("meta".getBytes(StandardCharsets.UTF_8));

        request.setObjects(List.of(document, metadata));

        TestRun run = mapper.toTestRun("oid", request, new AccessControlList());
        Step step = run.getTest().getSteps().get(0);

        assertThat(run.getInputs(), hasSize(2));
        ByteArrayProperty firstInput = (ByteArrayProperty) run.getInputs().get(0);
        ByteArrayProperty secondInput = (ByteArrayProperty) run.getInputs().get(1);
        assertThat(firstInput.getName(), is("inputFile-document"));
        assertThat(secondInput.getName(), is("inputFile-metadata"));
        assertArrayEquals("payload".getBytes(StandardCharsets.UTF_8), (byte[]) firstInput.getValue());
        assertArrayEquals("meta".getBytes(StandardCharsets.UTF_8), (byte[]) secondInput.getValue());

        assertThat(((ByteArrayProperty) step.getProperty("document")).getValue(), is("${inputFile-document}"));
        assertThat(((ByteArrayProperty) step.getProperty("metadata")).getValue(), is("${inputFile-metadata}"));
    }

    @Test
    @DisplayName("VAL-022 uses the profile input name when role is not provided")
    void val022_usesProfileInputNameWhenRoleMissing() {
        ValidationDTO request = new ValidationDTO();
        HandledObjectDTO object = new HandledObjectDTO();
        object.setContent("payload".getBytes(StandardCharsets.UTF_8));
        object.setOriginalFileName("payload.txt");
        request.setObjects(List.of(object));

        TestRun run = mapper.toTestRun("oid", request, new AccessControlList(), "document");
        Step step = run.getTest().getSteps().get(0);

        ByteArrayProperty property = (ByteArrayProperty) step.getProperty("document");
        assertThat(property.getValue(), is("${payload.txt}"));
    }

    @Test
    @DisplayName("VAL-022 keeps role precedence over the profile input name")
    void val022_roleTakesPrecedenceOverProfileInputName() {
        ValidationDTO request = new ValidationDTO();
        HandledObjectDTO object = new HandledObjectDTO();
        object.setRole("attachment");
        object.setContent("payload".getBytes(StandardCharsets.UTF_8));
        object.setOriginalFileName("payload.txt");
        request.setObjects(List.of(object));

        TestRun run = mapper.toTestRun("oid", request, new AccessControlList(), "document");
        Step step = run.getTest().getSteps().get(0);

        ByteArrayProperty property = (ByteArrayProperty) step.getProperty("attachment");
        assertThat(property.getValue(), is("${payload.txt}"));
    }

    @Test
    @DisplayName("VAL-014 defaults the validation date for normalized requests")
    void val014_defaultsValidationDate() {
        ValidationDTO request = new ValidationDTO();
        request.setDate(null);

        ValidationDTO normalized = mapper.normalizeRequest(request);

        assertThat(normalized.getDate(), notNullValue());
        assertThat(request.getDate(), equalTo(normalized.getDate()));
    }

    @Test
    @DisplayName("VAL-037 keeps metadata and report references for EVS responses")
    void val037_copiesMetadataToResponses() {
        ValidationDTO base = new ValidationDTO();
        ValidationServiceDTO service = new ValidationServiceDTO();
        service.setName("service-a");
        service.setValidator("profile-a");
        base.setValidationService(service);
        base.setValidationType(ValidationType.DEFAULT);
        base.setObjectType("object-a");
        base.setObjects(List.of(new HandledObjectDTO()));

        SharingMetadataDTO sharing = new SharingMetadataDTO();
        sharing.setPrivate(true);
        base.setSharing(sharing);

        OwnerMetadataDTO owner = new OwnerMetadataDTO();
        owner.setUsername("owner-a");
        base.setOwner(owner);

        CallerMetadataDTO caller = new CallerMetadataDTO();
        caller.setEntryPoint(EntryPoint.WS);
        base.setCaller(caller);

        OffsetDateTime now = OffsetDateTime.now();
        base.setDate(now);

        ValidationDTO response = mapper.toValidationResponse(
              "oid",
              base,
              ValidationStatus.DONE_PASSED,
              "https://example/report"
        );

        assertThat(response.getOid(), is("oid"));
        assertThat(response.getValidationService(), sameInstance(service));
        assertThat(response.getSharing(), sameInstance(sharing));
        assertThat(response.getOwner(), sameInstance(owner));
        assertThat(response.getCaller(), sameInstance(caller));
        assertThat(response.getDate(), sameInstance(now));
        assertThat(response.getValidationReportRef().getLocation(), is("https://example/report"));
    }

    @Test
    @DisplayName("VAL-049 generates a non-empty identifier for EVS executions")
    void val049_generatesOid() {
        assertThat(mapper.generateOid(), not(emptyString()));
    }
}

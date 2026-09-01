package net.ihe.gazelle.validation.gateway.technical;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.search.api.MapPresentationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Map;

public class ProfilePresenterService extends MapPresentationService<ValidationProfile> {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    @Override
    protected Map<String, Object> toMap(ValidationProfile validationProfile) {
        return OBJECT_MAPPER.convertValue(validationProfile, new TypeReference<>() {});
    }

    @Override
    protected ValidationProfile fromMap(Map<String, Object> map) {
        return OBJECT_MAPPER.convertValue(map, ValidationProfile.class);
    }
}

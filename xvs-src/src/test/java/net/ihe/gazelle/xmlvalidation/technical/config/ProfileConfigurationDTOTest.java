/*
Copyright 2010-2025 IHE International

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.ihe.gazelle.xmlvalidation.technical.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ProfileConfigurationDTOTest {

    @Test
    void profileConfigurationDTOTest() {
        String expectedResult = "ProfileConfigurationDTO{profileConfiguration=ProfileConfiguration{xsdPath='xsdpath', schematronPath='schematronpath', xsltPath='xsltpath', schematronVersion='2025', xsdVersion='1234', cacheEnabled='false', validationProfile{profileID='id', profileName='profile', domain='domain'}}}";
        ProfileConfigurationDTO dto = new ProfileConfigurationDTO();
        dto.setProfileName("profile")
                .setProfileID("id")
                .setDomain("domain")
                .setXsdPath("xsdpath")
                .setSchematronVersion("2025")
                .setXsdVersion("1234")
                .setXsltPath("xsltpath")
                .setSchematronPath("schematronpath")
                .setCacheEnabled(false);
        assertEquals("profile", dto.getProfileName());
        assertEquals("id", dto.getProfileID());
        assertEquals("domain", dto.getDomain());
        assertEquals("xsdpath", dto.getXsdPath());
        assertEquals("xsltpath", dto.getXsltPath());
        assertEquals("schematronpath", dto.getSchematronPath());
        assertFalse(dto.getCacheEnabled());
        assertEquals(ProfileConfiguration.class, dto.getBusinessObject().getClass());
        assertEquals(expectedResult, dto.toString());
    }

    @Test
    void serializeProfileConfigurationDTOTest() throws Exception {
        ProfileConfigurationDTO dto = new ProfileConfigurationDTO();
        dto.setProfileName("profile")
                .setProfileID("id")
                .setDomain("domain")
                .setXsdPath("xsdpath")
                .setXsltPath("xsltpath")
                .setSchematronPath("schematronpath")
                .setCacheEnabled(false);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dto);
        System.out.println(json);
        ProfileConfigurationDTO object = mapper.readValue(json, ProfileConfigurationDTO.class);
        assertEquals(dto.getProfileName(),object.getProfileName());
        assertEquals(dto.getProfileID(),object.getProfileID());
        assertEquals(dto.getDomain(),object.getDomain());
        assertEquals(dto.getXsdPath(),object.getXsdPath());
        assertEquals(dto.getXsltPath(),object.getXsltPath());
        assertEquals(dto.getSchematronPath(),object.getSchematronPath());
        assertEquals(dto.getCacheEnabled(),object.getCacheEnabled());
    }

    @Test
    void deserializeProfileConfigurationDTOTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"profileID\":\"id\"," +
                "\"profileName\":\"profile\"," +
                "\"domain\":\"domain\"," +
                "\"xsdPath\":\"xsdpath\"," +
                "\"xsltPath\":\"xsltpath\"," +
                "\"schematronPath\":\"schematronpath\"," +
                "\"cacheEnabled\":\"false\"}";
        ProfileConfigurationDTO object = mapper.readValue(json, ProfileConfigurationDTO.class);
        assertEquals("profile",object.getProfileName());
        assertEquals("id",object.getProfileID());
        assertEquals("domain",object.getDomain());
        assertEquals("xsdpath",object.getXsdPath());
        assertEquals("xsltpath",object.getXsltPath());
        assertEquals("schematronpath",object.getSchematronPath());
        assertFalse(object.getCacheEnabled());
        assertEquals(0, object.getValidationProfile().getCoveredItems().size());
    }

    @Test
    void deserializeWithoutCoveredItemsProfileConfigurationDTOTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"profileID\":\"id\"," +
                "\"profileName\":\"profile\"," +
                "\"domain\":\"domain\"," +
                "\"xsdPath\":\"xsdpath\"," +
                "\"xsltPath\":\"xsltpath\"," +
                "\"schematronPath\":\"schematronpath\"," +
                "\"cacheEnabled\":\"false\"," +
                "\"validationProfile\":{\"profileID\":\"id\",\"profileName\":\"profile\",\"domain\":\"domain\"}}";
        ProfileConfigurationDTO object = mapper.readValue(json, ProfileConfigurationDTO.class);
        assertEquals("profile",object.getProfileName());
        assertEquals("id",object.getProfileID());
        assertEquals("domain",object.getDomain());
        assertEquals("xsdpath",object.getXsdPath());
        assertEquals("xsltpath",object.getXsltPath());
        assertEquals("schematronpath",object.getSchematronPath());
        assertEquals(false,object.getCacheEnabled());
        assertEquals(0, object.getValidationProfile().getCoveredItems().size());
    }

    @Test
    public void deserializeWrongFieldProfileConfigurationDTOTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String json = "{\"profileID\":\"id\"," +
                "\"Wrong\":\"wrong\"," +
                "\"profileName\":\"profile\"," +
                "\"domain\":\"domain\"," +
                "\"xsdPath\":\"xsdpath\"," +
                "\"xsltPath\":\"xsltpath\"," +
                "\"schematronPath\":\"schematronpath\"," +
                "\"cacheEnabled\":\"false\"}";
        Throwable throwable = assertThrows(UnrecognizedPropertyException.class, () -> mapper.readValue(json, ProfileConfigurationDTO.class));
        assertTrue(throwable.getMessage().contains("Unrecognized field \"Wrong\""));
    }
}

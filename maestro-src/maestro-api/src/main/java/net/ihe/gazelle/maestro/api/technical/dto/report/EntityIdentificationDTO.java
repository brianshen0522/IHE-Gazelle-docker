/*
 * Copyright 2025-2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.maestro.api.technical.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.maestro.api.business.testreport.EntityIdentification;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
        name = "EntityIdentification",
        description = "The name and version used to identify an entity."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version",
        "name"
})
public class EntityIdentificationDTO implements DTO<EntityIdentification> {

    @JsonIgnore
    private final EntityIdentification entityIdentification;

    public EntityIdentificationDTO() {
        this(new EntityIdentification());
    }

    public EntityIdentificationDTO(EntityIdentification entityIdentification) {
        this.entityIdentification = entityIdentification;
    }

    @Override
    @JsonIgnore
    public EntityIdentification getBusinessObject() {
        return entityIdentification;
    }

    @Schema(
            name = "version",
            description = "The version of the entity.",
            examples = "1.0"
    )
    @JsonProperty(value = "version")
    public String getVersion() {
        return entityIdentification.getVersion();
    }

    public void setVersion(String version) {
       entityIdentification.setVersion(version);
    }

    @Schema(
            name = "name",
            description = "The name of the entity.",
            examples = "My service",
            required = true
    )
    @JsonProperty(value = "name")
    public String getName() {
        return entityIdentification.getName();
    }

    public void setName(String name) {
       entityIdentification.setName(name);
    }
}

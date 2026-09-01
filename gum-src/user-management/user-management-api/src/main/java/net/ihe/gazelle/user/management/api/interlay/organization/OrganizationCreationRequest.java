/*
 * Copyright 2024-2026 IHE International.
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

package net.ihe.gazelle.user.management.api.interlay.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;
@Schema(name = "OrganizationCreationRequest", description = "Data Transfer Object for creating an organization in Gazelle User Management.")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"shortname", "name"})
public class OrganizationCreationRequest {
    /**
     * Shortname of the organization.
     */
    private String shortname;
    /**
     * Name of the organization.
     */
    private String name;

    /**
     * Creates an empty registration request.
     */
    public OrganizationCreationRequest() {
        // Default constructor
    }

    /**
     * Gets the organization shortname.
     *
     * @return the organization shortname
     */
    @JsonProperty("shortname")
    public String getShortname() {
        return shortname;
    }

    /**
     * Sets the organization shortname.
     *
     * @param shortname the organization shortname
     */
    public OrganizationCreationRequest setShortname(String shortname) {
        this.shortname = shortname;
        return this;
    }

    /**
     * Gets the organization name.
     *
     * @return the organization name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Sets the organization name.
     *
     * @param name the organization name
     */
    public OrganizationCreationRequest setName(String name) {
        this.name = name;
        return this;
    }



    public Organization asOrganization() {
        return new Organization()
                .setShortname(getShortname())
                .setName(getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationCreationRequest that = (OrganizationCreationRequest) o;
        return Objects.equals(shortname, that.shortname) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortname, name);
    }
}

/*
 * Copyright 2024 IHE International.
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

package net.ihe.gazelle.user.management.api.interlay.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.interlay.organization.OrganizationCreationRequest;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;

/**
 * Data Transfer Object for user registration requests in Gazelle User Management.
 * <p>
 * This class encapsulates all information required to create a new user, including personal details,
 * organization or organization ID (depends on if we join or create organization).
 * </p>
 */
@Schema(name = "UserCreationRequest", description = "Data Transfer Object for user creation requests in Gazelle User Management.")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"firstName", "lastName", "email", "organizationId", "organization"})
public class UserCreationRequest {
    /**
     * The user's first name.
     */
    private String firstName;
    /**
     * The user's last name.
     */
    private String lastName;
    /**
     * The user's email address.
     */
    private String email;
    /**
     * The ID of the organization the user belongs to.
     */
    private String organizationId;
    /**
     * The organization object, if available.
     */
    private OrganizationCreationRequest organization;

    /**
     * Creates an empty registration request.
     */
    public UserCreationRequest() {
        // Default constructor
    }

    /**
     * Gets the user's first name.
     *
     * @return the first name
     */
    @JsonProperty(value = "firstName")
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the user's last name.
     *
     * @return the last name
     */
    @JsonProperty(value = "lastName")
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the user's email address.
     *
     * @return the email address
     */
    @JsonProperty(value = "email")
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the organization ID.
     *
     * @return the organization ID
     */
    @JsonProperty(value = "organizationId")
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the organization ID.
     *
     * @param organizationId the organization ID
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Gets the organization object.
     *
     * @return the organization
     */
    @JsonProperty(value = "organization")
    public OrganizationCreationRequest getOrganization() {
        return organization;
    }

    /**
     * Sets the organization object.
     *
     * @param organization the organization
     */
    public void setOrganization(OrganizationCreationRequest organization) {
        this.organization = organization;
    }

    /**
     * Converts this request into a User domain object.
     *
     * @return a user populated with request fields
     */
    public User asUser() {
        User user = new User();
        user.setFirstName(getFirstName());
        user.setLastName(getLastName());
        user.setEmail(getEmail());
        user.setEmail(getEmail());
        user.setOrganizationId(getOrganizationId());
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCreationRequest that = (UserCreationRequest) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName)
                && Objects.equals(email, that.email) && Objects.equals(organizationId, that.organizationId)
                && Objects.equals(organization, that.organization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, organizationId, organization);
    }

    @Override
    public String toString() {
        return "UserRegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", organizationId='" + organizationId + '\'' +
                ", organization=" + organization +
                '}';
    }
}

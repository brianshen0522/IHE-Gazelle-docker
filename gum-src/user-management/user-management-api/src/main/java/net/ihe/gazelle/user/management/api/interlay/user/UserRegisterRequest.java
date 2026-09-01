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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.interlay.organization.OrganizationCreationRequest;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;

/**
 * Data Transfer Object for user registration requests in Gazelle User Management.
 * <p>
 * This class encapsulates all information required to register a new user, including personal details,
 * organization or organization ID (depends on if we join or create organization).
 * And password and consent.
 * </p>
 */
@Schema(name = "UserRegisterRequest", description = "Data Transfer Object for user registration requests in Gazelle User Management.")
@JsonPropertyOrder({"firstName", "lastName", "email", "organizationId", "organization", "password", "passwordConfirmation", "consent"})
public class UserRegisterRequest {
    /** The user's first name. */
    private String firstName;
    /** The user's last name. */
    private String lastName;
    /** The user's email address. */
    private String email;
    /** The ID of the organization the user belongs to. */
    private String organizationId;
    /** The organization object, if available. */
    private OrganizationCreationRequest organization;
    /** The user's password. */
    private String password;
    /** The user's password confirmation. */
    private String passwordConfirmation;
    /** Whether the user has given consent. */
    private Boolean consent;

    /**
     * Creates an empty registration request.
     */
    public UserRegisterRequest() {
        // Default constructor
    }

    /**
     * Gets the user's first name.
     * @return the first name
     */
    @Schema(
            description = "The user first name.",
            required = true,
            examples = {"John"}
    )
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the user's last name.
     * @return the last name
     */
    @Schema(
            description = "The user last name.",
            required = true,
            examples = {"Doe"}
    )
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the user's email address.
     * @return the email address
     */
    @Schema(
            description = "The user email.",
            required = true,
            examples = {"john.doe@example.com"}
    )
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the organization ID.
     * @return the organization ID
     */
    @Schema(
            description = "The user organization id.",
            required = true,
            examples = {"b59fc01e-a3f4-457f-836f-d57830bacf71"}
    )
    @JsonProperty("organizationId")
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the organization ID.
     * @param organizationId the organization ID
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Gets the organization object.
     * @return the organization
     */
    @JsonProperty("organization")
    public OrganizationCreationRequest getOrganization() {
        return organization;
    }

    /**
     * Sets the organization object.
     * @param organization the organization
     */
    public void setOrganization(OrganizationCreationRequest organization) {
        this.organization = organization;
    }

    /**
     * Gets the user's password.
     * @return the password
     */
    @Schema(
            description = "The user password",
            required = true,
            examples = {"P@$$w0rd123"}
    )
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's password confirmation.
     * @return the password confirmation
     */
    @Schema(
            description = "The user password confirmation",
            required = true,
            examples = {"P@$$w0rd123"}
    )
    @JsonProperty("passwordConfirmation")
    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    /**
     * Sets the user's password confirmation.
     * @param passwordConfirmation the password confirmation
     */
    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    /**
     * Gets the user's consent status.
     * @return true if consent is given, false otherwise
     */
    @Schema(
            description = "The user password confirmation",
            required = true,
            examples = {"true"}
    )
    @JsonProperty("consent")
    public Boolean getConsent() {
        return consent;
    }

    /**
     * Sets the user's consent status.
     * @param consent true if consent is given, false otherwise
     */
    public void setConsent(Boolean consent) {
        this.consent = consent;
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
        UserRegisterRequest that = (UserRegisterRequest) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName)
                && Objects.equals(email, that.email) && Objects.equals(organizationId, that.organizationId)
                && Objects.equals(organization, that.organization) && Objects.equals(password, that.password)
                && Objects.equals(passwordConfirmation, that.passwordConfirmation) && Objects.equals(consent, that.consent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, organizationId, organization, password, passwordConfirmation, consent);
    }

    @Override
    public String toString() {
        return "UserRegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", organizationId='" + organizationId + '\'' +
                ", organization=" + organization +
                ", password='" + (password != null ? "****" : null) + '\'' +
                ", passwordConfirmation='" + (passwordConfirmation != null ? "****" : null) + '\'' +
                ", consent=" + consent +
                '}';
    }
}

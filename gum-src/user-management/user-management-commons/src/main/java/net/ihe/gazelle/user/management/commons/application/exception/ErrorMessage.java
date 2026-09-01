package net.ihe.gazelle.user.management.commons.application.exception;

/**
 * This Enum is to groups errors messages and avoid same hard coded String
 * If there are more than 3 uses in the same class, sonar put a warning
 */
public enum ErrorMessage {

    USER_NOT_FOUND("User not found"),
    ORGANIZATION_NOT_FOUND("Organization not found"),
    ORGANIZATION_DOES_NOT_EXIST("Organization does not exist"),
    ORGANIZATION_ALREADY_EXISTS("Organization already exists"),
    USER_ID_IS_NULL("userId is null"),
    USER_EMAIL_IS_NULL("Email is null"),
    ORGANIZATION_ID_IS_NULL("organizationId is null"),
    ORGANIZATION_CREATION_DISABLED("Organization creation is disabled"),
    USER_REGISTRATION_DISABLED("User registration is disabled"),
    CREDENTIALS_NOT_FOUND("Credentials not found for user"),
    FIRSTNAME_NOT_VALID("First name is not valid for Gazelle"),
    LASTNAME_NOT_VALID("Last name is not valid for Gazelle"),
    EMAIL_NOT_VALID("Email is not valid for Gazelle"),
    EMAIL_ALREADY_EXISTS("Email already exists"),
    PASSWORDS_NOT_EQUAL("Passwords are not equal"),
    PASSWORD_NOT_SECURE("The password is not secure enough (at least 8 characters, 1 uppercase, 1 lowercase, 1 number and 1 special character)");

    private final String message;

    /**
     * Constructor for ErrorMessage enum.
     *
     * @param message the error message associated with the enum constant
     */
    ErrorMessage(String message) {
        this.message = message;
    }

    /** Get the error message associated with the enum constant.
     *
     * @return the error message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the error message with an additional parameter for more context.
     * @param parameter the additional parameter to include in the message
     * @return the error message with the parameter included
     */
    public String getMessageWithParameter(String parameter) {
        return this.message + " (" + parameter + ").";
    }
}

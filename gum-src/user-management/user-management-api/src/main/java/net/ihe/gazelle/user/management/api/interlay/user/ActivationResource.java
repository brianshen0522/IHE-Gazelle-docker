package net.ihe.gazelle.user.management.api.interlay.user;

/**
 * Resource class representing user activation data.
 *
 * This class is used to transfer user activation information between
 * different layers of the application. It contains the user identifier
 * required for activation operations.
 *
 */
public class ActivationResource {

    /**
     * The unique identifier of the user to be activated.
     */
    private String userId;

    /**
     * Default constructor.
     *
     * Creates an empty ActivationResource instance. This constructor
     * is required for JSON deserialization with Jackson.
     */
    public ActivationResource() {
        // Empty constructor needed for Jackson.
    }

    /**
     * Constructor with user ID parameter.
     *
     * @param userId the unique identifier of the user to be activated.
     *               Must not be null or empty.
     */
    public ActivationResource(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the user identifier.
     *
     * @return the user ID as a String, or null if not set
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user identifier.
     *
     * @param userId the unique identifier of the user to be activated.
     *               Can be null to reset the value.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}

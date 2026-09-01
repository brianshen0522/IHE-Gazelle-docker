package net.ihe.gazelle.user.management.api.domain.user;

import java.util.Map;
import java.util.Objects;

/**
 * Class representing user credentials with password hashing parameters.
 *
 * This class encapsulates password information along with cryptographic
 * parameters such as salt and iterations used for secure password hashing.
 * It supports various hashing methods and provides utilities for parameter
 * management.
 *
 */
public class Credentials {

    /**
     * Key constant for salt parameter in credential maps.
     */
    public static final String SALT_KEY = "salt";

    /**
     * Key constant for iterations parameter in credential maps.
     */
    public static final String ITERATIONS_KEY = "iterations";

    /**
     * The hashed password value.
     */
    private String password;

    /**
     * The salt value used for password hashing.
     */
    private String salt;

    /**
     * The number of iterations used for password hashing.
     */
    private Integer iterations;

    /**
     * The name of the hash method used for password hashing.
     */
    private String hashMethodName;

    /**
     * Default constructor.
     *
     * Creates an empty Credentials instance. This constructor
     * is required for JSON deserialization with Jackson.
     */
    public Credentials() {
        // Empty constructor needed for Jackson.
    }

    /**
     * Constructor with password parameter.
     *
     * @param password the password value to set
     */
    public Credentials(String password) {
        this.password = password;
    }

    /**
     * Copy constructor.
     *
     * Creates a new Credentials instance by copying all values
     * from the provided credentials object.
     *
     * @param credentials the credentials object to copy from
     */
    public Credentials(Credentials credentials) {
        this.password = credentials.getPassword();
        this.salt = credentials.getSalt();
        this.iterations = credentials.getIterations();
        this.hashMethodName = credentials.getHashMethod();
    }

    /**
     * Gets the password value.
     *
     * @return the password, or null if not set
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password value.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the salt value used for password hashing.
     *
     * @return the salt value, or null if not set
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Sets the salt value used for password hashing.
     *
     * @param salt the salt value to set
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * Gets the number of iterations used for password hashing.
     *
     * @return the number of iterations, or null if not set
     */
    public Integer getIterations() {
        return iterations;
    }

    /**
     * Sets the number of iterations used for password hashing.
     *
     * @param iterations the number of iterations to set
     */
    public void setIterations(Integer iterations) {
        this.iterations = iterations;
    }

    /**
     * Gets the hash method name.
     *
     * @return the hash method name, or null if not set
     */
    public String getHashMethod() {
        return hashMethodName;
    }

    /**
     * Sets the hash method name.
     *
     * @param hashMethodName the hash method name to set
     */
    public void setHashMethod(String hashMethodName) {
        this.hashMethodName = hashMethodName;
    }

    /**
     * Generates a map containing the cryptographic parameters.
     *
     * Creates a map with salt and iterations parameters that can be
     * used for password hashing operations.
     *
     * @return a map containing salt and iterations parameters
     */
    public Map<String, String> generateMapOfParameters() {
        return Map.of(SALT_KEY, salt, ITERATIONS_KEY, String.valueOf(iterations));
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credentials that = (Credentials) o;
        return Objects.equals(password, that.password) && Objects.equals(salt, that.salt) && Objects.equals(iterations, that.iterations) && Objects.equals(hashMethodName, that.hashMethodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(password, salt, iterations, hashMethodName);
    }
}

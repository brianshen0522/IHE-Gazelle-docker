package net.ihe.gazelle.user.management.commons.interlay.exceptions;

import java.util.Objects;

/**
 * Class representing the body of an error response.
 */
public class ErrorResponseBody {
    private String error;
    private String message;
    private int code;

    /**
     * Creates a new error response body with the given error, message and code.
     * @param error the error type
     * @param message the error message
     * @param code the HTTP status code associated with the error
     */
    public ErrorResponseBody(String error, String message, int code) {
        this.error = error;
        this.message = message;
        this.code = code;
    }

    /** Returns the error type.
     * @return the error */
    public String getError() {
        return error;
    }

    /** Returns the error type.
     * @param error the error to set */
    public void setError(String error) {
        this.error = error;
    }

    /** Returns the error message.
     * @return the message */
    public String getMessage() {
        return message;
    }

    /** Sets the error message.
     * @param message the message to set */
    public void setMessage(String message) {
        this.message = message;
    }

    /** Returns the HTTP status code associated with the error.
     * @return the code */
    public int getCode() {
        return code;
    }

    /** Sets the HTTP status code associated with the error.
     * @param code the code to set */
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ErrorResponseBody that = (ErrorResponseBody) object;
        return code == that.code
                && Objects.equals(error, that.error)
                && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, message, code);
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", code=" + code +
                '}';
    }
}

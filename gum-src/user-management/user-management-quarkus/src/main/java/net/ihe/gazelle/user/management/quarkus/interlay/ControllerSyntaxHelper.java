package net.ihe.gazelle.user.management.quarkus.interlay;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.user.management.api.application.GazelleServiceException;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.ErrorResponseBody;
import org.slf4j.Logger;

import java.util.NoSuchElementException;

/**
 * Utility helper for controller response handling.
 */
public class ControllerSyntaxHelper {

    /** Logger pattern for two values. */
    public static final String DOUBLE_DATA_INPUT_LOGGER = "{} {}";

    /**
     * Action wrapper used to execute controller logic.
     */
    @FunctionalInterface
    public interface Action {
        /**
         * Executes the action and returns a response.
         * @return the response
         */
        Response run();
    }

    /**
     * Utility class, not meant to be instantiated.
     */
    private ControllerSyntaxHelper() {
        // Utility class
    }

    /**
     * Executes the action and maps common exceptions to HTTP responses.
     * @param identity current identity
     * @param logger logger to use for warnings
     * @param message error message prefix
     * @param action action to execute
     * @return the response from the action or an error response
     */
    public static Response executeActionAndCatchPotentialException(GazelleIdentity identity, Logger logger, String message, final Action action) {
        try {
            return action.run();
        } catch (UnauthorizedException e) {
            return getUnauthorizedOrForbiddenResponse(identity, e, logger);
        } catch (NoSuchElementException e) {
            logger.warn(DOUBLE_DATA_INPUT_LOGGER, message, e.getMessage());
            return getErrorResponseWithMessage(Response.Status.NOT_FOUND, message, e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn(DOUBLE_DATA_INPUT_LOGGER, message, e.getMessage());
            return getErrorResponseWithMessage(Response.Status.NOT_ACCEPTABLE, message, e.getMessage());
        } catch (IllegalArgumentException | GazelleServiceException e) {
            logger.warn(DOUBLE_DATA_INPUT_LOGGER, message, e.getMessage());
            return getErrorResponseWithMessage(Response.Status.BAD_REQUEST, message, e.getMessage());
        } catch (ConflictException e) {
            logger.warn(DOUBLE_DATA_INPUT_LOGGER, message, e.getMessage());
            return getErrorResponseWithMessage(Response.Status.CONFLICT, message, e.getMessage());
        } catch (Exception e) {
            logger.warn(DOUBLE_DATA_INPUT_LOGGER, message, e.getMessage());
            return getErrorResponseWithMessage(Response.Status.INTERNAL_SERVER_ERROR, message, e.getMessage());
        }
    }

    /**
     * Builds an error response with the provided details.
     * @param status response status
     * @param error error title
     * @param message error message
     * @return error response
     */
    public static Response getErrorResponseWithMessage(Response.Status status, String error, String message) {
        ErrorResponseBody errorResponseBody = new ErrorResponseBody(error, message, status.getStatusCode());
        return Response.status(status).entity(errorResponseBody).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Builds an unauthorized or forbidden response based on identity state.
     * @param identity current identity
     * @param e unauthorized exception
     * @param logger logger to use for warnings
     * @return unauthorized or forbidden response
     */
    public static Response getUnauthorizedOrForbiddenResponse(GazelleIdentity identity, UnauthorizedException e, Logger logger) {
        if (identity == null || !identity.isAuthenticated()) {
            logger.warn("Current identity is unauthorized", e);
            return getErrorResponseWithMessage(Response.Status.UNAUTHORIZED, "Unauthorized", e.getMessage());
        } else {
            logger.warn("Current identity is forbidden", e);
            return getErrorResponseWithMessage(Response.Status.FORBIDDEN, "Forbidden", e.getMessage());
        }
    }
}

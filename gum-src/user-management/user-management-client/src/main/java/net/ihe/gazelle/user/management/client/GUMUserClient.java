package net.ihe.gazelle.user.management.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.ihe.gazelle.m2m.client.technical.filter.M2MAuthMethod;
import net.ihe.gazelle.user.management.api.interlay.user.UserResource;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * Client interface for interacting with the Gazelle User Management API to retrieve user information.
 */
@Path("/users")
@RegisterRestClient(configKey = "gzl-gum-api")
public interface GUMUserClient {

    /**
     * Retrieves a list of users from the Gazelle User Management API.
     * @return a list of UserResource objects representing the users in the system
     */
    @GET
    @ClientHeaderParam(name = "Accept", value = "application/json")
    @M2MAuthMethod()
    List<UserResource> getUsers();

}

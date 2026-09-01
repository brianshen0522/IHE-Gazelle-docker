package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.user.management.api.domain.configuration.ConfigurationsResource;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST endpoints exposing application configuration.
 */
@Path("/rest")
@Tag(name = "Application configuration", description = "Application configuration API.")
public interface ApplicationController {

    /**
     * Retrieve GUM microservice configurations.
     *
     * @return configurations response
     */
    @GET
    @Path("configurations")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve GUM microservice configurations", description = "Get all the configurations regarding GUM microservice.")
    @APIResponse(responseCode = "200", description = "GUM microservice configurations",
            content = @Content(mediaType = "application/json",schema = @Schema(implementation = ConfigurationsResource.class))
    )
    Response getConfigurations();

}

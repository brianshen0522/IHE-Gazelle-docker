/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.user.management.quarkus.interlay.controller.organization;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.interlay.organization.OrganizationDto;
import net.ihe.gazelle.user.management.api.interlay.organization.OrganizationCreationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.ihe.gazelle.user.management.quarkus.interlay.ControllerSyntaxHelper.executeActionAndCatchPotentialException;

/**
 * Controller implementation for managing organizations.
 */
@RequestScoped
public class OrganizationControllerImpl implements OrganizationController {

    private final Logger logger = LoggerFactory.getLogger(OrganizationControllerImpl.class.getName());
    private final OrganizationLookupService organizationLookupService;
    private final OrganizationManagementService organizationManagementService;
    private final GazelleIdentity identity;

    /**
     * Creates a controller instance wired with the required services.
     *
     * @param organizationLookupService service handling organization lookup operations
     */
    @Inject
    public OrganizationControllerImpl(OrganizationLookupService organizationLookupService, OrganizationManagementService organizationManagementService, GazelleIdentity identity) {
        this.organizationLookupService = organizationLookupService;
        this.organizationManagementService = organizationManagementService;
        this.identity = identity;
    }

    @Override
    public Response getOrganizationsById(String organizationId) {
        return executeActionAndCatchPotentialException(identity, logger, "Unable get organizations by ID", () -> {
            Organization organization = organizationLookupService.getOrganizationById(organizationId);
            return Response.ok().status(Response.Status.OK).entity(new OrganizationDto(organization)).build();
        });
    }

    @Override
    public Response createOrganization(OrganizationCreationRequest organizationCreationRequest) {
        return executeActionAndCatchPotentialException(identity, logger, "Unable to create organization", () -> {
            Organization registeredOrganization = organizationManagementService.createOrganization(organizationCreationRequest.asOrganization(), identity);
            return Response.ok().status(Response.Status.CREATED).entity(new OrganizationDto(registeredOrganization)).build();
        });
    }

    @Override
    public Response patchOrganization(String organizationId, OrganizationCreationRequest organizationCreationRequest) {
        return executeActionAndCatchPotentialException(identity, logger, "Unable to patch organization", () -> {
            Organization organization = organizationManagementService.updateOrganization(organizationId, organizationCreationRequest.asOrganization(), identity);
            return Response.ok().status(Response.Status.OK).entity(new OrganizationDto(organization)).build();
        });
    }

    @Override
    public Response archiveOrganization(String organizationId) {
        return executeActionAndCatchPotentialException(identity, logger, "Unable to archive organization", () -> {
            organizationManagementService.archiveOrganization(organizationId, identity);
            return Response.ok().status(Response.Status.OK).build();
        });
    }
}
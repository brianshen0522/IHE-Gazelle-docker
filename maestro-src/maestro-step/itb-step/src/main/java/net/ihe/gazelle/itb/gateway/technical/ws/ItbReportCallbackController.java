/*
 * Copyright 2025-2026 IHE International.
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

package net.ihe.gazelle.itb.gateway.technical.ws;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.itb.gateway.business.CatchItbReportServiceException;
import net.ihe.gazelle.itb.gateway.business.ItbReportingService;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;

import java.util.NoSuchElementException;

/**
 * Adapter to push test report to TM
 */
@Path("/itb")
public class ItbReportCallbackController {

    private final ItbReportingService itbReportingService;

    /**
     * Creates callback controller.
     *
     * @param itbReportingService service handling ITB callbacks
     */
    @Inject
    public ItbReportCallbackController(ItbReportingService itbReportingService) {
        this.itbReportingService = itbReportingService;
    }

    /**
     * Method to trigger report generation
     *
     * @param itbReporting ITB callback payload
     * @return HTTP response describing callback handling result
     */
    @POST
    @Path("/report")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerReportGeneration(ItbReporting itbReporting) {
        try {
           itbReportingService.receiveReporting(itbReporting);
           return Response.ok().build();
        } catch (NoSuchElementException e) {
           return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (CatchItbReportServiceException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad format for test report. Error message : " + e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error while manipulating test sessions. Error message : " + e.getMessage()).build();
        }
    }
}

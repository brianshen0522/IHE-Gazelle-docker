package net.ihe.gazelle.validation.gateway.migration.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationError;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationStatus;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationSummary;
import net.ihe.gazelle.validation.gateway.migration.engine.MigrationContext;
import net.ihe.gazelle.validation.gateway.migration.engine.MigrationEngine;
import net.ihe.gazelle.validation.gateway.migration.engine.MigrationMode;
import net.ihe.gazelle.validation.gateway.migration.engine.VerificationService;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsDatabaseHealth;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsSourceService;
import net.ihe.gazelle.validation.gateway.migration.dto.IncrementalMigrationStats;
import net.ihe.gazelle.validation.gateway.migration.output.PersistedMigrationState;
import net.ihe.gazelle.validation.gateway.migration.output.MigrationTargetService;
import net.ihe.gazelle.validation.gateway.migration.output.TargetHealth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@Path("/api/migration")
@Produces(MediaType.APPLICATION_JSON)
public class MigrationController {

   private final MigrationEngine migrationEngine;
   private final EvsSourceService sourceService;
   private final VerificationService verificationService;
   private final MigrationTargetService targetService;
   private final ObjectMapper objectMapper;

   public MigrationController(MigrationEngine migrationEngine,
                              EvsSourceService sourceService,
                              VerificationService verificationService,
                              MigrationTargetService targetService) {
      this.migrationEngine = migrationEngine;
      this.sourceService = sourceService;
      this.verificationService = verificationService;
      this.targetService = targetService;
      this.objectMapper = new ObjectMapper();
   }

   @GET
   @Path("/status")
   public MigrationStatus status() {
      EvsDatabaseHealth dbHealth = sourceService.checkDatabaseHealth();
      TargetHealth targetHealth = targetService.checkHealth();
      boolean migrationCompleted = false;
      java.time.Instant completedAt = null;
      String message = migrationEngine.getLastMessage();
      boolean newReportsCountKnown = false;
      long newReportsCount = 0L;
      java.time.Instant oldestNewReportDate = null;
      java.time.Instant newestNewReportDate = null;
      try {
         migrationCompleted = migrationEngine.isMigrationCompleted();
         completedAt = migrationEngine.getCompletedAt();
         if (dbHealth.accessible() && targetHealth.accessible()) {
            PersistedMigrationState state = targetService.readMigrationState().orElse(null);
            if (state != null && state.lastSuccessfulCheckpoint() != null) {
               IncrementalMigrationStats incrementalStats = sourceService.loadIncrementalStats(state.lastSuccessfulCheckpoint());
               newReportsCount = incrementalStats.newReportsCount();
               oldestNewReportDate = incrementalStats.oldestNewReportDate();
               newestNewReportDate = incrementalStats.newestNewReportDate();
               newReportsCountKnown = true;
            }
         }
      } catch (Exception e) {
         message = message + " | Migration marker unavailable: " + e.getMessage();
      }
      return new MigrationStatus(
            migrationEngine.getState().name(),
            migrationCompleted,
            completedAt,
            message,
            dbHealth.accessible(),
            dbHealth.message(),
            targetService.getTargetType(),
            targetHealth.accessible(),
            targetHealth.message(),
            newReportsCountKnown,
            newReportsCount,
            oldestNewReportDate,
            newestNewReportDate
      );
   }

   @GET
   @Path("/stats")
   public Response stats() {
      ensureEvsDatabaseAccessible();
      return Response.ok(sourceService.loadStats()).build();
   }

   @POST
   @Path("/start")
   public Response start(@QueryParam("freezeConfirmed") boolean freezeConfirmed,
                         @QueryParam("rerunConfirmed") boolean rerunConfirmed,
                         @QueryParam("retryFailedOnly") boolean retryFailedOnly,
                         @QueryParam("mode") String modeParam,
                         @QueryParam("ignoreAllMissingInputs") @DefaultValue("false") boolean ignoreAllMissingInputs,
                         String requestBody) {
      ensureTargetAccessible();
      ensureEvsDatabaseAccessible();

      MigrationMode mode = resolveMode(rerunConfirmed, retryFailedOnly, modeParam);

      // Parse ignore configuration from request body
      Set<String> specificIgnoredOids = parseIgnoredOids(requestBody);
      MigrationContext context = new MigrationContext(ignoreAllMissingInputs, specificIgnoredOids);

      try {
         migrationEngine.startMigration(freezeConfirmed, mode, context);
         return Response.accepted().build();
      } catch (IllegalArgumentException e) {
         throw new BadRequestException(e.getMessage());
      } catch (IllegalStateException e) {
         throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
      }
   }

   private MigrationMode resolveMode(boolean rerunConfirmed, boolean retryFailedOnly, String modeParam) {
      if (modeParam != null && !modeParam.isBlank()) {
         if (rerunConfirmed || retryFailedOnly) {
            throw new BadRequestException("Cannot combine mode with rerunConfirmed or retryFailedOnly");
         }
         try {
            return MigrationMode.valueOf(modeParam.trim().toUpperCase(java.util.Locale.ROOT));
         } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid mode: " + modeParam);
         }
      }
      if (rerunConfirmed && retryFailedOnly) {
         throw new BadRequestException("Cannot specify both rerunConfirmed and retryFailedOnly");
      }
      if (retryFailedOnly) {
         return MigrationMode.RETRY_FAILED_ONLY;
      }
      return MigrationMode.FULL;
   }

   @GET
   @Path("/progress")
   public Response progress() {
      return Response.ok(migrationEngine.getProgress()).build();
   }

   @GET
   @Path("/summary")
   public Response summary() {
      var progress = migrationEngine.getProgress();
      MigrationSummary summary = new MigrationSummary(
            migrationEngine.isMigrationCompleted(),
            migrationEngine.getCompletedAt(),
            progress.total(),
            progress.succeeded(),
            progress.failed(),
            progress.previews(),
            progress.recentErrors()
      );
      return Response.ok(summary).build();
   }

   @GET
   @Path("/failed-count")
   public Response failedCount() {
      try {
         long count = targetService.countFailedReports();
         return Response.ok(new FailedCountResponse((int) count, count > 0)).build();
      } catch (Exception e) {
         return Response.ok(new FailedCountResponse(0, false)).build();
      }
   }

   public record FailedCountResponse(int count, boolean hasFailures) {
   }

   @GET
   @Path("/failed-reports")
   public Response failedReports(@QueryParam("offset") @DefaultValue("0") int offset,
                                  @QueryParam("limit") @DefaultValue("20") int limit) {
      try {
         var page = targetService.readFailedReports(offset, limit);
         return Response.ok(new FailedReportsResponse(page.errors(), (int) page.total())).build();
      } catch (Exception e) {
         return Response.ok(new FailedReportsResponse(List.of(), 0)).build();
      }
   }

   public record FailedReportsResponse(List<MigrationError> errors, int total) {
   }

   @GET
   @Path("/verification")
   public Response verification(@QueryParam("count") Integer count) {
      ensureTargetAccessible();
      ensureEvsDatabaseAccessible();
      int requestedCount = count == null ? 10 : count;
      return Response.ok(verificationService.spotCheck(requestedCount)).build();
   }

   @GET
   @Path("/items/{itemId}")
   public Response item(@PathParam("itemId") String itemId) {
      ensureTargetAccessible();
      try {
         return Response.ok(verificationService.readMigratedItem(itemId)).build();
      } catch (IllegalArgumentException e) {
         throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
      }
   }

   @GET
   @Path("/items/{itemId}/input")
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response downloadInput(@PathParam("itemId") String itemId) {
      ensureTargetAccessible();
      try {
         var download = verificationService.readMigratedInputAttachment(itemId);
         StreamingOutput output = stream -> download.writer().writeTo(stream);
         return Response.ok(output)
               .type(download.contentType())
               .header("Content-Disposition", "attachment; filename=\"" + safeFilename(download.filename()) + "\"")
               .build();
      } catch (UnsupportedOperationException e) {
         throw new WebApplicationException(e.getMessage(), Response.Status.NOT_IMPLEMENTED);
      } catch (IllegalArgumentException e) {
         throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
      }
   }

   @GET
   @Path("/items/{itemId}/inputs")
   public Response listInputs(@PathParam("itemId") String itemId) {
      ensureTargetAccessible();
      try {
         List<InputAttachmentInfoResponse> inputs = verificationService.readMigratedInputAttachments(itemId).stream()
               .map(input -> new InputAttachmentInfoResponse(
                     input.attachmentId(),
                     input.filename(),
                     input.contentType()
               ))
               .toList();
         return Response.ok(inputs).build();
      } catch (UnsupportedOperationException e) {
         throw new WebApplicationException(e.getMessage(), Response.Status.NOT_IMPLEMENTED);
      } catch (IllegalArgumentException e) {
         throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
      }
   }

   @GET
   @Path("/items/{itemId}/inputs/{attachmentId}")
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response downloadInputByAttachmentId(@PathParam("itemId") String itemId,
                                               @PathParam("attachmentId") String attachmentId) {
      ensureTargetAccessible();
      try {
         var download = verificationService.readMigratedInputAttachment(itemId, attachmentId);
         StreamingOutput output = stream -> download.writer().writeTo(stream);
         return Response.ok(output)
               .type(download.contentType())
               .header("Content-Disposition", "attachment; filename=\"" + safeFilename(download.filename()) + "\"")
               .build();
      } catch (UnsupportedOperationException e) {
         throw new WebApplicationException(e.getMessage(), Response.Status.NOT_IMPLEMENTED);
      } catch (IllegalArgumentException e) {
         throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
      }
   }

   public record InputAttachmentInfoResponse(String attachmentId, String filename, String contentType) {
   }

   private void ensureEvsDatabaseAccessible() {
      EvsDatabaseHealth dbHealth = sourceService.checkDatabaseHealth();
      if (!dbHealth.accessible()) {
         throw new WebApplicationException(
               Response.status(Response.Status.SERVICE_UNAVAILABLE)
                     .entity(dbHealth.message())
                     .type(MediaType.TEXT_PLAIN)
                     .build()
         );
      }
   }

   private void ensureTargetAccessible() {
      TargetHealth health = targetService.checkHealth();
      if (!health.accessible()) {
         throw new WebApplicationException(
               Response.status(Response.Status.SERVICE_UNAVAILABLE)
                     .entity(health.message())
                     .type(MediaType.TEXT_PLAIN)
                     .build()
         );
      }
   }

   private String safeFilename(String filename) {
      if (filename == null || filename.isBlank()) {
         return "input.bin";
      }
      return filename.replace("\\", "_").replace("/", "_").replace("\"", "_");
   }

   private Set<String> parseIgnoredOids(String requestBody) {
      if (requestBody == null || requestBody.isBlank()) {
         return Set.of();
      }
      try {
         @SuppressWarnings("unchecked")
         Map<String, Object> payload = objectMapper.readValue(requestBody, Map.class);
         Object ignoredOids = payload.get("ignoredOids");
         if (ignoredOids instanceof List<?> list) {
            Set<String> oids = new HashSet<>();
            for (Object item : list) {
               if (item instanceof String oid) {
                  oids.add(oid);
               }
            }
            return oids;
         }
         return Set.of();
      } catch (Exception e) {
         return Set.of();
      }
   }
}

package net.ihe.gazelle.validation.gateway.migration.evs;

import net.ihe.gazelle.validation.gateway.migration.dto.PreMigrationStats;
import net.ihe.gazelle.validation.gateway.migration.dto.IncrementalMigrationStats;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationCheckpoint;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class EvsSourceService {

   private static final String BASE_SELECT = """
         SELECT v.id,
                p.oid,
                p.validation_date,
                v.status,
                v.validation_type,
                v.validation_service,
                v.validation_service_version,
                v.validator_keyword,
                v.validator_version,
                om.username,
                om.organization,
                sm.is_private,
                sm.privacy_key,
                cm.entrypoint,
                vr.archive_path AS validation_report_archive_path
         FROM evsc_validation v
         JOIN evsc_processing p ON p.id = v.id
         LEFT JOIN evsc_owner_metadata om ON om.id = p.owner_id
         LEFT JOIN evsc_sharing_metadata sm ON sm.id = p.sharing_id
         LEFT JOIN evsc_caller_metadata cm ON cm.id = p.caller_id
         LEFT JOIN evsc_report vr ON vr.id = v.validation_report_id
         """;

   @Inject
   DataSource dataSource;

   public EvsDatabaseHealth checkDatabaseHealth() {
      String sql = "SELECT 1";
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql);
           ResultSet rs = stmt.executeQuery()) {
         if (rs.next()) {
            return new EvsDatabaseHealth(true, "EVS database reachable");
         }
         return new EvsDatabaseHealth(false, "EVS database check returned no result");
      } catch (Exception e) {
         return new EvsDatabaseHealth(false, "EVS database is not accessible: " + e.getMessage());
      }
   }

   public PreMigrationStats loadStats() {
      return new PreMigrationStats(
            countReports(),
            firstDate(),
            lastDate(),
            estimateTotalSizeBytes()
      );
   }

   public long countReports() {
      String sql = "SELECT COUNT(*) FROM evsc_validation";
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql);
           ResultSet rs = stmt.executeQuery()) {
         rs.next();
         return rs.getLong(1);
      } catch (Exception e) {
         throw new IllegalStateException("Failed to count EVS validations", e);
      }
   }

   public long countReportsAfter(MigrationCheckpoint checkpoint) {
      if (checkpoint == null || checkpoint.validationDate() == null || checkpoint.sourceId() == null) {
         return countReports();
      }
      String sql = """
            SELECT COUNT(*)
            FROM evsc_validation v
            JOIN evsc_processing p ON p.id = v.id
            WHERE p.validation_date > ?
               OR (p.validation_date = ? AND v.id > ?)
            """;
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql)) {
         Timestamp checkpointTs = Timestamp.from(checkpoint.validationDate());
         stmt.setTimestamp(1, checkpointTs);
         stmt.setTimestamp(2, checkpointTs);
         stmt.setInt(3, checkpoint.sourceId());
         try (ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
         }
      } catch (Exception e) {
         throw new IllegalStateException("Failed to count EVS validations after checkpoint", e);
      }
   }

   public IncrementalMigrationStats loadIncrementalStats(MigrationCheckpoint checkpoint) {
      if (checkpoint == null || checkpoint.validationDate() == null || checkpoint.sourceId() == null) {
         return new IncrementalMigrationStats(0, null, null);
      }
      String sql = """
            SELECT COUNT(*), MIN(p.validation_date), MAX(p.validation_date)
            FROM evsc_validation v
            JOIN evsc_processing p ON p.id = v.id
            WHERE p.validation_date > ?
               OR (p.validation_date = ? AND v.id > ?)
            """;
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql)) {
         Timestamp checkpointTs = Timestamp.from(checkpoint.validationDate());
         stmt.setTimestamp(1, checkpointTs);
         stmt.setTimestamp(2, checkpointTs);
         stmt.setInt(3, checkpoint.sourceId());
         try (ResultSet rs = stmt.executeQuery()) {
            rs.next();
            long count = rs.getLong(1);
            Timestamp oldest = rs.getTimestamp(2);
            Timestamp newest = rs.getTimestamp(3);
            return new IncrementalMigrationStats(
                  count,
                  oldest == null ? null : oldest.toInstant(),
                  newest == null ? null : newest.toInstant()
            );
         }
      } catch (Exception e) {
         throw new IllegalStateException("Failed to load incremental EVS stats after checkpoint", e);
      }
   }

   public List<EvsValidationSourceRow> fetchBatch(long offset, int limit) {
      String sql = BASE_SELECT + " ORDER BY p.validation_date ASC NULLS FIRST, v.id ASC OFFSET ? LIMIT ?";
      List<EvsValidationSourceRow> rows = new ArrayList<>();
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql)) {
         stmt.setLong(1, offset);
         stmt.setInt(2, limit);
         try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
               rows.add(mapRow(rs));
            }
         }
         return rows;
      } catch (Exception e) {
         throw new IllegalStateException("Failed to fetch EVS validation batch", e);
      }
   }

   public List<EvsValidationSourceRow> fetchBatchAfter(MigrationCheckpoint checkpoint, long offset, int limit) {
      if (checkpoint == null || checkpoint.validationDate() == null || checkpoint.sourceId() == null) {
         return fetchBatch(offset, limit);
      }
      String sql = BASE_SELECT + """
             WHERE p.validation_date > ?
                OR (p.validation_date = ? AND v.id > ?)
             ORDER BY p.validation_date ASC NULLS FIRST, v.id ASC
             OFFSET ? LIMIT ?
            """;
      List<EvsValidationSourceRow> rows = new ArrayList<>();
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql)) {
         Timestamp checkpointTs = Timestamp.from(checkpoint.validationDate());
         stmt.setTimestamp(1, checkpointTs);
         stmt.setTimestamp(2, checkpointTs);
         stmt.setInt(3, checkpoint.sourceId());
         stmt.setLong(4, offset);
         stmt.setInt(5, limit);
         try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
               rows.add(mapRow(rs));
            }
         }
         return rows;
      } catch (Exception e) {
         throw new IllegalStateException("Failed to fetch EVS validation batch after checkpoint", e);
      }
   }

   public List<EvsValidationSourceRow> fetchBatchByOids(Set<String> oids, long offset, int limit) {
      if (oids == null || oids.isEmpty()) {
         return List.of();
      }
      String sql = BASE_SELECT + """
             WHERE p.oid = ANY (?)
             ORDER BY p.validation_date ASC NULLS FIRST, v.id ASC
             OFFSET ? LIMIT ?
            """;
      List<EvsValidationSourceRow> rows = new ArrayList<>();
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql)) {
         stmt.setArray(1, cnx.createArrayOf("text", oids.toArray(String[]::new)));
         stmt.setLong(2, offset);
         stmt.setInt(3, limit);
         try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
               rows.add(mapRow(rs));
            }
         }
         return rows;
      } catch (Exception e) {
         throw new IllegalStateException("Failed to fetch EVS validation batch by OIDs", e);
      }
   }

   public List<EvsValidationSourceRow> fetchRandom(int limit) {
      String sql = BASE_SELECT + " ORDER BY random() LIMIT ?";
      List<EvsValidationSourceRow> rows = new ArrayList<>();
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql)) {
         stmt.setInt(1, limit);
         try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
               rows.add(mapRow(rs));
            }
         }
         return rows;
      } catch (Exception e) {
         throw new IllegalStateException("Failed to fetch random EVS validations", e);
      }
   }

   public List<EvsHandledObjectSource> fetchHandledObjects(int processingId) {
      String sql = """
            SELECT ho.id,
                   ho.role,
                   ho.original_file_name,
                   ho.file_path
            FROM evsc_processing_evsc_handled_object link
            JOIN evsc_handled_object ho ON ho.id = link.objects_id
            WHERE link.evsc_processing_id = ?
            ORDER BY ho.id ASC
            """;
      List<EvsHandledObjectSource> objects = new ArrayList<>();
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql)) {
         stmt.setInt(1, processingId);
         try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
               objects.add(new EvsHandledObjectSource(
                     rs.getInt("id"),
                     rs.getString("role"),
                     rs.getString("original_file_name"),
                     rs.getString("file_path")
               ));
            }
         }
         return objects;
      } catch (Exception e) {
         throw new IllegalStateException("Failed to fetch handled objects for processing id " + processingId, e);
      }
   }

   private EvsValidationSourceRow mapRow(ResultSet rs) throws Exception {
      Timestamp validationTs = rs.getTimestamp("validation_date");
      return new EvsValidationSourceRow(
            rs.getInt("id"),
            rs.getString("oid"),
            validationTs == null ? null : validationTs.toInstant(),
            rs.getString("status"),
            rs.getString("validation_type"),
            rs.getString("validation_service"),
            rs.getString("validation_service_version"),
            rs.getString("validator_keyword"),
            rs.getString("validator_version"),
            rs.getString("username"),
            rs.getString("organization"),
            (Boolean) rs.getObject("is_private"),
            rs.getString("privacy_key"),
            rs.getString("entrypoint"),
            rs.getString("validation_report_archive_path")
      );
   }

   private Instant firstDate() {
      return querySingleInstant("SELECT MIN(validation_date) FROM evsc_processing");
   }

   private Instant lastDate() {
      return querySingleInstant("SELECT MAX(validation_date) FROM evsc_processing");
   }

   private Instant querySingleInstant(String sql) {
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql);
           ResultSet rs = stmt.executeQuery()) {
         rs.next();
         Timestamp ts = rs.getTimestamp(1);
         return ts == null ? null : ts.toInstant();
      } catch (Exception e) {
         throw new IllegalStateException("Failed to load date from EVS source", e);
      }
   }

   private long estimateTotalSizeBytes() {
      return estimateSizeByPaths("SELECT archive_path FROM evsc_report WHERE archive_path IS NOT NULL", "EVS report")
            + estimateSizeByPaths("SELECT file_path FROM evsc_handled_object WHERE file_path IS NOT NULL", "EVS input file");
   }

   private long estimateSizeByPaths(String sql, String context) {
      Set<String> uniquePaths = new HashSet<>();
      try (Connection cnx = dataSource.getConnection();
           PreparedStatement stmt = cnx.prepareStatement(sql);
           ResultSet rs = stmt.executeQuery()) {
         while (rs.next()) {
            uniquePaths.add(rs.getString(1));
         }
      } catch (Exception e) {
         throw new IllegalStateException("Failed to estimate " + context + " size", e);
      }
      long total = 0;
      for (String path : uniquePaths) {
         if (path == null || path.isBlank()) {
            continue;
         }
         File file = new File(path);
         if (file.exists() && file.isFile()) {
            total += file.length();
         }
      }
      return total;
   }
}

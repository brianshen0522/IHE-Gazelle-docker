package net.ihe.gazelle.validation.gateway.migration.ws;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RootPathSlashRedirectRoute {

   @Inject
   Router router;

   private final String rootPath;

   public RootPathSlashRedirectRoute(
         @ConfigProperty(name = "quarkus.http.root-path", defaultValue = "/") String configuredRootPath) {
      this.rootPath = normalize(configuredRootPath);
   }

   void register(@Observes StartupEvent event) {
      if ("/".equals(rootPath)) {
         return;
      }
      router.route()
            .order(-1000)
            .handler(context -> {
               if (context.request().method() != HttpMethod.GET
                     || !rootPath.equals(context.request().path())) {
                  context.next();
                  return;
               }
               String target = rootPath + "/";
               String query = context.request().query();
               if (query != null && !query.isBlank()) {
                  target += "?" + query;
               }
               context.response()
                     .setStatusCode(308)
                     .putHeader("Location", target)
                     .end();
            });
   }

   private static String normalize(String value) {
      String trimmed = value == null || value.isBlank() ? "/" : value.trim();
      if (!trimmed.startsWith("/")) {
         trimmed = "/" + trimmed;
      }
      while (trimmed.length() > 1 && trimmed.endsWith("/")) {
         trimmed = trimmed.substring(0, trimmed.length() - 1);
      }
      return trimmed;
   }
}

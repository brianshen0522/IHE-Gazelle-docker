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

package net.ihe.gazelle.maestro.quarkus.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import net.ihe.gazelle.maestro.api.business.MaestroObserver;
import net.ihe.gazelle.maestro.api.business.message.ExecutionFinished;
import net.ihe.gazelle.maestro.api.business.message.InteractWithUser;
import net.ihe.gazelle.maestro.api.business.message.UserInteractionCompleted;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * This client is used to send the Test Report to TM once test set run execution ends
 * <br> It uses the callback URL and authorization token stored
 */
@ApplicationScoped
public class ApacheTestReportClient implements MaestroObserver {

   private static final Logger LOG = LoggerFactory.getLogger(ApacheTestReportClient.class);

   private final String callBackAuthorization;
   private final String callBackUrl;

   /**
    * Creates a new {@code ApacheTestReportClient} with the specified callback URL.
    *
    * @param callBackUrl the URL to which test reports will be sent
    */
   public ApacheTestReportClient(String callBackUrl) {
      this(null, callBackUrl);
   }

   /**
    * Creates a new {@code ApacheTestReportClient} with the specified callback URL and authorization.
    *
    * @param callBackAuthorization the authorization token or credentials to access the callback URL, may be {@code null}
    * @param callBackUrl           the URL to which test reports will be sent
    */
   public ApacheTestReportClient(String callBackAuthorization, String callBackUrl) {
      this.callBackAuthorization = callBackAuthorization;
      this.callBackUrl = callBackUrl;
   }

   @Override
   public CompletableFuture<UserInteractionCompleted> interactWithUser(InteractWithUser interactWithUser) {
      throw new UnsupportedOperationException("User interaction is not supported via REST API.");
   }

   @Override
   public void onExecutionFinished(ExecutionFinished executionFinished) {
      sendTestReport(executionFinished.getReport(), executionFinished.getReportLocation());
   }

   private void sendTestReport(TestReport testReport, String reportLocation) {
      try (CloseableHttpClient client = HttpClients.createDefault()) {
         HttpPost httpPost = new HttpPost(callBackUrl);
         if (callBackAuthorization != null) {
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, callBackAuthorization);
         }
         if (reportLocation != null) {
            httpPost.setHeader(HttpHeaders.LOCATION, reportLocation);
         }

         TextSerDes serDes = new JacksonSerDes();
         String json = serDes.serializeAsString(new TestReportDTO(testReport));

         httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
         try (HttpEntity entity = EntityBuilder.create()
               .setText(json)
               .build()) {
            httpPost.setEntity(entity);
            client.execute(httpPost, new BasicHttpClientResponseHandler());
         }
      } catch (Exception e) {
         LOG.error(e.getMessage(), e);
      }
   }
}

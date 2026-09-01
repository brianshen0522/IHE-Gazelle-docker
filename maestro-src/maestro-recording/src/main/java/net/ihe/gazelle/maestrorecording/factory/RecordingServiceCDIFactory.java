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

package net.ihe.gazelle.maestrorecording.factory;

import com.kereval.gazelle.datahouse.technical.rest.client.RecordItemClientImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import net.ihe.gazelle.maestrorecording.adapter.DatahouseTestReportRecordingService;
import net.ihe.gazelle.maestrorecording.adapter.SPIStepOutputPlanDissectorProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * CDI factory for producing {@link DatahouseTestReportRecordingService} instances.
 */
@ApplicationScoped
public class RecordingServiceCDIFactory {

   @ConfigProperty(name = "datahouse.url")
   String datahouseUrl;

   private final SPIStepOutputPlanDissectorProvider dissectorProvider = new SPIStepOutputPlanDissectorProvider();

   /**
    * Default constructor
    */
   public RecordingServiceCDIFactory() {
      // Empty
   }

   /**
    * Produces a new {@link DatahouseTestReportRecordingService} for each request.
    *
    * @return a {@code DatahouseTestReportRecordingService} instance configured with the
    *         datahouse URL and {@link SPIStepOutputPlanDissectorProvider}.
    */
   @Produces
   @RequestScoped
   public DatahouseTestReportRecordingService getRecordingService() {
      return new DatahouseTestReportRecordingService(
            new RecordItemClientImpl(datahouseUrl, "gzl.service.k8s.id"),
            datahouseUrl,
            dissectorProvider
      );
   }

}

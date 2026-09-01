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

package net.ihe.gazelle.maestrorecording.adapter;

import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissector;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissectorFactory;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Loads {@link StepOutputPlanDissectorFactory} implementations and returns the first one
 * supporting the provided step.
 */
public class SPIStepOutputPlanDissectorProvider implements StepOutputPlanDissectorProvider {

   private final List<StepOutputPlanDissectorFactory> factories;

   /**
    * Creates a provider backed by Java {@link ServiceLoader}.
    */
   public SPIStepOutputPlanDissectorProvider() {
      this(loadFactories());
   }

   SPIStepOutputPlanDissectorProvider(List<StepOutputPlanDissectorFactory> dissectorFactories) {
      this.factories = List.copyOf(Objects.requireNonNull(dissectorFactories, "dissector factories must not be null"));
   }

   /**
    * Returns the first SPI dissector matching the provided step run report.
    *
    * @param stepRunReport step run report to inspect
    * @return matching dissector, or {@code null}
    */
   @Override
   public StepOutputPlanDissector getDissector(StepRunReport stepRunReport) {
      return factories.stream()
            .filter(factory -> factory.supports(stepRunReport))
            .map(StepOutputPlanDissectorFactory::create)
            .findFirst()
            .orElse(null);
   }

   private static List<StepOutputPlanDissectorFactory> loadFactories() {
      ServiceLoader<StepOutputPlanDissectorFactory> loader = ServiceLoader.load(StepOutputPlanDissectorFactory.class);
      return loader.stream().map(ServiceLoader.Provider::get).toList();
   }
}

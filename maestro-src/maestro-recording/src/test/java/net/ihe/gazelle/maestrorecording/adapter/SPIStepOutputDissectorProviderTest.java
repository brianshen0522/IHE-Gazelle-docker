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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SPIStepOutputDissectorProviderTest {

   @Test
   void shouldReturnDissectorFromFirstSupportingFactory() {
      RecordingFactory supportedFactory = new RecordingFactory(true);
      RecordingFactory unsupportedFactory = new RecordingFactory(false);
      SPIStepOutputPlanDissectorProvider provider = new SPIStepOutputPlanDissectorProvider(
            List.of(unsupportedFactory, supportedFactory));

      StepRunReport step = new StepRunReport().setType("SIMULATION");
      StepOutputPlanDissector dissector = provider.getDissector(step);

      assertThat(dissector).isSameAs(supportedFactory.createdDissector);
   }

   @Test
   void shouldReturnNullWhenNoFactorySupportsStep() {
      RecordingFactory factory = new RecordingFactory(false);
      SPIStepOutputPlanDissectorProvider provider = new SPIStepOutputPlanDissectorProvider(List.of(factory));

      StepOutputPlanDissector dissector = provider.getDissector(new StepRunReport().setType("SIMULATION"));

      assertThat(dissector).isNull();
   }

   private static final class RecordingFactory implements StepOutputPlanDissectorFactory {

      private final StepOutputPlanDissector createdDissector = stepOutputPlan -> { };
      private final boolean supports;

      private RecordingFactory(boolean supports) {
         this.supports = supports;
      }

      @Override
      public boolean supports(StepRunReport stepRunReport) {
         return supports;
      }

      @Override
      public StepOutputPlanDissector create() {
         return createdDissector;
      }
   }
}

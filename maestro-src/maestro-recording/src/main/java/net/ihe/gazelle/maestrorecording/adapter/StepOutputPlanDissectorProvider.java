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

/**
 * Abstraction responsible for selecting the appropriate v2 dissector for a step.
 */
public interface StepOutputPlanDissectorProvider {

   /**
    * Returns a dissector supporting the provided step run report.
    *
    * @param stepRunReport step run report to inspect
    * @return matching dissector, or {@code null} if none supports the step
    */
   StepOutputPlanDissector getDissector(StepRunReport stepRunReport);
}

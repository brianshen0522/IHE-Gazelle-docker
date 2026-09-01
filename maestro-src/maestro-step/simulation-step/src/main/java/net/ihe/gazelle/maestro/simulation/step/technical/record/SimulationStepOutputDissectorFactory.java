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

package net.ihe.gazelle.maestro.simulation.step.technical.record;

import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissector;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissectorFactory;

/**
 * Registers the simulation step output dissector.
 */
public class SimulationStepOutputDissectorFactory implements StepOutputPlanDissectorFactory {

    /**
     * Creates the factory.
     */
    public SimulationStepOutputDissectorFactory() { /* Default constructor */ }

    @Override
    public boolean supports(StepRunReport stepRunReport) {
        return stepRunReport != null && "SIMULATION".equals(stepRunReport.getType());
    }

    @Override
    public StepOutputPlanDissector create() {
        return new SimulationStepOutputDissector();
    }
}

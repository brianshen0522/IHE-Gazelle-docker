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

package net.ihe.gazelle.maestro.simulation.step.technical.dto;

import net.ihe.gazelle.maestro.simulation.step.business.ReportSerializer;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.SerDes;
import net.ihe.gazelle.simulation.business.callback.SimulationReport;
import net.ihe.gazelle.simulation.jaxrs.api.technical.dto.callback.SimulationReportDTO;

/**
 * Implementation of ReportSerializer for serializing simulation reports.
 * Uses Jackson serialization to convert reports to JSON byte arrays.
 */
public class ReportSerializerImpl implements ReportSerializer {

   private final SerDes serDes = new JacksonSerDes();

   /**
    * Default constructor.
    */
   public ReportSerializerImpl() { /* Default constructor */ }

   @Override
   public byte[] toByteArray(SimulationReport report) {
      return serDes.serialize(new SimulationReportDTO(report));
   }

}

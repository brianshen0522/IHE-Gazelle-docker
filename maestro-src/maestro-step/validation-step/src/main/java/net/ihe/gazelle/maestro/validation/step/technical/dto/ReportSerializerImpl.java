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

package net.ihe.gazelle.maestro.validation.step.technical.dto;

import net.ihe.gazelle.maestro.validation.step.business.ReportSerializer;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.SerDes;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO;

/**
 * Provided implementation for serializing validation report in Validation Step runner
 */
public class ReportSerializerImpl implements ReportSerializer {

   private final SerDes serDes = new JacksonSerDes();

   /**
    * Default constructor.
    */
   public ReportSerializerImpl() { /* Default constructor */ }

   @Override
    public byte[] toByteArray(ValidationReport report) {
       return serDes.serialize(new ValidationReportDTO(report));
    }
}

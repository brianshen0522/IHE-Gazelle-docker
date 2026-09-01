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

package net.ihe.gazelle.maestro.validation.step.business;

import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;

/**
 * Interface for serializing validation reports.
 * Provides methods to convert validation report objects to byte arrays.
 */
public interface ReportSerializer {

    /**
     * Convert a ValidationReport object to byte array
     * @param report object to convert
     * @return same report as byte array
     * @throws IllegalStateException if report can't be converted
     */
    byte[] toByteArray(ValidationReport report);
}

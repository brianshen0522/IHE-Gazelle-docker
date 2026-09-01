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

package net.ihe.gazelle.simulation.business.search;

import net.ihe.gazelle.search.api.IndexService;

/**
 * SequenceIndexService defines the canonical set of index field names
 * that can be used to search and filter Simulation Sequences.
 * <p>
 * Implementations should expose these fields via {@link IndexService#getIndexes()}
 * so that clients can reliably build criteria against a stable set of keys.
 * </p>
 *
 * @see IndexService#getIndexes()
 */

public interface SequenceIndexService extends IndexService {

    /** Index field to filter sequences by service name. */
    String SERVICE_NAME = "serviceName";

    /** Index field to filter sequences by sequence id. */
    String ID = "id";

    /** Index field to filter sequences by transactions. */
    String TRANSACTION = "transactions";

    /** Index field to filter sequences by standards. */
    String STANDARD = "standards";

    /** Index field to filter sequences by simulated roles. */
    String SIMULATED_ROLE = "simulatedRole";

    /** Index field to filter sequences by tested roles. */
    String TESTED_ROLE = "testedRole";

    /** Index field to filter sequences by short descriptions. */
    String SHORT_DESCRIPTION = "shortDescription";

    /** Index field to filter sequences by runnability. */
    String RUNNABLE = "runnable";

    /** Index field to filter sequences by validity. */
    String VALID = "valid";
}

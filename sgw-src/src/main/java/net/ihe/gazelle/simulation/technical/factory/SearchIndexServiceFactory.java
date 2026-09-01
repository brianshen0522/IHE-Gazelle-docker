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

package net.ihe.gazelle.simulation.technical.factory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import net.ihe.gazelle.simulation.business.search.SequenceIndexService;
import net.ihe.gazelle.simulation.business.search.SequenceIndexServicesImpl;

/**
 * Factory class that produces the {@link SequenceIndexService} implementation for use within the application context.
 * <p>
 * The {@link SequenceIndexService} provides a canonical set of index fields for searching and filtering simulation sequences.
 * These fields are exposed through the indexed fields returned by the {@link SequenceIndexServicesImpl}.
 */
@ApplicationScoped
public class SearchIndexServiceFactory {

   /**
    * Creates a new instance of {@code SearchIndexServiceFactory}.
    */
   public SearchIndexServiceFactory() {
      // Empty
   }

   /**
    * Creates a new instance of the {@link SequenceIndexService} implementation.
    * @return a new instance of the {@link SequenceIndexService} implementation
    */
    @Produces
    public SequenceIndexService createSequenceIndexService() {
        return new SequenceIndexServicesImpl();
    }
}

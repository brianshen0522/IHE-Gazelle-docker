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

package net.ihe.gazelle.simulation.technical.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.enterprise.context.ApplicationScoped;
import net.ihe.gazelle.simulation.business.SimulationSequenceDAO;
import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;
import net.ihe.gazelle.simulation.client.business.SimulationServiceClient;
import net.ihe.gazelle.simulation.client.technical.SimulationServiceClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link SimulationSequenceDAO} interface.
 */
@ApplicationScoped
public class SimulationSequenceDAOImpl implements SimulationSequenceDAO {

   private static final Logger LOG = LoggerFactory.getLogger(SimulationSequenceDAOImpl.class);

   private static final int CHECKSUM_TIMEOUT_MINUTES = 1;

   private final LoadingCache<String, String> checksumCache;

   /**
    * Constructor.
    */
   public SimulationSequenceDAOImpl() {
      this(Duration.ofMinutes(CHECKSUM_TIMEOUT_MINUTES));
   }

   /**
    * Constructs an instance of SimulationSequenceDAOImpl with the specified checksum timeout duration.
    *
    * @param checksumTimeout the duration for which checksums are cached before expiry; must be non-null and positive
    */
   public SimulationSequenceDAOImpl(Duration checksumTimeout) {
      checksumCache = Caffeine.newBuilder()
            .expireAfterWrite(checksumTimeout)
            .maximumSize(100)
            .build(this::loadChecksum);
   }

   @Override
   public String getServiceChecksum(String serviceUrl) {
      return checksumCache.get(serviceUrl);
   }

   @Override
   public List<SimulationSequence> getSimulationSequences(String serviceUrl) {
      try {
         SimulationServiceClient client = createClient(serviceUrl);
         return client.getSimulationSequences();
      } catch (Exception e) {
         LOG.debug(e.getMessage());
         return new ArrayList<>();
      }
   }

   private String loadChecksum(String serviceUrl) {
      SimulationServiceClient client = createClient(serviceUrl);
      return client.getChecksum(new ArrayList<>());
   }

   /**
    * Creates and returns a new instance of {@link SimulationServiceClient}.
    *
    * @param serviceUrl the base URL of the simulation service; must be non-null and not blank
    * @return a {@link SimulationServiceClient} initialized with the provided service URL
    * @throws IllegalArgumentException if {@code serviceUrl} is null, blank, or empty
    */
   protected SimulationServiceClient createClient(String serviceUrl) {
      return new SimulationServiceClientImpl(serviceUrl, null);
   }
}

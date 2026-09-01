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

package net.ihe.gazelle.simulation.technical.ws;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.search.api.IndexedField;
import net.ihe.gazelle.search.api.SuggestionService;
import net.ihe.gazelle.search.jaxrs.api.QueryMapper;
import net.ihe.gazelle.simulation.business.*;
import net.ihe.gazelle.simulation.business.search.SequenceIndexService;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;
import net.ihe.gazelle.simulation.business.search.SequenceSearchServiceImpl;
import net.ihe.gazelle.simulation.business.search.SimulationSequenceReadService;
import net.ihe.gazelle.simulation.business.svs.SimulationSVSServiceImpl;
import net.ihe.gazelle.simulation.mock.IdentityMock;
import net.ihe.gazelle.simulation.technical.config.ApplicationConfigMock;
import net.ihe.gazelle.simulation.technical.dao.AutoRefreshSequenceLookupDAO;
import net.ihe.gazelle.simulation.technical.dao.ServiceRegistryDAOMock;
import net.ihe.gazelle.simulation.technical.dao.SimulationSequenceDAOMock;
import net.ihe.gazelle.simulation.technical.dao.SimulationSequenceLookupDAOImpl;
import net.ihe.gazelle.simulation.technical.dto.ResolvedSimulationSequenceDTO;
import net.ihe.gazelle.simulation.technical.factory.SearchIndexServiceFactory;
import net.ihe.gazelle.simulation.technical.factory.SearchParameterServiceFactory;
import net.ihe.gazelle.svs.client.business.SVSServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static net.ihe.gazelle.simulation.business.search.SequenceIndexService.ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchSequenceControllerTest {

   private static SearchSequenceController controller;

   @BeforeAll
   static void setUp() {
      SimulationSequenceLookupDAO lookupDAO = createSimulationSequenceLookupDAO();
      SearchIndexServiceFactory searchIndexServiceFactory = new SearchIndexServiceFactory();
      SequenceIndexService indexService = searchIndexServiceFactory.createSequenceIndexService();
      SearchParameterServiceFactory factory = new SearchParameterServiceFactory(indexService, lookupDAO);
      SuggestionService<SequenceSearchCriteria> suggestionService = factory.createSuggestionService();
      QueryMapper<SequenceQueryBeanParam, SequenceSearchCriteria> queryMapper = factory.createQueryMapper();
      controller = new SearchSequenceController(
            new IdentityMock(Set.of("role:gazelle_admin")),
            indexService,
            suggestionService,
            queryMapper,
            new SequenceSearchServiceImpl(lookupDAO),
            new SimulationSequenceReadService(lookupDAO, new SimulationSVSServiceImpl(new SVSServiceImpl(new SVSHttpClientMock()))));
   }

   private static SimulationSequenceLookupDAO createSimulationSequenceLookupDAO() {
      ApplicationConfig applicationConfig = new ApplicationConfigMock();
      ServiceRegistryDAO serviceRegistryDAO = new ServiceRegistryDAOMock(true);
      SimulationSequenceDAO simulationSequenceDAOMock = new SimulationSequenceDAOMock();
      return new AutoRefreshSequenceLookupDAO(
            applicationConfig,
            new SequenceChecksumCacheImpl(serviceRegistryDAO, simulationSequenceDAOMock),
            new SimulationSequenceLookupDAOImpl(
                  new SimulationSequenceLoader(
                        serviceRegistryDAO,
                        simulationSequenceDAOMock
                  )
            )
      );
   }

   @Test
   void should_get_indexes() {
      Response response = controller.getIndexes();
      List<IndexedField> indexes = response.readEntity(new GenericType<>() {
      });

      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      assertEquals(9, indexes.size());
   }

   @Test
   void should_get_possible_values_for_field_test() {
      Response response = controller.getPossibleValues(ID, new SequenceQueryBeanParam());
      List<String> possibleValues = response.readEntity(new GenericType<>() {
      });

      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      assertEquals(4, possibleValues.size());
      assertTrue(possibleValues.contains("CHXDS_ITI-41-42"));
   }

   @Test
   void should_not_found_for_unknown_field() {
      Response response = controller.getPossibleValues(null, new SequenceQueryBeanParam());

      assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

      response = controller.getPossibleValues("unknown", new SequenceQueryBeanParam());

      assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
   }

   @Test
   void search_should_return_all_sequences() {
      try (Response response = controller.search(new SequenceQueryBeanParam())) {
         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(4, sequences.size());
      }
   }

   @Test
   void search_should_return_filter_by_service_name() {
      try (Response response = controller.search(new SequenceQueryBeanParam().setServiceName("XDS"))) {

         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(2, sequences.size());
      }
   }

   @Test
   void search_should_return_filter_by_id() {
      try (Response response = controller.search(new SequenceQueryBeanParam().setId("CHXDS_ITI-41-42"))) {

         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(1, sequences.size());
      }
   }

   @Test
   void search_should_return_filter_by_simulated_role() {
      try (Response response = controller.search(new SequenceQueryBeanParam().setSimulatedRole("Document Repository"))) {

         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(1, sequences.size());
      }
   }

   @Test
   void search_should_return_filter_by_tested_role() {
      try (Response response = controller.search(new SequenceQueryBeanParam().setTestedRole("Document Registry"))) {

         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(1, sequences.size());
      }
   }

   @Test
   void search_should_return_filter_by_transaction() {
      try (Response response = controller.search(new SequenceQueryBeanParam().setTransaction("ITI-65"))) {

         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(1, sequences.size());
      }
   }

   @Test
   void search_should_return_filter_by_standard() {
      try (Response response = controller.search(new SequenceQueryBeanParam().setStandard("HTTP"))) {

         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(2, sequences.size());
      }
   }

   @Test
   void search_should_return_filter_by_short_description() {
      try (Response response = controller.search(new SequenceQueryBeanParam().setShortDescription("XDS"))) {

         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(2, sequences.size());
      }
   }

   @Test
   void search_should_return_filter_by_runnability() {
      try (Response response = controller.search(new SequenceQueryBeanParam().setRunnable("true"))) {

         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(4, sequences.size());
      }
   }

   @Test
   void search_should_return_filter_by_validity() {
      try (Response response = controller.search(new SequenceQueryBeanParam().setValid("true"))) {

         List<ResolvedSimulationSequenceDTO> sequences = response.readEntity(new GenericType<>() {
         });

         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         assertEquals(4, sequences.size());
      }
   }

   @Test
   void should_get_sequence_by_id() {
      Response response = controller.getSimulationSequenceById("CHXDS_ITI-41-42");

      ResolvedSimulationSequenceDTO sequence = response.readEntity(ResolvedSimulationSequenceDTO.class);

      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      assertEquals("CHXDS_ITI-41-42", sequence.getId());
   }

   @Test
   void should_bad_request_when_null_id() {
      Response response = controller.getSimulationSequenceById(null);

      assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
   }

   @Test
   void should_bad_request_when_no_existing_id() {
      Response response = controller.getSimulationSequenceById("unknown");

      assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
   }
}

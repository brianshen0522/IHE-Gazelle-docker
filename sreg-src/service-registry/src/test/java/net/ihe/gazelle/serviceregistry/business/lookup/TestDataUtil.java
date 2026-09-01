/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.business.lookup;

import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.mocks.MockedGazelleIdentity;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;

import java.util.Set;

import static net.ihe.gazelle.security.business.Groups.ROLE_TEST_SERVICE;

public class TestDataUtil {

   private final GazelleIdentity identity = new MockedGazelleIdentity(Set.of(ROLE_TEST_SERVICE));
   private final ServiceRegistration registration;

   public TestDataUtil(ServiceRegistration registration) {
      this.registration = registration;
   }

   public static Service getXmlValidator() {
      return new Service().setName("XML Validator").setVersion("1.0.0").setInstanceId("11111").setReplicaId("1");
   }

   public static Service getMaestro1() {
      return new Service().setName("Maestro").setVersion("1.1.0").setInstanceId("22222").setReplicaId("1");
   }

   public static Service getMaestro2() {
      return getMaestro1().setReplicaId("2");
   }

   public static Service getMCSDSimulator() {
      return new Service().setName("mCSD Simulator").setVersion("2.0.0").setInstanceId("33333").setReplicaId("1");
   }

   public static Service getTestManagement() {
      return new Service().setName("Test Management").setVersion("10.1.0").setInstanceId("44444").setReplicaId("1");
   }

   public static Service getEVS() {
      return new Service().setName("EVS Client").setVersion("7.2.5").setInstanceId("55555").setReplicaId("1");
   }

   public static Service getHL7v2Validator() {
      return new Service().setName("HL7v2 Validator").setVersion("1.0.0").setInstanceId("66666").setReplicaId("1");
   }

   public static Service getMHDSimulator() {
      return new Service().setName("MHD Simulator").setVersion("1.0.0").setInstanceId("77777").setReplicaId("1");
   }

   public void registerAllServices() {
      registerService(getTestManagement());
      registerService(getEVS());
      connectService(getMaestro2());
      connectService(getMaestro1());
      connectService(getMCSDSimulator());
      connectService(getMHDSimulator());
      connectService(getHL7v2Validator());
      connectService(getXmlValidator());
   }

   public void registerService(Service service) {
      registration.register(service);
   }

   public void connectService(Service service) {
      registration.connectService(service, identity);
   }
}

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
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.api.business.TestDataUtil;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;

import java.util.Set;

import static net.ihe.gazelle.security.business.Groups.ROLE_TEST_SERVICE;

public class RegistrationUtil {

   private final ServiceRegistration registration;
   private final GazelleIdentity identity = new MockedGazelleIdentity(Set.of(ROLE_TEST_SERVICE));

   public RegistrationUtil(ServiceRegistration registration) {
      this.registration = registration;
   }

   public void registerAllServices() {
      registerService(TestDataUtil.getTestManagement());
      registerService(TestDataUtil.getEVS());
      connectService(TestDataUtil.getMaestro2());
      connectService(TestDataUtil.getMaestro1());
      connectService(TestDataUtil.getMCSDSimulator());
      connectService(TestDataUtil.getMHDSimulator());
      connectService(TestDataUtil.getHL7v2Validator());
      connectService(TestDataUtil.getXmlValidator());
      connectService(TestDataUtil.getIUASimulator());
   }

   public void registerService(Service service) {
      registration.register(service);
   }

   public void connectService(Service service) {
      registration.connectService(service, identity);
   }

   public void disconnectService(ServiceId serviceId) {
      registration.disconnectService(serviceId);
   }

}

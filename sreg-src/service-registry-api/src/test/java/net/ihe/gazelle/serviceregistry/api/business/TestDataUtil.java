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

package net.ihe.gazelle.serviceregistry.api.business;

import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.Service;

import java.util.List;

public class TestDataUtil {

   public static Service getXmlValidator() {
      return new Service()
            .setName("XML Validator")
            .setVersion("1.0.0")
            .setInstanceId("11111")
            .setReplicaId("1")
            .setProvidedInterfaces(List.of(
                  new ProvidedInterface()
                        .setInterfaceName("Gazelle Validation API")
                        .setInterfaceVersion("2.0.0")
                        .setBindings(List.of(
                              new HttpRestBinding().setServiceUrl("http://localhost:8180/xml-validator/rest")
                        ))
            ));
   }

   public static Service getMaestro1() {
      return new Service().setName("Maestro").setVersion("1.1.0").setInstanceId("22222").setReplicaId("1");
   }

   public static Service getMaestro2() {
      return getMaestro1().setReplicaId("2");
   }

   public static Service getMCSDSimulator() {
      return new Service()
            .setName("mCSD Simulator")
            .setVersion("2.0.0")
            .setInstanceId("33333")
            .setReplicaId("1")
            .setProvidedInterfaces(List.of(
                  new ProvidedInterface()
                        .setInterfaceName("Gazelle Simulation API")
                        .setInterfaceVersion("1.0.0")
                        .setBindings(List.of(
                              new HttpRestBinding().setServiceUrl("http://localhost:8380/mcsd-simulator/rest")
                        ))
            ))
            .setConsumedInterfaces(List.of(
                  new ConsumedInterface()
                          .setInterfaceName("Service Registration API")
                          .setSupportedVersions(List.of("1.0","2.0"))
                          .setSupportedBindings(List.of("WEB_SOCKET"))
          ));
   }

   public static Service getTestManagement() {
      return new Service().setName("Test Management").setVersion("10.1.0").setInstanceId("44444").setReplicaId("1");
   }

   public static Service getEVS() {
      return new Service().setName("EVS Client").setVersion("7.2.5").setInstanceId("55555").setReplicaId("1");
   }

   public static Service getHL7v2Validator() {
      return new Service()
            .setName("HL7v2 Validator")
            .setVersion("1.0.0")
            .setInstanceId("66666")
            .setReplicaId("1")
            .setProvidedInterfaces(List.of(
                  new ProvidedInterface()
                        .setInterfaceName("Gazelle Validation API")
                        .setInterfaceVersion("2.0.0")
                        .setBindings(List.of(
                              new HttpRestBinding().setServiceUrl("http://localhost:8680/hl7v2-validator/rest")
                        ))
            ))
            .setConsumedInterfaces(List.of(
                  new ConsumedInterface()
                          .setInterfaceName("Service Registration API")
                          .setSupportedVersions(List.of("1.0","2.0"))
                          .setSupportedBindings(List.of("WEB_SOCKET"))
            ));
   }

   public static Service getMHDSimulator() {
      return new Service()
            .setName("MHD Simulator")
            .setVersion("1.0.0")
            .setInstanceId("77777")
            .setReplicaId("1")
            .setProvidedInterfaces(List.of(
                  new ProvidedInterface()
                        .setInterfaceName("Gazelle Simulation API")
                        .setInterfaceVersion("1.0.0")
                        .setBindings(List.of(
                              new HttpRestBinding().setServiceUrl("http://localhost:8780/mhd-simulator/rest")
                        ))
            ));
   }

   public static Service getIUASimulator() {
      return new Service()
            .setName("IUA Simulator")
            .setVersion("1.2.0")
            .setInstanceId("88888")
            .setReplicaId("1")
            .setProvidedInterfaces(List.of(
                  new ProvidedInterface()
                        .setInterfaceName("Gazelle Simulation API")
                        .setInterfaceVersion("1.0.0")
                        .setBindings(List.of(
                              new HttpRestBinding().setServiceUrl("http://localhost:8780/mhd-simulator/rest")
                        ))
            ));
   }

   public static List<Service> getAllServices() {
      return List.of(
            getTestManagement(),
            getEVS(),
            getMaestro2(),
            getMaestro1(),
            getMCSDSimulator(),
            getMHDSimulator(),
            getHL7v2Validator(),
            getXmlValidator(),
            getIUASimulator()
      );
   }

}

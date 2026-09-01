/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.maestro.quarkus.configuration;

import net.ihe.gazelle.maestro.quarkus.websocket.TestStreamSocketController;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.WebSocketBindingBuilder;
import net.ihe.gazelle.servicemetadata.api.technical.ProvidedInterfaceIdentifier;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Collections;

/**
 * Provided interface for Test Stream WebSocket API.
 */
public class TestRunStreamProvidedInterfaceIdentifier implements ProvidedInterfaceIdentifier {

   private final String maestroUrl;

   /**
    * Default constructor.
    */
   public TestRunStreamProvidedInterfaceIdentifier() {
      this(
            ConfigProvider.getConfig()
                  .getOptionalValue("gzl.maestro.url", String.class)
                  .orElseThrow(() -> new IllegalStateException(
                        "Service registry URL not configured. Please set the 'gzl.maestro.url' property."
                  ))
      );
   }

   private TestRunStreamProvidedInterfaceIdentifier(String maestroUrl) {
      this.maestroUrl = maestroUrl;
   }

   @Override
   public ProvidedInterface getProvidedInterface() {

      return new ProvidedInterfaceBuilder()
            .setInterfaceName(TestStreamSocketController.INTERFACE_NAME)
            .setInterfaceVersion(TestStreamSocketController.INTERFACE_VERSION)
            .setBindings(Collections.singleton(
                  new WebSocketBindingBuilder()
                        .setWebSocketUrl(maestroUrl.replace("http", "ws")))
            )
            .build();

   }
}

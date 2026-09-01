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

package net.ihe.gazelle.serviceregistry.technical.healthcheck;

import net.ihe.gazelle.servicemetadata.api.business.HttpRestBindingBuilder;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.technical.ProvidedInterfaceIdentifier;
import org.eclipse.microprofile.config.ConfigProvider;

import static net.ihe.gazelle.serviceregistry.technical.config.ServiceRegistryMicroprofileConfiguration.GZL_SERVICE_REGISTRY_URL;

/**
 * Identifies health check provided interface.
 */
public class HealthcheckInterfaceIdentifier implements ProvidedInterfaceIdentifier {

    private final String healthcheckUrl;

    /**
     * Default constructor. Requires Microprofile Config to retrieve <code>gzl.test.model.repository.url</code> and
     * <code>gzl.healthcheck.path</code> configurations.
     */
    public HealthcheckInterfaceIdentifier() {
        this(
              ConfigProvider.getConfig()
                    .getValue(GZL_SERVICE_REGISTRY_URL, String.class),
              ConfigProvider.getConfig()
                    .getValue("gzl.healthcheck.path", String.class)
        );
    }

    /**
     * Constructor with parameters.
     *
     * @param testModelRepositoryUrl the base URL of the Test Model Repository service
     * @param healthCheckPath        the path to the health check endpoint (e.g. "/health")
     */
    public HealthcheckInterfaceIdentifier(String testModelRepositoryUrl, String healthCheckPath) {
        healthcheckUrl = testModelRepositoryUrl + healthCheckPath;
    }

    @Override
    public ProvidedInterface getProvidedInterface() {
        return new ProvidedInterfaceBuilder()
              .setInterfaceName("Microprofile Health")
              .setInterfaceVersion("4.0")
              .addBinding(
                    new HttpRestBindingBuilder()
                          .setServiceUrl(healthcheckUrl)
              )
              .build();
    }

}

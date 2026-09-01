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

package net.ihe.gazelle.simulation.business.svs;

import net.ihe.gazelle.simulation.business.model.Option;
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.business.model.ResolvedSupportedParameter;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.svs.client.business.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implementation of the {@link SimulationSVSService} interface.
 */
public class SimulationSVSServiceImpl implements SimulationSVSService {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationSVSServiceImpl.class);

    private final SVSService svsService;

   /**
    * Constructor to initialize {@code SVSServiceImpl} with the specified {@code SVSClient}.
    *
    * @param svsService An instance of {@code SVSService} used to interact with the SVS application for retrieving value sets.
    */
    public SimulationSVSServiceImpl(SVSService svsService) {
        this.svsService = svsService;
    }

    @Override
    public ResolvedSimulationSequence resolveValueSets(SimulationSequenceExtended sequence) {
        ResolvedSimulationSequence resolvedSequence = new ResolvedSimulationSequence(sequence);
        for (ResolvedSupportedParameter parameter : resolvedSequence.getSupportedParameters()) {
            if (parameter.getValueSetId() != null) {
                resolveValueSetForParameter(parameter);
            }
        }
        return resolvedSequence;
    }

    private void resolveValueSetForParameter(ResolvedSupportedParameter parameter) {
        try {
            List<SVSResource> svsResources = svsService.resolveValueSets(parameter.getValueSetId());
            parameter.setOptions(svsResources.stream()
                    .map(svsResource -> new Option(svsResource.code(), svsResource.displayName()))
                    .toList()
            );
        } catch (SVSNotReachableException | UnknownValueSetException e) {
            parameter.setError(e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            parameter.setError("Unexpected error while retrieving value set " + parameter.getValueSetId());
        }
    }
}

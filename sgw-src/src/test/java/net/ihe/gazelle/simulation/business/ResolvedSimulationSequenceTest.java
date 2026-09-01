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

package net.ihe.gazelle.simulation.business;

import net.ihe.gazelle.simulation.business.model.Option;
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.business.model.ResolvedSupportedParameter;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.simulation.business.sequence.*;
import net.ihe.gazelle.simulation.business.setup.ParameterType;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResolvedSimulationSequenceTest {

    @Test
    void should_set_value_resolved_simulation_sequence() {
        SimulationSequenceExtended sequence = new SimulationSequenceExtended(new SimulationSequenceBuilder()
                .setId("id")
                .setVersion("1.0")
                .setDescription("description")
                .setShortDescription("shortDescription")
                .setRunnable(true)
                .addStandard("SOAP")
                .addTransactionKeyword("ITI-42")
                .addSimulatedRole(new SimulatedRoleBuilder()
                        .setName("Doc responder")
                        .setType(RoleType.RESPONDER))
                .addTestRole(new TestedRoleBuilder()
                        .setName("Doc responder")
                        .setType(RoleType.RESPONDER))
                .addSupportedParameter(new SupportedParameterBuilder()
                        .setName("param1")
                        .setGroupName("group")
                        .setType(ParameterType.TEXT)
                        .setDescription("description")
                        .setOptions(List.of("option1", "option2")))
                .build())
                .setSimulatorName("simulator")
                .setSimulatorUrl("url")
                .setSimulatorVersion("1.0")
                .setValid(false)
                .setValidReportMessage("invalid");
        ResolvedSimulationSequence resolved = new ResolvedSimulationSequence(sequence);

        assertEquals(sequence.getId(), resolved.getId());
        assertEquals(sequence.getVersion(), resolved.getVersion());
        assertEquals(sequence.getDescription(), resolved.getDescription());
        assertEquals(sequence.getShortDescription(), resolved.getShortDescription());
        assertEquals(sequence.isRunnable(), resolved.isRunnable());
        assertEquals(sequence.getStandards(), resolved.getStandards());
        assertEquals(sequence.getTransactions(), resolved.getTransactions());
        assertEquals(sequence.getSimulatedRoles(), resolved.getSimulatedRoles());
        assertEquals(sequence.getTestedRoles(), resolved.getTestedRoles());
        assertEquals(sequence.getSimulatorName(), resolved.getSimulatorName());
        assertEquals(sequence.getSimulatorVersion(), resolved.getSimulatorVersion());
        assertEquals(sequence.getSimulatorUrl(), resolved.getSimulatorUrl());
        assertEquals(sequence.isValid(), resolved.isValid());
        assertEquals(sequence.getValidReportMessage(), resolved.getValidReportMessage());
        assertEquals(sequence.getSupportedParameters().size(), resolved.getSupportedParameters().size());
        SupportedParameter supportedParameter = sequence.getSupportedParameters().getFirst();
        ResolvedSupportedParameter resolvedParameter = resolved.getSupportedParameters().getFirst();
        assertEquals(supportedParameter.getName(), resolvedParameter.getName());
        assertEquals(supportedParameter.getGroupName(), resolvedParameter.getGroupName());
        assertEquals(supportedParameter.getType(), resolvedParameter.getType());
        assertEquals(supportedParameter.getDescription(), resolvedParameter.getDescription());

        List<String> options = resolvedParameter.getOptions()
                .stream()
                .map(Option::getValue)
                .toList();
        assertEquals(supportedParameter.getOptions(), options);
    }

    @Test
    void should_set_value_resolved_supported_parameter() {
        ResolvedSupportedParameter resolvedParameter = new ResolvedSupportedParameter()
                .setName("param1")
                .setValueSetId("value set")
                .setOptions(List.of(new Option("value", "label")))
                .setError("error");
        ResolvedSupportedParameter copy = new ResolvedSupportedParameter(resolvedParameter);
        assertEquals(resolvedParameter, copy);
    }

    @Test
    void verify_equals_for_resolved_sequence() {
        EqualsVerifier.simple().forClass(ResolvedSimulationSequence.class).verify();
        EqualsVerifier.simple().forClass(ResolvedSupportedParameter.class).verify();
        EqualsVerifier.simple().forClass(Option.class).verify();
    }
}

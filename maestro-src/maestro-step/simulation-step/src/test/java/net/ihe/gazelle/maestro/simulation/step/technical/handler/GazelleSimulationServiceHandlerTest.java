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

package net.ihe.gazelle.maestro.simulation.step.technical.handler;

import net.ihe.gazelle.simulation.business.callback.SimulationReport;
import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;
import net.ihe.gazelle.simulation.business.setup.AdditionalInstructions;
import net.ihe.gazelle.simulation.business.setup.SimulationRequest;
import net.ihe.gazelle.simulation.client.business.SimulationClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GazelleSimulationServiceHandlerTest {

    @Mock
    SimulationClient simulationClient;

    @Test
    void isAvailable_returnsTrue_whenClientProvidesSequences() {
        when(simulationClient.getChecksum(List.of())).thenReturn("0x12345678");
        GazelleSimulationServiceHandler handler = new GazelleSimulationServiceHandler(simulationClient);

        assertTrue(handler.isAvailable());
        verify(simulationClient, times(1)).getChecksum(List.of());
    }

    @Test
    void isAvailable_returnsFalse_whenClientThrows() {
        when(simulationClient.getChecksum(List.of())).thenThrow(new RuntimeException("boom"));
        GazelleSimulationServiceHandler handler = new GazelleSimulationServiceHandler(simulationClient);

        assertFalse(handler.isAvailable());
        verify(simulationClient, times(1)).getChecksum(List.of());
    }

    @Test
    void getSimulationSequences_delegatesToClient() {
        List<SimulationSequence> sequences = List.of(mock(SimulationSequence.class), mock(SimulationSequence.class));
        when(simulationClient.getSimulationSequences()).thenReturn(sequences);
        GazelleSimulationServiceHandler handler = new GazelleSimulationServiceHandler(simulationClient);

        List<SimulationSequence> result = handler.getSimulationSequences();
        assertSame(sequences, result);
        verify(simulationClient, times(1)).getSimulationSequences();
    }

    @Test
    void simulate_delegatesToClient_andPassesConsumers() {
        GazelleSimulationServiceHandler handler = new GazelleSimulationServiceHandler(simulationClient);
        SimulationRequest request = mock(SimulationRequest.class);

        AtomicBoolean instructionCalled = new AtomicBoolean(false);
        AtomicBoolean reportCalled = new AtomicBoolean(false);

        Consumer<AdditionalInstructions> instructionConsumer = additionalInstructions -> instructionCalled.set(true);
        Consumer<SimulationReport> reportConsumer = report -> reportCalled.set(true);

        // Capture consumers passed to client
        ArgumentCaptor<Consumer<AdditionalInstructions>> instrCaptor = ArgumentCaptor.forClass(Consumer.class);
        ArgumentCaptor<Consumer<SimulationReport>> reportCaptor = ArgumentCaptor.forClass(Consumer.class);

        doAnswer(invocation -> {
            Consumer<AdditionalInstructions> ic = invocation.getArgument(1);
            Consumer<SimulationReport> rc = invocation.getArgument(2);
            // Simulate client calling back the consumers
            ic.accept(mock(AdditionalInstructions.class));
            rc.accept(mock(SimulationReport.class));
            return null;
        }).when(simulationClient).simulate(eq(request), instrCaptor.capture(), reportCaptor.capture());

        handler.simulate(request, instructionConsumer, reportConsumer);

        // Verify delegation and that the same consumers were passed through
        verify(simulationClient, times(1)).simulate(eq(request), any(), any());
        assertTrue(instructionCalled.get());
        assertTrue(reportCalled.get());
        assertSame(instructionConsumer, instrCaptor.getValue());
        assertSame(reportConsumer, reportCaptor.getValue());
    }

    @Test
    void testGetChecksumThrowsUnsupportedOperationException() {
        GazelleSimulationServiceHandler handler = new GazelleSimulationServiceHandler(simulationClient);
        assertThrows(UnsupportedOperationException.class, () -> handler.getChecksum(null));
    }
}


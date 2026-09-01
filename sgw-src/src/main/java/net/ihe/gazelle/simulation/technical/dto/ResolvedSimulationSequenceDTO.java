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

package net.ihe.gazelle.simulation.technical.dto;

import com.fasterxml.jackson.annotation.*;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.jaxrs.api.technical.dto.sequence.SimulatedRoleDTO;
import net.ihe.gazelle.simulation.jaxrs.api.technical.dto.sequence.TestedRoleDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "SimulationSequence",
        description = "Represents an extension of a Simulation Sequence with simulation service metadata and value set parameters options."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "id",
        "version",
        "simulatorName",
        "simulatorVersion",
        "simulatorUrl",
        "valid",
        "validReportMessage",
        "transactions",
        "shortDescription",
        "description",
        "runnable",
        "standards",
        "simulatedRoles",
        "testedRoles",
        "supportedParameters"
})
public class ResolvedSimulationSequenceDTO implements DTO<ResolvedSimulationSequence> {

    @JsonIgnore
    private final ResolvedSimulationSequence resolvedSimulationSequence;

    public ResolvedSimulationSequenceDTO() {
        this(new ResolvedSimulationSequence());
    }

    public ResolvedSimulationSequenceDTO(ResolvedSimulationSequence resolvedSimulationSequence) {
        this.resolvedSimulationSequence = resolvedSimulationSequence;
    }

    @Override
    @JsonIgnore
    public ResolvedSimulationSequence getBusinessObject() {
        return resolvedSimulationSequence;
    }

    @Schema(
            name = "id",
            description = "The unique identifier of the sequence.",
            examples = "XDS Doc Repo - Provide and Register",
            required = true
    )
    @JsonGetter("id")
    public String getId() {
        return resolvedSimulationSequence.getId();
    }

    @JsonSetter("id")
    public void setId(String id) {
        resolvedSimulationSequence.setId(id);
    }

    @Schema(
            name = "version",
            description = "The version of the sequence.",
            examples = "1.0"
    )
    @JsonGetter("version")
    public String getVersion() {
        return resolvedSimulationSequence.getVersion();
    }

    @JsonSetter("version")
    public void setVersion(String version) {
        resolvedSimulationSequence.setVersion(version);
    }

    @Schema(
            name = "simulatorName",
            description = "The name of the simulation service."
    )
    @JsonGetter("simulatorName")
    public String getSimulatorName() {
        return getBusinessObject().getSimulatorName();
    }

    @JsonSetter("simulatorName")
    public void setSimulatorName(String simulatorName) {
        getBusinessObject().setSimulatorName(simulatorName);
    }

    @Schema(
            name = "simulatorVersion",
            description = "The version of the simulation service."
    )
    @JsonGetter("simulatorVersion")
    public String getSimulatorVersion() {
        return getBusinessObject().getSimulatorVersion();
    }

    @JsonSetter("simulatorVersion")
    public void setSimulatorVersion(String simulatorVersion) {
        getBusinessObject().setSimulatorVersion(simulatorVersion);
    }

    @Schema(
            name = "simulatorUrl",
            description = "The base URL of the simulation service."
    )
    @JsonGetter("simulatorUrl")
    public String getSimulatorUrl() {
        return getBusinessObject().getSimulatorUrl();
    }

    @JsonSetter("simulatorUrl")
    public void setSimulatorUrl(String simulatorUrl) {
        getBusinessObject().setSimulatorUrl(simulatorUrl);
    }

    @Schema(
            name = "valid",
            description = "The validity state of the simulation sequence from a business point of view."
    )
    @JsonGetter("valid")
    public boolean getValid() {
        return getBusinessObject().isValid();
    }

    @JsonSetter("valid")
    public void setValid(boolean valid) {
        getBusinessObject().setValid(valid);
    }

    @Schema(
            name = "validReportMessage",
            description = "A list of all invalid rules if any."
    )
    @JsonGetter("validReportMessage")
    public String getValidReportMessage() {
        return getBusinessObject().getValidReportMessage();
    }

    @JsonSetter("validReportMessage")
    public void setValidReportMessage(String validReportMessage) {
        getBusinessObject().setValidReportMessage(validReportMessage);
    }

    @Schema(
            name = "transactions",
            description = "The keyword list of the transactions in the given sequence.",
            examples = {"ITI-41", "ITI-42"}
    )
    @JsonGetter("transactions")
    public List<String> getTransactions() {
        return getBusinessObject().getTransactions();
    }

    @JsonSetter("transactions")
    public void setTransactions(List<String> transactions) {
        getBusinessObject().setTransactions(transactions);
    }

    @Schema(
            name = "shortDescription",
            description = "A short description of what this sequence does.",
            examples = "A short description of what this sequence does."
    )
    @JsonGetter("shortDescription")
    public String getShortDescription() {
        return getBusinessObject().getShortDescription();
    }

    @JsonSetter("shortDescription")
    public void setShortDescription(String shortDescription) {
        getBusinessObject().setShortDescription(shortDescription);
    }

    @Schema(
            name = "description",
            description = "A more detailed description of what this sequence does. Transactions involved, standards... Can even be html code.",
            examples = "<p>A more detailed description of what this sequence does. Transactions involved, standards... Can even be html code.</p>"
    )
    @JsonGetter("description")
    public String getDescription() {
        return getBusinessObject().getDescription();
    }

    @JsonSetter("description")
    public void setDescription(String description) {
        getBusinessObject().setDescription(description);
    }

    @Schema(
            name= "runnable",
            description = "Defining if a simulation sequence can be executed automatically or not.",
            defaultValue = "true"
    )
    @JsonGetter("runnable")
    public boolean getRunnable() {
        return getBusinessObject().isRunnable();
    }

    @JsonSetter("runnable")
    public void setRunnable(boolean runnable) {
        getBusinessObject().setRunnable(runnable);
    }

    @Schema(
            name = "standards",
            description = "The list of standards used by the transactions in that sequence.",
            examples = {"SOAP", "HTTP/1.1", "XML 1.0", "ebRIM"}
    )
    @JsonGetter("standards")
    public List<String> getStandards() {
        return getBusinessObject().getStandards();
    }

    @JsonSetter("standards")
    public void setStandards(List<String> standards) {
        getBusinessObject().setStandards(standards);
    }

    @Schema(
            name = "simulatedRoles",
            description = "The of roles supported by the Simulator."
    )
    @JsonGetter("simulatedRoles")
    public List<SimulatedRoleDTO> getSimulatedRoles() {
        return getBusinessObject().getSimulatedRoles().stream().map(SimulatedRoleDTO::new).toList();
    }

    @JsonSetter("simulatedRoles")
    public void setSimulatedRoles(List<SimulatedRoleDTO> dto) {
        getBusinessObject().setSimulatedRoles(
                dto.stream().map(SimulatedRoleDTO::getBusinessObject).toList()
        );
    }

    @Schema(
            name = "testedRoles",
            description = "List of the roles supported by the system under test."
    )
    @JsonGetter("testedRoles")
    public List<TestedRoleDTO> getTestedRoles() {
        return getBusinessObject().getTestedRoles().stream().map(TestedRoleDTO::new).toList();
    }

    @JsonSetter("testedRoles")
    public void setTestedRoles(List<TestedRoleDTO> dto) {
        getBusinessObject().setTestedRoles(
                dto.stream().map(TestedRoleDTO::getBusinessObject).toList()
        );
    }

    @Schema(
            name = "supportedParameters",
            description = "The list of configuration parameters supported by the simulator in that for the given Simulation Sequence."
    )
    @JsonGetter("supportedParameters")
    public List<ResolvedParameterDTO> getSupportedParameters() {
        return getBusinessObject().getSupportedParameters().stream().map(ResolvedParameterDTO::new).toList();
    }

    @JsonSetter("supportedParameters")
    public void setSupportedParameters(List<ResolvedParameterDTO> dto) {
        getBusinessObject().setSupportedParameters(
                dto.stream().map(ResolvedParameterDTO::getBusinessObject).toList()
        );
    }
}

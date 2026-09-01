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

package net.ihe.gazelle.simulation.business.model;

import net.ihe.gazelle.simulation.business.sequence.SimulatedRole;
import net.ihe.gazelle.simulation.business.sequence.TestedRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a resolved simulation sequence, especially with resolved parameters and extended sequence attributes.
 */
public class ResolvedSimulationSequence {

    private String id;
    private String version;
    private String simulatorName;
    private String simulatorVersion;
    private String simulatorUrl;
    private boolean valid;
    private String validReportMessage;
    private List<String> transactions;
    private String shortDescription;
    private String description;
    private Boolean runnable;
    private List<String> standards;
    private List<SimulatedRole> simulatedRoles;
    private List<TestedRole> testedRoles;
    private List<ResolvedSupportedParameter> supportedParameters;

    /**
     * Creates an empty ResolvedSimulationSequence.
     */
    public ResolvedSimulationSequence() {
        transactions = new ArrayList<>();
        standards = new ArrayList<>();
        simulatedRoles = new ArrayList<>();
        testedRoles = new ArrayList<>();
        supportedParameters = new ArrayList<>();
    }

    /**
     * Creates a ResolvedSimulationSequence by copying values from the provided extended sequence.
     *
     * @param copy of a {@link SimulationSequenceExtended}
     */
    public ResolvedSimulationSequence(SimulationSequenceExtended copy) {
        this();
        if (copy != null) {
            id = copy.getId();
            version = copy.getVersion();
            simulatorName = copy.getSimulatorName();
            simulatorVersion = copy.getSimulatorVersion();
            simulatorUrl = copy.getSimulatorUrl();
            valid = copy.isValid();
            validReportMessage = copy.getValidReportMessage();
            transactions = copy.getTransactions();
            shortDescription = copy.getShortDescription();
            description = copy.getDescription();
            runnable = copy.isRunnable();
            standards = copy.getStandards();
            simulatedRoles = new ArrayList<>(
                    copy.getSimulatedRoles()
                            .stream()
                            .map(SimulatedRole::new)
                            .toList()
            );
            testedRoles = new ArrayList<>(
                    copy.getTestedRoles()
                            .stream()
                            .map(TestedRole::new)
                            .toList()
            );
            supportedParameters = new ArrayList<>(
                    copy.getSupportedParameters()
                            .stream()
                            .map(ResolvedSupportedParameter::new)
                            .toList()
            );
        }
    }

    /**
     * Gets the unique identifier of the sequence.
     *
     * @return the sequence identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the sequence.
     *
     * @param id the sequence identifier
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the version of the sequence.
     *
     * @return the sequence version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the sequence.
     *
     * @param version the sequence version
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Gets the name of the simulator providing this sequence.
     *
     * @return the simulator name
     */
    public String getSimulatorName() {
        return simulatorName;
    }

    /**
     * Sets the name of the simulator providing this sequence.
     *
     * @param simulatorName the simulator name
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setSimulatorName(String simulatorName) {
        this.simulatorName = simulatorName;
        return this;
    }

    /**
     * Gets the version of the simulator.
     *
     * @return the simulator version
     */
    public String getSimulatorVersion() {
        return simulatorVersion;
    }

    /**
     * Sets the version of the simulator.
     *
     * @param simulatorVersion the simulator version
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setSimulatorVersion(String simulatorVersion) {
        this.simulatorVersion = simulatorVersion;
        return this;
    }

    /**
     * Gets the base URL of the simulator.
     *
     * @return the simulator URL
     */
    public String getSimulatorUrl() {
        return simulatorUrl;
    }

    /**
     * Sets the base URL of the simulator.
     *
     * @param simulatorUrl the simulator URL
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setSimulatorUrl(String simulatorUrl) {
        this.simulatorUrl = simulatorUrl;
        return this;
    }

    /**
     * Indicates whether the sequence is valid from a business perspective.
     *
     * @return true if valid; false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the business validity of the sequence.
     *
     * @param valid validity flag
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    /**
     * Gets the validation report message, if any.
     *
     * @return the validation report message
     */
    public String getValidReportMessage() {
        return validReportMessage;
    }

    /**
     * Sets the validation report message.
     *
     * @param validReportMessage the validation report message
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setValidReportMessage(String validReportMessage) {
        this.validReportMessage = validReportMessage;
        return this;
    }

    /**
     * Gets the list of transactions involved in the sequence.
     *
     * @return a list of transaction keywords
     */
    public List<String> getTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Sets the list of transactions involved in the sequence.
     *
     * @param transactions a list of transaction keywords
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setTransactions(List<String> transactions) {
        this.transactions = new ArrayList<>(transactions);
        return this;
    }

    /**
     * Gets the short description of the sequence.
     *
     * @return the short description
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Sets the short description of the sequence.
     *
     * @param shortDescription the short description
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
        return this;
    }

    /**
     * Gets the detailed description of the sequence.
     *
     * @return the detailed description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the detailed description of the sequence.
     *
     * @param description the detailed description
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Indicates whether this sequence can be executed automatically.
     *
     * @return true if runnable; false otherwise, or null if unspecified
     */
    public Boolean isRunnable() {
        if (runnable == null) {
            return true;
        }
        return runnable;
    }

    /**
     * Sets whether this sequence can be executed automatically.
     *
     * @param runnable runnable flag
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setRunnable(Boolean runnable) {
        this.runnable = runnable;
        return this;
    }

    /**
     * Gets the list of standards used by the sequence.
     *
     * @return a list of standard identifiers
     */
    public List<String> getStandards() {
        return new ArrayList<>(standards);
    }

    /**
     * Sets the list of standards used by the sequence.
     *
     * @param standards a list of standard identifiers
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setStandards(List<String> standards) {
        this.standards = new ArrayList<>(standards);
        return this;
    }

    /**
     * Gets the roles simulated by the simulator.
     *
     * @return a list of simulated roles
     */
    public List<SimulatedRole> getSimulatedRoles() {
        return new ArrayList<>(simulatedRoles);
    }

    /**
     * Sets the roles simulated by the simulator.
     *
     * @param simulatedRoles a list of simulated roles
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setSimulatedRoles(List<SimulatedRole> simulatedRoles) {
        this.simulatedRoles = new ArrayList<>(simulatedRoles);
        return this;
    }

    /**
     * Gets the roles expected from the system under test.
     *
     * @return a list of tested roles
     */
    public List<TestedRole> getTestedRoles() {
        return new ArrayList<>(testedRoles);
    }

    /**
     * Sets the roles expected from the system under test.
     *
     * @param testedRoles a list of tested roles
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setTestedRoles(List<TestedRole> testedRoles) {
        this.testedRoles = new ArrayList<>(testedRoles);
        return this;
    }

    /**
     * Gets the list of supported configuration parameters for this sequence.
     *
     * @return a list of supported parameters
     */
    public List<ResolvedSupportedParameter> getSupportedParameters() {
        return new ArrayList<>(supportedParameters);
    }

    /**
     * Sets the list of supported configuration parameters for this sequence.
     *
     * @param supportedParameters a list of supported parameters
     * @return this instance for fluent usage
     */
    public ResolvedSimulationSequence setSupportedParameters(List<ResolvedSupportedParameter> supportedParameters) {
        this.supportedParameters = new ArrayList<>(supportedParameters);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ResolvedSimulationSequence that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(version, that.version)
                && Objects.equals(simulatorName, that.simulatorName)
                && Objects.equals(simulatorVersion, that.simulatorVersion)
                && Objects.equals(simulatorUrl, that.simulatorUrl)
                && Objects.equals(valid, that.valid)
                && Objects.equals(validReportMessage, that.validReportMessage)
                && Objects.equals(transactions, that.transactions)
                && Objects.equals(shortDescription, that.shortDescription)
                && Objects.equals(description, that.description)
                && Objects.equals(runnable, that.runnable)
                && Objects.equals(standards, that.standards)
                && Objects.equals(simulatedRoles, that.simulatedRoles)
                && Objects.equals(testedRoles, that.testedRoles)
                && Objects.equals(supportedParameters, that.supportedParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, simulatorName, simulatorVersion, simulatorUrl, valid, validReportMessage, transactions, shortDescription, description, runnable, standards, simulatedRoles, testedRoles, supportedParameters);
    }
}

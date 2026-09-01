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

package net.ihe.gazelle.itb.gateway.business.reporting;

import java.util.Objects;

/**
 * ITB system descriptor sent in callback payload.
 */
public class ItbSystem {

    private Long id;
    private String shortName;
    private String fullName;

    /**
     * Creates an empty system descriptor.
     */
    public ItbSystem() {
    }

    /**
     * Creates a fully initialized system descriptor.
     *
     * @param id system identifier
     * @param shortName short label
     * @param fullName full label
     */
    public ItbSystem(Long id, String shortName, String fullName) {
        this.id = id;
        this.shortName = shortName;
        this.fullName = fullName;
    }

    /**
     * Returns system identifier.
     *
     * @return system identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets system identifier.
     *
     * @param id system identifier
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns system short name.
     *
     * @return short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets system short name.
     *
     * @param shortName short name
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Returns system full name.
     *
     * @return full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets system full name.
     *
     * @param fullName full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItbSystem systemDTO = (ItbSystem) o;
        return Objects.equals(id, systemDTO.id) &&
                Objects.equals(shortName, systemDTO.shortName) &&
                Objects.equals(fullName, systemDTO.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shortName, fullName);
    }

    @Override
    public String toString() {
        return "SystemDTO{" +
                "id=" + id +
                ", shortName='" + shortName + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}

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

package net.ihe.gazelle.user.management.core.interlay.publisher;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;

/**
 * DTO for organization management events, used for publishing events related to organization creations and updates.
 */
public class OrganizationManagementEventDto {

    /** type of the event can be organization:created or organization:updated **/
    private String type;
    /** shortname of the associated organization **/
    private String shortname;
    /** name of the associated organization **/
    private String name;

    /**
     * Default constructor
     */
    public OrganizationManagementEventDto() {
        // For Jackson
    }

    @JsonGetter("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter("shortname")
    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    @JsonGetter("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationManagementEventDto that = (OrganizationManagementEventDto) o;
        return Objects.equals(type, that.type) && Objects.equals(shortname, that.shortname) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, shortname, name);
    }
}


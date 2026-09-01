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

package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import jakarta.persistence.*;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "organization", schema = "gum")
@Inheritance(strategy = InheritanceType.JOINED)
public class OrganizationEntity {

    @Id
    private String id;

    @Column(name="name", unique = true)
    private String name;

    @Column(name="shortname", unique = true, length = 32)
    private String shortname;

    @Column(name="archived", nullable = false, columnDefinition = "boolean not null default false")
    private Boolean archived = false;

    @UpdateTimestamp
    @Column(name="last_update_timestamp")
    private Timestamp lastUpdateTimestamp;

    public OrganizationEntity() { /* for JPA */ }

    public OrganizationEntity(String id, String name, String shortname) {
        this.setId(id);
        this.setName(name);
        this.setShortname(shortname);
    }

    public OrganizationEntity(Organization organization) {
        this.setId(organization.getId());
        this.setName(organization.getName());
        this.setShortname(organization.getShortname());
    }


    public String getId() {
        return id;
    }

    public OrganizationEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public OrganizationEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getShortname() {
        return shortname;
    }

    public OrganizationEntity setShortname(String shortname) {
        this.shortname = shortname;
        return this;
    }

    public Boolean isArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Timestamp getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public Organization asOrganization() {
        Organization organization =  new Organization(getId(), getShortname(), getName());
        organization.setArchived(isArchived());
        if (lastUpdateTimestamp != null) {
            organization.setLastUpdateTimestamp(lastUpdateTimestamp.getTime());
        }
        return organization;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationEntity that = (OrganizationEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(shortname, that.shortname)
                && Objects.equals(archived, that.archived)
                && Objects.equals(lastUpdateTimestamp, that.lastUpdateTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, shortname, archived, lastUpdateTimestamp);
    }
}

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
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;

import java.util.Objects;

@Entity
@Table(name = "delegated_organization", schema = "gum")
@PrimaryKeyJoinColumn(name = "organization_id", foreignKey = @ForeignKey(name = "fk_delegated_organization_organization"))
public class DelegatedOrganizationEntity extends OrganizationEntity {

    @Column(name="external_id")
    private String externalId;
    @Column(name="idp_id")
    private String idpId;

    public DelegatedOrganizationEntity() {
        // for JPA
    }

    public DelegatedOrganizationEntity(Organization organization, String externalId, String idpId) {
        super(organization);
        this.setIdpId(idpId);
        this.setExternalId(externalId);
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getIdpId() {
        return idpId;
    }

    public void setIdpId(String idpId) {
        this.idpId = idpId;
    }

    @Override
    public DelegatedOrganization asOrganization() {
        Organization organization = super.asOrganization();
        return new DelegatedOrganization(organization, externalId, idpId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DelegatedOrganizationEntity that = (DelegatedOrganizationEntity) o;
        return Objects.equals(externalId, that.externalId) && Objects.equals(idpId, that.idpId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), externalId, idpId);
    }
}

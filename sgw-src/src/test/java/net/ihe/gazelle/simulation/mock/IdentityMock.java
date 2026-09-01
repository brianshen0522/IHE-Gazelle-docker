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

package net.ihe.gazelle.simulation.mock;

import net.ihe.gazelle.security.business.GazelleIdentity;

import java.security.Principal;
import java.util.Set;

public class IdentityMock implements GazelleIdentity {

   private Set<String> groups;
   private String id;
   private String orgaId;

   public IdentityMock(Set<String> groups) {
      this.groups = groups;
      this.id="";
   }

   @Override
   public String getId() {
      return id;
   }

   @Override
   public String getName() {
      return "";
   }

   @Override
   public Set<String> getGroups() {
      return groups;
   }

   @Override
   public String getOrganizationGroup() {
      return null;
   }

   @Override
   public String getOrganizationId() {
      return orgaId;
   }

   @Override
   public Principal getPrincipal() {
      return null;
   }

   @Override
   public boolean isAuthenticated() {
      return true;
   }

   @Override
   public boolean hasGroup(String groupName) {
      return groups.contains(groupName);
   }

   public IdentityMock setGroups(Set<String> groups) {
      this.groups = groups;
      return this;
   }

   public IdentityMock setIdentityId(String id) {
      this.id = id;
      return this;
   }

   public IdentityMock setOrganizationId(String orgaId) {
      this.orgaId = orgaId;
      return this;
   }
}
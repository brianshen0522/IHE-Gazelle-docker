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

package net.ihe.gazelle.user.management.commons.application.user.registration;

import net.ihe.gazelle.security.business.GazelleIdentity;

import java.security.Principal;
import java.util.Set;

/**
 * This class is used to represent the identity of a user in the GUM (Gazelle User Management) system.
 * It implements the Principal interface, which represents an entity (such as an individual user or a group)
 * that can be authenticated and authorized within the system.
 */
public class GUMIdentity implements GazelleIdentity {

    public static final String GUM_ADMIN = "gum_admin";
    @Override
    public String getId() {
        return "gumID";
    }

    @Override
    public String getName() {
        return "GUM admin";
    }

    @Override
    public Set<String> getGroups() {
        return Set.of(GUM_ADMIN);
    }

    @Override
    public String getOrganizationGroup() { return null; }

    @Override
    public String getOrganizationId() { return null; }

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
        return getGroups().contains(groupName);
    }
}

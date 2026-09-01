/*
 * Copyright 2024 IHE International.
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

package net.ihe.gazelle.user.management.api.domain.group;

import java.util.Arrays;
import java.util.Optional;

/**
 * GroupType represents the type of group in the user management system.
 */
public enum GroupType {

    /** A group representing an organization. */
    ORGANIZATION("org"),
    /** A group representing an organization administrator. */
    ORGANIZATION_ADMIN("org-adm"),
    /** A group representing a static role. */
    ROLE("role");

    private final String prefix;

    /**
     * Constructs a GroupType with the given prefix.
     * @param prefix the prefix of the group type
     */
    GroupType(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Returns the prefix of the group type.
     * @return the prefix of the group type
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Returns the GroupType corresponding to the given prefix.
     * @param prefix the prefix of the group type to retrieve
     * @return the GroupType corresponding to the given prefix
     * @throws IllegalArgumentException if the given prefix doesn't exist
     */
    public static GroupType getTypeFromPrefix(String prefix) {
        Optional<GroupType> optionalGroupType = Arrays.stream(GroupType.values())
                .filter(groupType -> groupType.getPrefix().equals(prefix))
                .findFirst();

        return optionalGroupType.orElseThrow(() -> new IllegalArgumentException("Given prefix doesn't exist"));
    }

}

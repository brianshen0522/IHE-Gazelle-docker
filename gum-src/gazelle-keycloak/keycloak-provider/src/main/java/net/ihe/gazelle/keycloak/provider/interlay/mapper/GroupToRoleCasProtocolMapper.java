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

package net.ihe.gazelle.keycloak.provider.interlay.mapper;

import org.keycloak.models.*;
import org.keycloak.protocol.cas.mappers.AbstractCASProtocolMapper;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupToRoleCasProtocolMapper extends AbstractCASProtocolMapper {

    public static final String PROVIDER_ID = "gazelle-groups-to-cas-roles-mapper";

    @Override
    public int getPriority() {
        return 50; // VLD increase priority to be before the role mapper of gazelle_microprofile-jwt
    }

    @Override
    public String getDisplayCategory() {
        return "GUM Group Mapper to Gazelle legacy roles";
    }

    @Override
    public String getDisplayType() {
        return "GUM groups to legacy roles mapper";
    }

    @Override
    public String getHelpText() {
        return "Bind new groups model to old roles (ex: group:gazelle_admin -> admin_role)";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void setAttribute(Map<String, Object> attributes, ProtocolMapperModel mappingModel, UserSessionModel userSession,
                             KeycloakSession session, ClientSessionContext clientSessionCt) {
        Stream<String> userRoles = userSession.getUser().getRoleMappingsStream()
                .map(RoleModel::getName)
                .map(GroupToRoleCasProtocolMapper::getOldRoleFromGroup)
                .filter(Objects::nonNull);

        userRoles = Stream.concat(userRoles, Stream.of("user_role"));
        String userRolesString = "[" + userRoles.collect(Collectors.joining (",")) + "]";
        attributes.put("role_name", userRolesString);
    }

    public static String getOldRoleFromGroup(String group) {
        String groupMatched = switch (group) {
            case "role:gazelle_admin" -> "admin_role";
            case "role:monitor" -> "monitor_role";
            case "role:testing_session_manager" -> "testing_session_admin_role";
            case "role:late_registration" -> "vendor_late_registration_role";
            case "role:project_admin" -> "project-manager_role";
            case "role:test_designer" -> "tests_editor_role";
            case "role:sut_operator" -> "vendor_role";
            default -> null;
        };

        if(groupMatched == null && group.startsWith("org-adm"))
            return "vendor_admin_role";

        return groupMatched;
    }
}

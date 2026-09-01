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

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupToRoleOIDCProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper,
        OIDCAccessTokenResponseMapper, TokenIntrospectionTokenMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, GroupToRoleOIDCProtocolMapper.class);
    }

    public static final String PROVIDER_ID = "gazelle-groups-to-roles-oidc-mapper";


    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public int getPriority() {
        return 50; // VLD increase priority to be before the role mapper of gazelle_microprofile-jwt
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    public String getDisplayCategory() {
        return "GUM Group Mapper to Gazelle old roles";
    }

    @Override
    public String getDisplayType() {
        return "GUM groups to old roles mapper";
    }

    @Override
    public String getHelpText() {
        return "Bind new groups model to old roles (ex: group:gazelle_admin -> admin_role)";
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
        mappingModel.getConfig().put("jsonType.label", "JSON");

        Stream<String> userRoles = userSession.getUser().getRoleMappingsStream()
                .map(RoleModel::getName)
                .map(GroupToRoleCasProtocolMapper::getOldRoleFromGroup)
                .filter(Objects::nonNull)
                .map(roles -> "\"" + roles + "\"");

        userRoles = Stream.concat(userRoles, Stream.of("\"user_role\""));
        String userRolesString = "[" + userRoles.collect(Collectors.joining(",")) + "]";

        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, userRolesString);
    }
}

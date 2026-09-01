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

package net.ihe.gazelle.maestro.quarkus.websocket.dto;

import com.fasterxml.jackson.annotation.*;
import net.ihe.gazelle.maestro.api.business.message.UserInteractionCompleted;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import net.ihe.gazelle.oidc.common.technical.dto.SecuredMessageDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
      name = "UserInteractionCompleted",
      description = "Message sent when a user interaction is completed."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeName(UserInteractionCompletedDTO.TYPE)
@JsonPropertyOrder({
      "type",
      "inputs"
})
public class UserInteractionCompletedDTO extends MessageDTO<UserInteractionCompleted> implements SecuredMessageDTO {

   @JsonIgnore
   public static final String TYPE = "USER_INTERACTION_COMPLETED";

   private final String authorization;

   public UserInteractionCompletedDTO() {
      this(new UserInteractionCompleted(), "");
   }

   public UserInteractionCompletedDTO(UserInteractionCompleted userInteractionCompleted,  String authorization) {
      super(userInteractionCompleted);
      this.authorization = authorization;
   }

   @Schema(
         name = "inputs",
         description = "The inputs provided by the user."
   )
   @SuppressWarnings("rawtypes")
   @JsonProperty(value = "inputs")
   public List<PropertyDTO> getInputs() {
      return message.getInputs().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(PropertyDTO.class::cast)
            .toList();
   }

   @SuppressWarnings("rawtypes")
   public void setInputs(List<PropertyDTO> inputs) {
      message.addInputs(
            inputs.stream()
                  .map(PropertyDTO::getBusinessObject)
                  .toList()
      );
   }

   @Override
   public String getAuthorization() {
      return authorization;
   }

   @Override
   @JsonGetter
   public Class<UserInteractionCompleted> getBusinessObjectClass() {
      return UserInteractionCompleted.class;
   }
}



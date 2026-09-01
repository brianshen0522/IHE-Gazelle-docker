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
import net.ihe.gazelle.maestro.api.business.message.InteractWithUser;
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.maestro.api.technical.dto.test.SupportedInputDTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
      name = "InteractWithUser",
      description = "Message sent when a step is asking for user interaction."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeName(InteractWithUserDTO.TYPE)
@JsonPropertyOrder({
      "interactionTitle",
      "message",
      "timeout",
      "supportedInputs"
})
public class InteractWithUserDTO extends MessageDTO<InteractWithUser> {

   @JsonIgnore
   public static final String TYPE = "INTERACT_WITH_USER";

   public InteractWithUserDTO() {
      super(new InteractWithUser());
   }

   public InteractWithUserDTO(InteractWithUser interactWithUser) {
      super(interactWithUser);
   }

   @Schema(
         name = "interactionTitle",
         description = "The title of the user interaction."
   )
   @JsonProperty("interactionTitle")
   public String getInteractionTitle() {
      return message.getInteractionTitle();
   }

   public void setInteractionTitle(String interactionTitle) {
      message.setInteractionTitle(interactionTitle);
   }

   @Schema(
         name = "message",
         description = "The text giving the instructions to the user for performing the interaction."
   )
   @JsonProperty("message")
   public String getMessage() {
      return message.getMessage();
   }

   public void setMessage(String message) {
      super.message.setMessage(message);
   }

   @Schema(
         name = "timeout",
         description = "The maximum amount of time in milliseconds to perform the user interaction."
   )
   @JsonProperty("timeout")
   public Long getTimeout() {
      return message.getTimeout();
   }

   public void setTimeout(Long timeout) {
      message.setTimeout(timeout);
   }

   @Schema(
         name = "supportedInputs",
         description = "The list of input that will be asked to the user."
   )
   @SuppressWarnings("rawtypes")
   @JsonProperty("supportedInputs")
   public List<SupportedInputDTO> getSupportedInputs() {
      return message.getSupportedInputs().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(SupportedInputDTO.class::cast)
            .toList();
   }

   @SuppressWarnings("rawtypes")
   public void setSupportedInputs(List<SupportedInputDTO> supportedInputs) {
      message.addSupportedInputs(
            supportedInputs != null ?
                  supportedInputs.stream()
                        .map(DTO::getBusinessObject)
                        .map(SupportedInput.class::cast)
                        .toList() :
                  null
      );
   }

   @Override
   @JsonIgnore
   public Class<InteractWithUser> getBusinessObjectClass() {
      return InteractWithUser.class;
   }
}

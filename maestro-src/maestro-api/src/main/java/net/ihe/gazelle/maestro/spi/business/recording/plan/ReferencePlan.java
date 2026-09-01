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

package net.ihe.gazelle.maestro.spi.business.recording.plan;

import java.util.Objects;

/**
 * Reference definition linking an item to another persisted object.
 */
public class ReferencePlan {

   private final MaestroRefType refType;
   private final String name;
   private final String targetType;
   private final Object target;

   private ReferencePlan(MaestroRefType refType,
                         String name,
                         String targetType,
                         Object target) {
      this.refType = refType;
      this.name = name;
      this.targetType = targetType;
      this.target = target;
   }

   /**
    * Creates an item-id reference.
    *
    * @param name reference name
    * @param targetType target type
    * @param target target item plan
    * @return item reference plan
    */
   public static ReferencePlan forItem(String name, String targetType, Object target) {
      return new ReferencePlan(MaestroRefType.ITEM_ID, name, targetType, target);
   }

   /**
    * Creates an attachment reference.
    *
    * @param name reference name
    * @param attachmentPlan attachment plan
    * @return attachment reference plan
   */
   public static ReferencePlan forAttachment(String name, AttachmentPlan attachmentPlan) {
      return forAttachment(name, attachmentPlan.type(), attachmentPlan);
   }

   /**
    * Creates an attachment reference with an explicit reference type.
    *
    * @param name reference name
    * @param targetType logical reference type stored on the Datahouse reference
    * @param attachmentPlan attachment plan
    * @return attachment reference plan
    */
   public static ReferencePlan forAttachment(String name, String targetType, AttachmentPlan attachmentPlan) {
      return new ReferencePlan(MaestroRefType.ATTACHMENT, name, targetType, attachmentPlan);
   }

   /**
    * Creates an attachment reference to an already persisted attachment.
    *
    * @param name reference name
    * @param type target type
    * @param attachmentId target attachment identifier
    * @return attachment reference plan
    */
   public static ReferencePlan forAttachmentId(String name, String type, String attachmentId) {
      return new ReferencePlan(MaestroRefType.ATTACHMENT, name, type, attachmentId);
   }

   /**
    * Creates an URL reference.
    *
    * @param name reference name
    * @param type target type
    * @param url target URL
    * @return URL reference plan
    */
   public static ReferencePlan forURL(String name, String type, String url) {
      return new ReferencePlan(MaestroRefType.URL, name, type, url);
   }

   /**
    * Returns reference kind.
    *
    * @return reference kind
    */
   public MaestroRefType getRefType() {
      return refType;
   }

   /**
    * Returns logical reference name.
    *
    * @return reference name
    */
   public String getName() {
      return name;
   }

   /**
    * Returns target type.
    *
    * @return target type
    */
   public String getTargetType() {
      return targetType;
   }

   /**
    * Returns target payload.
    *
    * @return target object
    */
   public Object getTarget() {
      return target;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ReferencePlan that)) {
         return false;
      }
      return refType == that.refType && Objects.equals(name, that.name)
            && Objects.equals(targetType, that.targetType) && Objects.equals(target, that.target);
   }

   @Override
   public int hashCode() {
      return Objects.hash(refType, name, targetType, target);
   }

}

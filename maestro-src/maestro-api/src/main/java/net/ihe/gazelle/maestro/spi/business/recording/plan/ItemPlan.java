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

import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;

import java.util.*;

/**
 * Mutable item persistence plan with deferred references and optional in-place mutations.
 *
 * @param <T> business object type to persist
 */
public class ItemPlan<T> {

   private final String type;
   private final T itemObject;
   private final TextMarshaller<T> textMarshaller;
   private final List<FutureReferencePlan<T>> futureReferencePlans = new ArrayList<>();
   private final Map<String, String> additionalParameters = new HashMap<>();

   /**
    * Creates a new item plan.
    *
    * @param type item type
    * @param itemObject business object to persist
    * @param textMarshaller serializer used to produce item payload
    */
   public ItemPlan(String type, T itemObject, TextMarshaller<T> textMarshaller) {
      this.type = type;
      this.itemObject = itemObject;
      this.textMarshaller = textMarshaller;
   }

   /**
    * Returns item type.
    *
    * @return item type
    */
   public String type() {
      return type;
   }

   /**
    * Returns mutable business object held by this plan.
    *
    * @return business object
    */
   public T itemObject() {
      return itemObject;
   }

   /**
    * Returns item serializer.
    *
    * @return text marshaller
    */
   public TextMarshaller<T> textMarshaller() {
      return textMarshaller;
   }

   /**
    * Returns a copy of deferred references.
    *
    * @return deferred references
    */
   public List<FutureReferencePlan<T>> futureReferencePlans() {
      return new ArrayList<>(futureReferencePlans);
   }

   /**
    * Adds a child item reference without mutation.
    *
    * @param referenceName reference name
    * @param itemPlan target item plan
    * @return current plan
    */
   public ItemPlan<T> withItemReference(String referenceName, ItemPlan<?> itemPlan) {
      return withItemReference(referenceName, itemPlan, null);
   }

   /**
    * Adds a child item reference with optional mutation.
    *
    * @param referenceName reference name
    * @param itemPlan target item plan
    * @param mutation optional mutation
    * @return current plan
    */
   public ItemPlan<T> withItemReference(String referenceName, ItemPlan<?> itemPlan, ItemObjectMutation<T> mutation) {
      return addFutureReferencePlan(ReferencePlan.forItem(referenceName, itemPlan.type(), itemPlan), mutation);
   }

   /**
    * Adds an attachment reference without mutation.
    *
    * @param referenceName reference name
    * @param attachmentPlan attachment plan
    * @return current plan
    */
   public ItemPlan<T> withAttachmentReference(String referenceName, AttachmentPlan attachmentPlan) {
      return withAttachmentReference(referenceName, attachmentPlan, null);
   }

   /**
    * Adds an attachment reference with optional mutation.
    *
    * @param referenceName reference name
    * @param attachmentPlan attachment plan
    * @param mutation optional mutation
    * @return current plan
    */
   public ItemPlan<T> withAttachmentReference(String referenceName,
                                              AttachmentPlan attachmentPlan,
                                              ItemObjectMutation<T> mutation) {
      return addFutureReferencePlan(ReferencePlan.forAttachment(referenceName, attachmentPlan), mutation);
   }



   /**
    * Add a child reference plan, with an optional mutation to apply to the item once the target of the reference has
    * been persisted.
    *
    * @param referencePlan the plan describing the reference to add
    * @param mutation Optional mutation to perform once the target of the reference has been persisted.
    *
    * @return this item plan
    */
   public ItemPlan<T> addFutureReferencePlan(ReferencePlan referencePlan, ItemObjectMutation<T> mutation) {
      this.futureReferencePlans.add(new FutureReferencePlan<>(referencePlan, mutation));
      return this;
   }

   /**
    * Returns a defensive copy of additional parameters.
    *
    * @return additional parameters
    */
   public Map<String, String> additionalParameters() {
      return new HashMap<>(additionalParameters);
   }

   /**
    * Adds one additional parameter.
    *
    * @param key parameter key
    * @param value parameter value
    * @return current plan
    */
   public ItemPlan<T> addAdditionalParameter(String key, String value) {
      this.additionalParameters.put(key, value);
      return this;
   }

   /**
    * Adds many additional parameters.
    *
    * @param additionalParameters parameters to merge
    * @return current plan
    */
   public ItemPlan<T> addAdditionalParameters(Map<String, String> additionalParameters) {
      this.additionalParameters.putAll(additionalParameters);
      return this;
   }

   /**
    * Creates a deep copy of this plan graph while preserving shared/cyclic references.
    *
    * @return deep-copied item plan
    */
   public ItemPlan<T> deepCopy() {
      return deepCopy(new IdentityHashMap<>());
   }

   @SuppressWarnings("unchecked")
   private ItemPlan<T> deepCopy(Map<ItemPlan<?>, ItemPlan<?>> copies) {
      ItemPlan<?> existingCopy = copies.get(this);
      if (existingCopy != null) {
         return (ItemPlan<T>) existingCopy;
      }

      ItemPlan<T> copy = new ItemPlan<>(type, itemObject, textMarshaller);
      copies.put(this, copy);
      copy.additionalParameters.putAll(this.additionalParameters);

      for (FutureReferencePlan<T> futureReferencePlan : this.futureReferencePlans) {
         ReferencePlan copiedReferencePlan = copyReferencePlan(futureReferencePlan.referencePlan(), copies);
         copy.futureReferencePlans.add(new FutureReferencePlan<>(copiedReferencePlan, futureReferencePlan.mutation()));
      }
      return copy;
   }

   private static ReferencePlan copyReferencePlan(ReferencePlan referencePlan, Map<ItemPlan<?>, ItemPlan<?>> copies) {
      return switch (referencePlan.getRefType()) {
         case ITEM_ID -> {
            Object target = referencePlan.getTarget();
            Object copiedTarget = target instanceof ItemPlan<?> itemPlan ? itemPlan.deepCopy(copies) : target;
            yield ReferencePlan.forItem(referencePlan.getName(), referencePlan.getTargetType(), copiedTarget);
         }
         case ATTACHMENT -> {
            Object target = referencePlan.getTarget();
            if (target instanceof AttachmentPlan attachmentPlan) {
               yield ReferencePlan.forAttachment(referencePlan.getName(), referencePlan.getTargetType(), attachmentPlan);
            }
            if (!(target instanceof String attachmentId)) {
               throw new IllegalStateException("Unsupported ATTACHMENT reference target: " + target);
            }
            yield ReferencePlan.forAttachmentId(
                  referencePlan.getName(),
                  referencePlan.getTargetType(),
                  attachmentId
            );
         }
         case URL -> ReferencePlan.forURL(
               referencePlan.getName(),
               referencePlan.getTargetType(),
               (String) referencePlan.getTarget()
         );
      };
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ItemPlan<?> itemPlan)) {
         return false;
      }
      return Objects.equals(type, itemPlan.type) && Objects.equals(itemObject, itemPlan.itemObject)
            && Objects.equals(textMarshaller, itemPlan.textMarshaller)
            && Objects.equals(futureReferencePlans, itemPlan.futureReferencePlans)
            && Objects.equals(additionalParameters, itemPlan.additionalParameters);
   }

   @Override
   public int hashCode() {
      return Objects.hash(type, itemObject, textMarshaller, futureReferencePlans, additionalParameters);
   }

}

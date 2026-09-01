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

import net.ihe.gazelle.security.business.acl.AccessControlList;

import java.util.Objects;

/**
 * Root persistence plan containing the root item and ACL to apply when persisting.
 *
 * @param <T> root item business object type
 */
public class PersistencePlan<T> {

   private final ItemPlan<T> rootItem;
   private final AccessControlList acl;

   /**
    * Creates a persistence plan.
    *
    * @param rootItem root item plan
    * @param acl access control list to apply
    */
   public PersistencePlan(ItemPlan<T> rootItem, AccessControlList acl) {
      this.rootItem = rootItem == null ? null : rootItem.deepCopy();
      this.acl = acl;
   }

   /**
    * Returns a defensive deep copy of the root item plan.
    *
    * @return root item plan
    */
   public ItemPlan<T> getRootItem() {
      return rootItem == null ? null : rootItem.deepCopy();
   }

   /**
    * Returns ACL associated with this persistence plan.
    *
    * @return ACL
    */
   public AccessControlList getAcl() {
      return acl;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof PersistencePlan<?> that)) {
         return false;
      }
      return Objects.equals(rootItem, that.rootItem) && Objects.equals(acl, that.acl);
   }

   @Override
   public int hashCode() {
      return Objects.hash(rootItem, acl);
   }
}

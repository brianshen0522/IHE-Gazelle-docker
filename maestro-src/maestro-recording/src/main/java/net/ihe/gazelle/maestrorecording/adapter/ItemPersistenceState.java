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

package net.ihe.gazelle.maestrorecording.adapter;

import java.util.Arrays;
import java.util.Objects;

record ItemPersistenceState(String[] itemIds, String[] itemUrls) {
    static final ItemPersistenceState EMPTY = new ItemPersistenceState(new String[0], new String[0]);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemPersistenceState(String[] ids, String[] urls))) return false;
        return Objects.deepEquals(itemIds, ids) && Objects.deepEquals(itemUrls, urls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(itemIds), Arrays.hashCode(itemUrls));
    }

    @Override
    public String toString() {
        return "ItemPersistenceState{" +
                "itemIds=" + Arrays.toString(itemIds) +
                ", itemUrls=" + Arrays.toString(itemUrls) +
                '}';
    }
}

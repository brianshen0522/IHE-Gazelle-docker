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

package net.ihe.gazelle.simulation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ResourceRetriever {

    public static String getResourceAsString(String resourcePath) throws IOException {
        InputStream inputStream = ResourceRetriever.class.getClassLoader().getResourceAsStream(resourcePath);
        Objects.requireNonNull(inputStream, "Resource not found: " + resourcePath);
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}

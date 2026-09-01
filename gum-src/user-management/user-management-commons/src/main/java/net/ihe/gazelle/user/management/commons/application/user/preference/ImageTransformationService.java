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

package net.ihe.gazelle.user.management.commons.application.user.preference;

public interface ImageTransformationService {

    /**
     * Transform the image in parameter to an image in JPEG format and resize it if necessary (200x200)
     * @param imageBytes the bytes of the original image
     * @return the transformed image
     */
    byte[] transformImageToJpeg(byte[] imageBytes);

    /**
     * Generate a thumbnail format of a JPEG image in parameter (48x48)
     * @param imageBytes the bytes of the original image
     * @return the thumbnail image
     */
    byte[] generateThumbnail(byte[] imageBytes);
}

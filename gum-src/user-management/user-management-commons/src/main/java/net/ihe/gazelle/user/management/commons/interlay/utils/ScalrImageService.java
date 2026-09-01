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

package net.ihe.gazelle.user.management.commons.interlay.utils;

import net.ihe.gazelle.user.management.api.application.user.preference.UserPreferenceServiceException;
import net.ihe.gazelle.user.management.commons.application.user.preference.ImageTransformationService;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Implementation of the ImageTransformationService interface using the Scalr library for image processing.
 * This class provides methods to transform images to JPEG format and generate thumbnails while maintaining image quality.
 */
public class ScalrImageService implements ImageTransformationService {

    private static final int MAX_HEIGHT = 200;
    private static final int MAX_WIDTH = 200;
    private static final int THUMBNAIL_WIDTH = 48;
    private static final int THUMBNAIL_HEIGHT = 48;
    private static final String JPEG_FORMAT_NAME = "JPEG";
    private static final String JPEG_START_OF_IMAGE = "ffd8";

    /** Default constructor */
    public ScalrImageService() {
        // Nothing to initialize
    }

    @Override
    public byte[] transformImageToJpeg(byte[] picture) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(picture)) {
            BufferedImage image = ImageIO.read(byteArrayInputStream);
            boolean needToResizeImage = needToResizeImage(image);
            boolean imageAJpeg = isImageAJpeg(picture);

            if (!needToResizeImage && imageAJpeg) {
                //Even if we do not need to do any transformation on the picture, then we return it as is.
                //We do not rewrite image as it can compress it again and thus loosing quality.
                return picture;
            }

            if (needToResizeImage) {
                //Default Scalr modes are used and thus image ratio is kept.
                image = Scalr.resize(image, MAX_WIDTH, MAX_HEIGHT);
            }

            if (!imageAJpeg) {
                final BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                //As jpeg does not support transparency, it is replaced with a white background
                convertedImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
                return getWrittenImageAsBytes(convertedImage);
            } else {
                return getWrittenImageAsBytes(image);
            }

        } catch (IOException e) {
            throw new UserPreferenceServiceException("Error while transforming image", e);
        }
    }

    @Override
    public byte[] generateThumbnail(byte[] imageBytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes)) {
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);

            BufferedImage thumbnailImage = Scalr.resize(bufferedImage, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Scalr.OP_ANTIALIAS);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                boolean written = ImageIO.write(thumbnailImage, JPEG_FORMAT_NAME, out);
                if (!written)
                    throw new IllegalStateException("Failed to write thumbnail.");
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new UserPreferenceServiceException("Error during thumbnail generation", e);
        }
    }

    private byte[] getWrittenImageAsBytes(BufferedImage convertedImage) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            boolean written = ImageIO.write(convertedImage, JPEG_FORMAT_NAME, out);
            if (!written)
                throw new IllegalStateException("Failed to write image.");
            return out.toByteArray();
        }
    }

    private boolean isImageAJpeg(byte[] image) {
        String imageToHex = String.format("%x", new BigInteger(1, image));
        //If an image starts with ffd8 then it means it is a JPEG.
        //Source : https://en.wikipedia.org/wiki/JPEG#Syntax_and_structure
        return imageToHex.startsWith(JPEG_START_OF_IMAGE);
    }


    private boolean needToResizeImage(BufferedImage image) {
        return image.getHeight() > MAX_HEIGHT || image.getWidth() > MAX_WIDTH;
    }
}

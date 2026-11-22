package com.game.spaceshooter.infrastructure.utils;

import com.game.shooting2DGame.infrastructure.utils.ImageLoader;
import com.game.shooting2DGame.infrastructure.utils.ResourceLoadException;
import com.game.shooting2DGame.infrastructure.utils.ResourceLoader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ResourceLoaderTest {

    @Nested
    @DisplayName("ResourceLoader.open")
    class OpenTests {

        @Test
        @DisplayName("open with relative path throws ResourceLoadException")
        void open_relativePath_throws() {
            assertThrows(ResourceLoadException.class,
                    () -> ResourceLoader.open("images/ship.png"));
        }

        @Test
        @DisplayName("open with missing resource throws ResourceLoadException")
        void open_missingResource_throwsResourceLoadException() {
            assertThrows(ResourceLoadException.class,
                    () -> ResourceLoader.open("/nonexistent/resource.png"));
        }
    }

    @Nested
    @DisplayName("ImageLoader.loadImage")
    class ImageLoaderTests {

        @Test
        @DisplayName("loadImage for missing resource returns placeholder of requested size (32×32)")
        void loadImage_missing_returnsPlaceholderOfRequestedSize() {
            BufferedImage img = ImageLoader.loadImage("/does/not/exist.png");

            assertNotNull(img, "placeholder image should not be null");
            assertEquals(32, img.getWidth(), "placeholder width");
            assertEquals(32, img.getHeight(), "placeholder height");
            assertEquals(BufferedImage.TYPE_INT_ARGB, img.getType(),
                    "placeholder should be ARGB");
        }
    }
}

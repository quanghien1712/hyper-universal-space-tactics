package com.game.shooting2DGame.infrastructure.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;

public class ImageLoader {
    private static final Map<String, BufferedImage> imageCache = new ConcurrentHashMap<>();
    private static final Map<String, BufferedImage> spriteCache = new ConcurrentHashMap<>();


    private ImageLoader() {}

    public static BufferedImage loadImage(String path) {
        return imageCache.computeIfAbsent(path, key -> load(key, 32, 32));
    }

    private static BufferedImage load(String path, int fallbackWidth, int fallbackHeight) {
        try (InputStream is = ResourceLoader.open(path)) {
            BufferedImage img = ImageIO.read(is);
            if (img == null) {
                throw new ResourceLoadException(path, "unsupported or corrupt image");
            }
            return img;
        } catch (IOException | ResourceLoadException e) {
            ResourceLoader.warn(path, e, "using placeholder image");
            return placeholder(fallbackWidth, fallbackHeight);
        }
    }

    private static BufferedImage placeholder(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.MAGENTA);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);
            for (int x = 0; x < width; x += 16) {
                for (int y = 0; y < height; y += 16) {
                    if ((x / 16 + y / 16) % 2 == 0) graphics.fillRect(x, y, 16, 16);
                }
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }
}

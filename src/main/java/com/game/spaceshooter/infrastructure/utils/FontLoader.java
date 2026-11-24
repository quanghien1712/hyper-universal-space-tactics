package com.game.spaceshooter.infrastructure.utils;

import java.awt.Font;
import java.io.InputStream;
import java.awt.FontFormatException;
import java.io.IOException;

public class FontLoader {

    private FontLoader() {
    }

    public static Font loadEthnocentricOrFallback(float size, String fallbackName, int fallbackStyle) {
        return loadOrFallback("/font/Ethnocentric.otf", size, fallbackName, fallbackStyle);
    }

    public static Font loadOrFallback(String path, float size, String fallbackName, int fallbackStyle) {
        if (size <= 0) throw new IllegalArgumentException("Font size must be positive");
        try (InputStream is = ResourceLoader.open(path)) {
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        } catch (IOException | FontFormatException | ResourceLoadException e) {
            ResourceLoader.warn(path, e, "using system font");
        }
        return new Font(fallbackName, fallbackStyle, 1).deriveFont(size);
    }
}

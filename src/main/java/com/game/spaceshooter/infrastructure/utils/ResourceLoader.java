package com.game.spaceshooter.infrastructure.utils;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourceLoader {
    private static final Logger logger = Logger.getLogger(ResourceLoader.class.getName());

    private ResourceLoader() { }

    public static InputStream open(String path) {
        if (path == null || !path.startsWith("/")) {
            throw new ResourceLoadException(String.valueOf(path), "expected absolute classpath path");
        }
        InputStream stream = ResourceLoader.class.getResourceAsStream(path);
        if (stream == null) throw new ResourceLoadException(path, "not found");
        return stream;
    }

    public static void warn(String path, Exception cause, String fallback) {
        ResourceLoadException failure = cause instanceof ResourceLoadException resourceFailure
                ? resourceFailure : new ResourceLoadException(path, cause);
        logger.log(Level.WARNING, failure.getMessage() + "; " + fallback, failure);
    }
}

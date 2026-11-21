package com.game.shooting2DGame.infrastructure.utils;

public class ResourceLoadException extends RuntimeException {
    public ResourceLoadException(String path, String reason) {
        super("Cannot load resource " + path + ": " + reason);
    }

    public ResourceLoadException(String path, Throwable cause) {
        super("Cannot load resource " + path, cause);
    }
}

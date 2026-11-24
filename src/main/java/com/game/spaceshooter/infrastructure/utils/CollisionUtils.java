package com.game.spaceshooter.infrastructure.utils;

import java.awt.Shape;
import java.awt.geom.Area;

public class CollisionUtils {
    private CollisionUtils() {}

    public static boolean intersects(Shape first, Shape second) {
        if (!first.getBounds().intersects(second.getBounds())) return false;
        Area overlap = new Area(first);
        overlap.intersect(new Area(second));
        return !overlap.isEmpty();
    }
}

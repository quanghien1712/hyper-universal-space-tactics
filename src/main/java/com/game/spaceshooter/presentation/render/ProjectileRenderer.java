package com.game.spaceshooter.presentation.render;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.domain.projectiles.ProjectileType;
import com.game.spaceshooter.infrastructure.utils.ImageLoader;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ProjectileRenderer {
    private Map<ProjectileType, BufferedImage> images = new HashMap<>();
    public ProjectileRenderer() {
        loadImage();
    }

    private void loadImage() {
        images.put(ProjectileType.PLASMA, ImageLoader.loadImage("/bullet/plasma.png"));
        images.put(ProjectileType.HOMING_BULLET,ImageLoader.loadImage("/bullet/homingbullet.png"));
        images.put(ProjectileType.NORMAL_BULLET,ImageLoader.loadImage("/bullet/normalbullet.png"));
    }


    public void render(Projectile projectile, Camera camera, Graphics2D g2D) {
        ProjectileType projectileType = projectile.getProjectileType();
        BufferedImage img = images.get(projectileType);
        int width = projectile.getWidth();
        int height = projectile.getHeight();
        Vector2D screenPos = camera.getScreenPos(projectile.getPosition());
        AffineTransform oldTransform = g2D.getTransform();
        double angle = projectile.getCurrentAngle();
        g2D.rotate(angle, screenPos.getX(), screenPos.getY());
        g2D.drawImage(img, (int)screenPos.getX() - width / 2, (int)screenPos.getY() - height / 2,
              width, height, null);
        g2D.setTransform(oldTransform);
    }

}

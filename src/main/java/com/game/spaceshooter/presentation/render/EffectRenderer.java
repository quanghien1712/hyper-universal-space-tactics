package com.game.spaceshooter.presentation.render;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.domain.effect.Effect;
import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.infrastructure.utils.ImageLoader;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class EffectRenderer {
    private Map<EffectType, BufferedImage> effects = new HashMap<>();

    public EffectRenderer() {
        loadAnimation();
    }

    private void loadAnimation() {
        effects.put(EffectType.BULLET_EXPLOSION, ImageLoader.loadImage("/effect/BulletExplosion.png"));
        effects.put(EffectType.PLASMA_EXPLOSION, ImageLoader.loadImage("/effect/PlasmaExplosion.png"));
        effects.put(EffectType.DEATH, ImageLoader.loadImage("/effect/Death.png"));
    }

    public void render(Effect effect, Graphics2D g2D, Camera camera) {
        BufferedImage effectSheet = effects.get(effect.getEffectType());
        int aniIndex = effect.getAniIndex();
        int frameSize = 64;
        int width = effect.getWidth();
        int height = effect.getHeight();
        BufferedImage effectImg = effectSheet.getSubimage(frameSize * aniIndex, 0, frameSize, frameSize);
        Vector2D screenPos = camera.getScreenPos(effect.getPosition());
        g2D.drawImage(effectImg, (int)(screenPos.getX() - width / 2.0) ,
              (int)(screenPos.getY() - height / 2.0), width, height, null);

    }
}

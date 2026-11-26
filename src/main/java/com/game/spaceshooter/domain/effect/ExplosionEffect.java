package com.game.spaceshooter.domain.effect;

public class ExplosionEffect extends Effect {
    private static final int WIDTH = 60;
    private static final int HEIGHT = 60;
    private static final int ANI_SPEED = 3;
    private static final int TOTAL_FRAME = 16;

    public ExplosionEffect() {
        super(0, 0, WIDTH, HEIGHT, ANI_SPEED, TOTAL_FRAME, EffectType.BULLET_EXPLOSION);
    }
}

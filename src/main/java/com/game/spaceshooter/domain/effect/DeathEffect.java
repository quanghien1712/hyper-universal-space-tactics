package com.game.spaceshooter.domain.effect;

public class DeathEffect extends Effect {
    private static final int WIDTH = 85;
    private static final int HEIGHT = 85;
    private static final int ANI_SPEED = 11;
    private static final int TOTAL_FRAME = 16;
    public DeathEffect() {
        super(0, 0, WIDTH, HEIGHT, ANI_SPEED, TOTAL_FRAME, EffectType.DEATH);
    }
}

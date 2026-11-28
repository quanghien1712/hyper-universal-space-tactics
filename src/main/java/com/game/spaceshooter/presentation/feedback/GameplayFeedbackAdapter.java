package com.game.spaceshooter.presentation.feedback;

import com.game.spaceshooter.application.port.AudioService;
import com.game.spaceshooter.application.port.EffectSpawner;
import com.game.spaceshooter.domain.effect.EffectType;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.infrastructure.sound.SoundConstants;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.util.Objects;

public final class GameplayFeedbackAdapter implements GameEventListener {
    private final AudioService audio;
    private final EffectSpawner effects;
    private final Runnable startBoostTrail;

    public GameplayFeedbackAdapter(AudioService audio, EffectSpawner effects,
                                   Runnable startBoostTrail) {
        this.audio = Objects.requireNonNull(audio, "audio is required");
        this.effects = Objects.requireNonNull(effects, "effects is required");
        this.startBoostTrail = Objects.requireNonNull(startBoostTrail, "startBoostTrail is required");
    }

    @Override
    public void onPlayerDamaged() {
        audio.playSfx(SoundConstants.SFX_ELECTRIC);
    }

    @Override
    public void onPlayerShot() {
        audio.playSfx(SoundConstants.SFX_PLASMA);
    }

    @Override
    public void onPlayerBoosted() {
        startBoostTrail.run();
        audio.playSfx(SoundConstants.SFX_BOOST);
    }

    @Override
    public void onEnemyDefeated(Vector2D position) {
        effects.spawn(EffectType.DEATH, position);
        audio.playSfx(SoundConstants.SFX_EXPLOSION);
    }

    @Override
    public void onProjectileHit(Projectile projectile) {
        EffectType type = switch (projectile.getProjectileType()) {
            case HOMING_BULLET, NORMAL_BULLET -> EffectType.BULLET_EXPLOSION;
            default -> EffectType.PLASMA_EXPLOSION;
        };
        Vector2D offset = projectile.getDirection().normalize().multiply(projectile.getHeight() / 2.0);
        effects.spawn(type, projectile.getPosition().add(offset));
    }
}

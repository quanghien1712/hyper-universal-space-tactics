package com.game.spaceshooter.application.core;

import com.game.spaceshooter.application.controller.PlayerController;
import com.game.spaceshooter.application.manager.EffectManager;
import com.game.spaceshooter.application.manager.EnemyManager;
import com.game.spaceshooter.application.manager.EnemyRoster;
import com.game.spaceshooter.application.manager.ProjectileManager;
import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.application.port.GameObjectPool;
import com.game.spaceshooter.domain.effect.Effect;
import com.game.spaceshooter.domain.entity.Enemy;
import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.infrastructure.config.GameConfig;

import java.util.List;
import java.util.Objects;

public class GameplaySession {
    private final Player player;
    private final PlayerController playerController;
    private final EnemyManager enemies;
    private final ProjectileManager projectiles;
    private final EffectManager effects;
    private final Camera view;

    public GameplaySession(GameObjectPool pool, Camera view,
                           GameEventListener feedback, EffectManager effects) {
        this.view = Objects.requireNonNull(view, "view is required");
        this.effects = Objects.requireNonNull(effects, "effects is required");
        player = new Player(GameConfig.PANEL_WIDTH / 2.0, GameConfig.PANEL_HEIGHT / 2.0, pool, feedback);
        EnemyRoster roster = new EnemyRoster();
        projectiles = new ProjectileManager(view, roster, player, feedback, pool);
        enemies = new EnemyManager(player, view, roster, projectiles, feedback, pool);
        playerController = new PlayerController(view, player, projectiles, feedback);
    }

    public synchronized void update(double deltaTime, double movementX, double movementY,
                       double aimX, double aimY, boolean shooting, boolean boostRequested) {
        playerController.update(movementX, movementY, aimX, aimY, shooting, boostRequested);
        player.update(deltaTime);
        view.followPlayer(player, deltaTime);
        enemies.update(deltaTime);
        projectiles.update(deltaTime);
        effects.update();
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies.snapshot();
    }

    public List<Projectile> getProjectiles() {
        return projectiles.snapshot();
    }

    public List<Effect> getEffects() {
        return effects.snapshot();
    }
}

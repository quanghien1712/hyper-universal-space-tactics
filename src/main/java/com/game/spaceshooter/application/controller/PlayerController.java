package com.game.spaceshooter.application.controller;

import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.application.port.ProjectileSpawner;
import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.domain.entity.PlayerState;
import com.game.spaceshooter.domain.port.GameEventListener;
import com.game.spaceshooter.domain.projectiles.Projectile;
import com.game.spaceshooter.infrastructure.config.PlayerConfig;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.util.Objects;

public class PlayerController {
    private final Player player;
    private final Camera camera;
    private final ProjectileSpawner projectiles;
    private final GameEventListener feedback;

    public PlayerController(Camera camera, Player player, ProjectileSpawner projectiles, GameEventListener feedback) {
        this.projectiles = Objects.requireNonNull(projectiles);
        this.feedback = Objects.requireNonNull(feedback);
        this.camera = camera;
        this.player = player;
    }

    public void update(double movementX, double movementY, double aimX, double aimY,
                       boolean shooting, boolean boostRequested) {
        player.setBoundPos(camera.getScreenPos(player.getPosition()));
        Vector2D screenPos = camera.getScreenPos(player.getPosition());
        Vector2D direction = new Vector2D(aimX - screenPos.getX(), aimY - screenPos.getY());
        double viewAngle = Math.atan2(direction.getY(), direction.getX()) + Math.PI / 2.0;
        player.setViewAngle(viewAngle);
        handleState(viewAngle, movementX, movementY, boostRequested);
        if (shooting) {
            Projectile projectile = player.shoot(direction);
            if (projectile != null) {
                feedback.onPlayerShot();
                projectile.setBoundPos(camera.getScreenPos(projectile.getPosition()));
                projectiles.addActiveProjectile(projectile);
            }
        }

    }

    private void handleState(double viewAngle, double movementX, double movementY, boolean boostRequested) {
        boolean moving = movementX != 0 || movementY != 0;
        if (player.getPlayerState() == PlayerState.BOOSTING) {
            player.setTargetAngle(viewAngle);
            return;
        }
        if (player.getPlayerState() == PlayerState.BOOST_DECAY) {
            if (moving) {
                double targetAngle = Math.atan2(movementY, movementX) + Math.PI / 2.0;
                player.setTargetAngle(targetAngle);
            }
            return;
        }
        if (boostRequested && player.shouldBoost()) {
            player.setTargetAngle(viewAngle);
            player.startBoost();
            feedback.onPlayerBoosted();
        } else {
            player.setState(PlayerState.NORMAL);
            if (moving) {
                player.setDirection(movementX, movementY);
                player.accelerate(PlayerConfig.ACCELERATION);
            } else player.dragged(PlayerConfig.DRAG);
        }
    }

}

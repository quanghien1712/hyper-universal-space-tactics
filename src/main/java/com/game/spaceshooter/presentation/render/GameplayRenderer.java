package com.game.spaceshooter.presentation.render;

import com.game.spaceshooter.application.core.GameplaySession;
import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.domain.entity.Player;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.config.PlayerConfig;
import com.game.spaceshooter.presentation.ui.HUD;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;

public class GameplayRenderer {
    private final Camera camera;
    private int displayScore;
    private double trailTimer;
    private final PlayerRenderer playerRenderer = new PlayerRenderer();
    private final EnemyRenderer enemyRenderer = new EnemyRenderer();
    private final ProjectileRenderer projectileRenderer = new ProjectileRenderer();
    private final EffectRenderer effectRenderer = new EffectRenderer();
    private final PlayingBackground background = new PlayingBackground();
    private final SpaceWarpTransition transition = new SpaceWarpTransition();
    private final HUD hud = new HUD();

    public GameplayRenderer(Camera camera) {
        this.camera = camera;
        transition.start();
    }

    public boolean isTransitionActive() {
        return transition.isActive();
    }

    public void updateTransition(double deltaTime) {
        transition.update(deltaTime);
    }

    public void update(Player player, double deltaTime, int scoreBeforeUpdate) {
        synchronized (this) {
            if (displayScore < scoreBeforeUpdate) {
                displayScore += Math.max(1, (scoreBeforeUpdate - displayScore) / 10);
            }
            trailTimer = Math.max(0, trailTimer - deltaTime);
        }
        playerRenderer.update(player, shouldRenderTrail());
    }

    public synchronized void startBoostTrail() {
        trailTimer = PlayerConfig.TRAIL_DURATION;
    }
    public synchronized int getDisplayScore() {
        return displayScore;
    }
    public synchronized boolean shouldRenderTrail() {
        return trailTimer > 0;
    }
    public synchronized double getTrailProgress() {
        return trailTimer / PlayerConfig.TRAIL_DURATION;
    }

    public void render(GameplaySession session, Graphics2D g2D, boolean paused) {
        synchronized (session) {
            if (!transition.isActive()) {
                renderGameplay(session, g2D, paused);
                return;
            }
            if (transition.shouldRevealGameplay()) renderGameplay(session, g2D, paused);
            else {
                g2D.setColor(Color.BLACK);
                g2D.fillRect(0, 0, GameConfig.PANEL_WIDTH, GameConfig.PANEL_HEIGHT);
            }
            transition.render(g2D);
            hud.renderBorder(g2D);
            float alpha = transition.getHudFlashOverlayAlpha();
            if (alpha > 0f) {
                Composite original = g2D.getComposite();
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2D.setColor(Color.WHITE);
                g2D.fillRect(0, 0, GameConfig.PANEL_WIDTH, GameConfig.PANEL_HEIGHT);
                g2D.setComposite(original);
            }
        }
    }

    private void renderGameplay(GameplaySession session, Graphics2D graphics, boolean paused) {
        Player player = session.getPlayer();
        background.render(graphics, camera);
        playerRenderer.render(player, camera, graphics, getTrailProgress(), paused);
        session.getEnemies().forEach(enemy -> enemyRenderer.render(enemy, camera, graphics));
        session.getProjectiles().forEach(projectile -> projectileRenderer.render(projectile, camera, graphics));
        session.getEffects().forEach(effect -> effectRenderer.render(effect, graphics, camera));
        hud.render(graphics, player.getHealth(), getDisplayScore(),
              player.getBoostCooldown(), player.getBoostCooldownDuration());
    }
}

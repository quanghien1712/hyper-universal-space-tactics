package com.game.spaceshooter.presentation.ui;

import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.config.PlayerConfig;
import com.game.spaceshooter.infrastructure.config.UIConfig;
import com.game.spaceshooter.infrastructure.utils.FontLoader;
import com.game.spaceshooter.infrastructure.utils.ImageLoader;
import com.game.spaceshooter.infrastructure.utils.Vector2D;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class HUD {
    private BufferedImage border;
    private BufferedImage[] healthBar, progressBar;

    private Font ethnocentricFont;
    private static final Vector2D HEALTH_BAR_POS = new Vector2D(70, GameConfig.PANEL_HEIGHT / 1.6);
    private static final Vector2D BOOST_BAR_POS = new Vector2D(1060, GameConfig.PANEL_HEIGHT / 1.07);
    public HUD() {
        border = ImageLoader.loadImage("/hud/border.png") ;
        healthBar = new BufferedImage[2];
        progressBar = new BufferedImage[2];
        for (int i = 0; i < healthBar.length; ++i) {
            healthBar[i] = ImageLoader.loadImage("/hud/HealthBar" + i + ".png");
        }
        for (int i = 0; i < progressBar.length; ++i) {
            progressBar[i] = ImageLoader.loadImage("/hud/ProgressBar" + i + ".png");
        }
        initFont();
    }

    private void initFont() {
        ethnocentricFont = FontLoader.loadEthnocentricOrFallback(30f, "Arial", Font.BOLD);
    }

    public void render(Graphics2D g2D, int playerHealth, int playerScore,
                       double boostCooldown, double boostCooldownDuration) {
        renderBorder(g2D);
        renderHealthBar(g2D, playerHealth);
        renderBoostBar(g2D, boostCooldown, boostCooldownDuration);
        renderScore(g2D, playerScore);
    }

    private void renderHealthBar(Graphics2D g2D, int playerHealth) {
        AlphaComposite oldComposite = (AlphaComposite) g2D.getComposite();
        double angle = 0.58;
        AffineTransform oldTransform = g2D.getTransform();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g2D.rotate(angle, HEALTH_BAR_POS.getX(), HEALTH_BAR_POS.getY());
        g2D.drawImage(healthBar[0], (int)HEALTH_BAR_POS.getX(), (int)HEALTH_BAR_POS.getY(),
              UIConfig.PROGRESS_BAR_WIDTH, UIConfig.PROGRESS_BAR_HEIGHT, null);
        double ratio = Math.max(0, 1.0 * playerHealth / PlayerConfig.MAX_HEALTH);
        int srcWidth = (int) (healthBar[1].getWidth() * ratio);
        int drawWidth = (int) (UIConfig.PROGRESS_BAR_WIDTH * ratio);
        if (srcWidth > 0 && drawWidth > 0) {
            BufferedImage subImg = healthBar[1].getSubimage(0, 0, srcWidth, healthBar[0].getHeight());
            g2D.drawImage(subImg, (int)HEALTH_BAR_POS.getX(), (int)HEALTH_BAR_POS.getY(),
                  drawWidth, UIConfig.PROGRESS_BAR_HEIGHT, null);
        }
        g2D.setComposite(oldComposite);
        g2D.setTransform(oldTransform);

        int x = (int)HEALTH_BAR_POS.getX() - 40;
        int y = (int)HEALTH_BAR_POS.getY() + 160;
        oldTransform = g2D.getTransform();
        double theta = Math.PI / 9.5;
        g2D.rotate(theta, x, y);
        g2D.setColor(Color.WHITE);
        g2D.setFont(ethnocentricFont.deriveFont(22f));
        g2D.drawString("HEALTH", x, y);
        g2D.setTransform(oldTransform);

    }

    private void renderBoostBar(Graphics2D g2D, double cooldown, double cooldownDuration) {
        AlphaComposite oldComposite = (AlphaComposite) g2D.getComposite();
        double angle = -0.58;
        AffineTransform oldTransform = g2D.getTransform();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g2D.rotate(angle, BOOST_BAR_POS.getX(), BOOST_BAR_POS.getY());
        g2D.drawImage(progressBar[1], (int)BOOST_BAR_POS.getX(), (int)BOOST_BAR_POS.getY(),
              UIConfig.PROGRESS_BAR_WIDTH, UIConfig.PROGRESS_BAR_HEIGHT, null);
        double ratio = cooldownDuration > 0 ? 1.0 - cooldown / cooldownDuration : 1.0;
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        int srcWidth = (int) (progressBar[0].getWidth() * ratio);
        int drawWidth = (int) (UIConfig.PROGRESS_BAR_WIDTH * ratio);
        if (srcWidth > 0 && drawWidth > 0) {
            int srcX = progressBar[0].getWidth() - srcWidth;
            BufferedImage subImg = progressBar[0].getSubimage(srcX, 0, srcWidth, progressBar[0].getHeight());
            int destX = (int) BOOST_BAR_POS.getX() + UIConfig.PROGRESS_BAR_WIDTH - drawWidth;
            g2D.drawImage(subImg, destX, (int)BOOST_BAR_POS.getY(),
                  drawWidth, UIConfig.PROGRESS_BAR_HEIGHT, null);
        }
        g2D.setComposite(oldComposite);
        g2D.setTransform(oldTransform);

        int x = (int)BOOST_BAR_POS.getX() + 282;
        int y = (int)BOOST_BAR_POS.getY() - 35;
        oldTransform = g2D.getTransform();
        double theta = -Math.PI / 9.5;
        g2D.rotate(theta, x, y);
        g2D.setColor(Color.WHITE);
        g2D.setFont(ethnocentricFont.deriveFont(21f));
        g2D.drawString("BOOST", x, y);
        g2D.setTransform(oldTransform);
    }

    public void renderBorder(Graphics2D g2D) {
        AlphaComposite oldComposite = (AlphaComposite) g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2D.drawImage(border, 0, 0, GameConfig.PANEL_WIDTH, GameConfig.PANEL_HEIGHT, null);
        g2D.setComposite(oldComposite);
    }

    private void renderScore(Graphics2D g2D, int score) {
        AlphaComposite oldComposite = (AlphaComposite) g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        int x = (int)HEALTH_BAR_POS.getX() - 40;
        int y = (int)HEALTH_BAR_POS.getY() + 60;
        double angle = Math.PI / 9.5;
        AffineTransform oldTransform = g2D.getTransform();
        g2D.rotate(angle, x, y);
        g2D.setColor(Color.WHITE);
        g2D.setFont(ethnocentricFont);
        g2D.drawString("Score " + score, x, y);
        g2D.setComposite(oldComposite);
        g2D.setTransform(oldTransform);
    }

}

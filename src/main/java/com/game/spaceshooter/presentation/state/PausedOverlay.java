package com.game.spaceshooter.presentation.state;

import com.game.spaceshooter.application.core.GameEngine;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.config.UIConfig;
import com.game.spaceshooter.infrastructure.utils.FontLoader;
import com.game.spaceshooter.infrastructure.utils.ImageLoader;
import com.game.spaceshooter.presentation.controller.GameFlowController;
import com.game.spaceshooter.presentation.input.MouseInputs;
import com.game.spaceshooter.presentation.ui.Button;
import com.game.spaceshooter.presentation.ui.Slider;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PausedOverlay implements State {
    private int bgPosX, bgPosY;
    private final GameFlowController actions;
    private BufferedImage background;
    private Font ethnocentricFont;
    private Button mainMenu;
    private Button replay;
    private Button continuePlay;
    private Slider musicSlider;
    private Slider sfxSlider;
    private final MouseInputs mouseInputs;

    public PausedOverlay(GameEngine gameEngine, GameFlowController actions) {
        this.mouseInputs = gameEngine.getGamePanel().getMouseInputs();
        this.actions = actions;
        initBackground();
        initButton();
        initFont();
    }

    private void initButton() {
        int buttonWidth = UIConfig.PAUSE_BUTTON_WIDTH;
        int buttonHeight = UIConfig.PAUSE_BUTTON_HEIGHT;
        int buttonY = bgPosY + 450;
        int spacing = 90;
        int totalWidth = (buttonWidth * 3) + (spacing * 2);
        int startX = bgPosX + (UIConfig.PAUSE_OVERLAY_WIDTH - totalWidth) / 2;
        mainMenu = new Button(startX, buttonY, buttonWidth, buttonHeight, "Menu");
        replay = new Button(startX + buttonWidth + spacing, buttonY, buttonWidth, buttonHeight, "Reset");
        continuePlay = new Button (startX + 2 * (buttonWidth + spacing), buttonY, buttonWidth, buttonHeight, "Continue");
        musicSlider = new Slider(bgPosX + 60, bgPosY + 165, UIConfig.SLIDER_WIDTH, UIConfig.SLIDER_HEIGHT);
        sfxSlider = new Slider(bgPosX + 60, bgPosY + 335, UIConfig.SLIDER_WIDTH, UIConfig.SLIDER_HEIGHT);
    }

    private void initBackground() {
        background = ImageLoader.loadImage("/background/PauseBackground.png");
        bgPosX = (GameConfig.PANEL_WIDTH - UIConfig.PAUSE_OVERLAY_WIDTH) / 2;
        bgPosY = (GameConfig.PANEL_HEIGHT - UIConfig.PAUSE_OVERLAY_HEIGHT) / 2;
    }

    private void initFont() {
        ethnocentricFont = FontLoader.loadEthnocentricOrFallback(45f, "Arial", Font.BOLD);
    }


    @Override
    public void update(double deltaTime) {
        mainMenu.update(mouseInputs);
        replay.update(mouseInputs);
        continuePlay.update(mouseInputs);
        sfxSlider.update(mouseInputs);
        musicSlider.update(mouseInputs);
        applySliderVolumes();
        handleState();
    }

    private void applySliderVolumes() {
        actions.applyVolumes(sfxSlider.getFilledPercent(), musicSlider.getFilledPercent());
    }

    private void handleState() {
        if (mainMenu.isClicked()) {
            actions.returnToMenu();
            mainMenu.resetButton();
        }
        if (replay.isClicked()) {
            actions.restartGame();
            replay.resetButton();
        }
        if (continuePlay.isClicked()) {
            applySliderVolumes();
            actions.resumeGame();
            continuePlay.resetButton();
        }
    }

    @Override
    public void render(Graphics2D g2D) {
        //anti-alias for text
        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
              RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.drawImage(background, bgPosX, bgPosY, UIConfig.PAUSE_OVERLAY_WIDTH, UIConfig.PAUSE_OVERLAY_HEIGHT, null);
        drawText(g2D);
        mainMenu.render(g2D);
        replay.render(g2D);
        continuePlay.render(g2D);
        musicSlider.render(g2D);
        sfxSlider.render(g2D);
    }

    private void drawText(Graphics2D g2D) {
        g2D.setColor(Color.WHITE);
        g2D.setFont(ethnocentricFont);
        g2D.drawString("PAUSED", bgPosX + 175, bgPosY + 55);
        g2D.setFont(ethnocentricFont.deriveFont(40f));
        g2D.drawString("MUSIC", bgPosX + 60, bgPosY + 135);
        g2D.drawString("SFX", bgPosX + 60, bgPosY + 303);
    }

    @Override
    public boolean rendersOnTopOfGameplay() {
        return true;
    }

}

package com.game.spaceshooter.presentation.state;

import com.game.spaceshooter.application.core.GameEngine;
import com.game.spaceshooter.infrastructure.config.GameConfig;
import com.game.spaceshooter.infrastructure.config.UIConfig;
import com.game.spaceshooter.infrastructure.utils.ImageLoader;
import com.game.spaceshooter.presentation.controller.GameFlowController;
import com.game.spaceshooter.presentation.input.MouseInputs;
import com.game.spaceshooter.presentation.ui.Button;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Menu implements State {
    private final GameFlowController actions;
    private final MouseInputs mouseInputs;

    private Button playButton;
    private Button infoButton;
    private Button exitButton;
    private final BufferedImage background;

    private static final Logger LOGGER = Logger.getLogger(Menu.class.getName());


    public Menu(GameEngine gameEngine, GameFlowController actions) {
        actions.playMenuMusic();
        this.actions = actions;
        mouseInputs = gameEngine.getGamePanel().getMouseInputs();
        background = ImageLoader.loadImage("/background/MenuBackground.png");
        initButton();
    }

    private void initButton() {
        int buttonWidth = UIConfig.MENU_BUTTON_WIDTH;
        int buttonHeight = UIConfig.MENU_BUTTON_HEIGHT;
        int centerX = (GameConfig.PANEL_WIDTH - buttonWidth) / 2;
        int spacing = 20;
        int totalHeight = (buttonHeight * 3) + (spacing * 2);
        int startY = (GameConfig.PANEL_HEIGHT - totalHeight) / 2 + 200;

        playButton = new Button(centerX, startY, buttonWidth, buttonHeight, "Start");
        infoButton = new Button(centerX, startY + buttonHeight + spacing, buttonWidth, buttonHeight, "Info");
        exitButton = new Button(centerX, startY + (buttonHeight + spacing) * 2, buttonWidth, buttonHeight, "Exit");
    }

    @Override
    public void update(double deltaTime) {
        playButton.update(mouseInputs);
        infoButton.update(mouseInputs);
        exitButton.update(mouseInputs);
        if (playButton.isClicked()) {
            actions.startNewGame();
            playButton.resetButton();
        }
        if (exitButton.isClicked()) {
            actions.exitGame();
        }
        if (infoButton.isClicked()) {
            openWebpage(UIConfig.MENU_LINK_URL);
            infoButton.resetButton();
        }
    }

    private void openWebpage(String url) {
        try {
            if (Desktop.isDesktopSupported()
                  && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.WARNING, "Failed to open link: " + url, e);
        }
    }

    @Override
    public void render(Graphics2D g2D) {
        g2D.drawImage(background, 0, 0, GameConfig.PANEL_WIDTH, GameConfig.PANEL_HEIGHT, null);
        playButton.render(g2D);
        infoButton.render(g2D);
        exitButton.render(g2D);
    }
}

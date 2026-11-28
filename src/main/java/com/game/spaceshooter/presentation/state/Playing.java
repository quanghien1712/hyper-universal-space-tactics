package com.game.spaceshooter.presentation.state;

import com.game.spaceshooter.application.core.GameEngine;
import com.game.spaceshooter.application.core.GameplaySession;
import com.game.spaceshooter.application.manager.EffectManager;
import com.game.spaceshooter.application.model.Camera;
import com.game.spaceshooter.application.port.AudioService;
import com.game.spaceshooter.infrastructure.pool.PoolManager;
import com.game.spaceshooter.infrastructure.utils.Vector2D;
import com.game.spaceshooter.presentation.controller.GameFlowController;
import com.game.spaceshooter.presentation.feedback.GameplayFeedbackAdapter;
import com.game.spaceshooter.presentation.input.KeyBoardInputs;
import com.game.spaceshooter.presentation.input.MouseInputs;
import com.game.spaceshooter.presentation.render.GameplayRenderer;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class Playing implements State {
    private final StateManager states;
    private final GameFlowController actions;
    private final KeyBoardInputs keyboard;
    private final MouseInputs mouse;
    private final GameplaySession session;
    private final GameplayRenderer renderer;
    private final Vector2D mousePosition = new Vector2D(0, 0);

    public Playing(GameEngine engine, StateManager states, GameFlowController actions, AudioService audio) {
        this.states = states;
        this.actions = actions;
        keyboard = engine.getGamePanel().getKeyboardInputs();
        mouse = engine.getGamePanel().getMouseInputs();
        PoolManager pool = new PoolManager();
        Camera camera = new Camera();
        EffectManager effects = new EffectManager(pool);
        renderer = new GameplayRenderer(camera);
        GameplayFeedbackAdapter feedback = new GameplayFeedbackAdapter(audio, effects, renderer::startBoostTrail);
        session = new GameplaySession(pool, camera, feedback, effects);
    }

    @Override
    public void update(double deltaTime) {
        synchronized (session) {
            if (renderer.isTransitionActive()) {
                renderer.updateTransition(deltaTime);
                return;
            }
            int scoreBeforeUpdate = session.getPlayer().getScore();
            updateGameplay(deltaTime);
            renderer.update(session.getPlayer(), deltaTime, scoreBeforeUpdate);
        }
        if (keyboard.isKeyPressed(KeyEvent.VK_ESCAPE)) actions.pauseGame();
        if (session.getPlayer().isDead()) actions.showGameOver(renderer.getDisplayScore());
    }

    private void updateGameplay(double deltaTime) {
        int x = (keyboard.isKeyPressed(KeyEvent.VK_D) ? 1 : 0) - (keyboard.isKeyPressed(KeyEvent.VK_A) ? 1 : 0);
        int y = (keyboard.isKeyPressed(KeyEvent.VK_S) ? 1 : 0) - (keyboard.isKeyPressed(KeyEvent.VK_W) ? 1 : 0);
        mouse.copyMousePosTo(mousePosition);
        session.update(deltaTime, x, y, mousePosition.getX(), mousePosition.getY(),
              mouse.isMousePressed(MouseEvent.BUTTON1), keyboard.isKeyPressed(KeyEvent.VK_SHIFT));
    }

    @Override
    public void render(Graphics2D graphics) {
        renderer.render(session, graphics, states.haveOverlay());
    }
}

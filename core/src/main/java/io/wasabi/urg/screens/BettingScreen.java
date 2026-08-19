package io.wasabi.urg.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.betting.BetScreenButton;
import io.wasabi.urg.elements.betting.ChipDragController;
import io.wasabi.urg.elements.game.BettingTable;
import io.wasabi.urg.managers.RendererManager;

public class BettingScreen implements Screen {
    private final Roulette game;

    // Renderers
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;

    // Elements
    private BettingTable bettingTable;
    private Texture betButtonTexture;
    private BetScreenButton betButton;
    private float baseWindowWidth;
    private float baseWindowHeight;
    private float baseButtonWidth;
    private float baseButtonHeight;
    private ChipDragController dragController;

    public BettingScreen(final Roulette game) {
        this.game = game;
        this.shapeRenderer = RendererManager.getInstance().getShapeRenderer();
        this.spriteBatch = RendererManager.getInstance().getSpriteBatch();

        this.bettingTable = new BettingTable();

        // Absolute value for now. Consider making this relative to table size and world
        // coords.
        this.bettingTable.setPosition(-640f, -48f);

        this.dragController = new ChipDragController(bettingTable, game.getCamera());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);

        // ShapeRenderer renders
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        bettingTable.update(delta);
        bettingTable.render();

        // SpriteBatch renders
        updateBetButtonLayout();
        betButton.update();
        betButton.draw(spriteBatch);

    }

    @Override
    public void resize(int width, int height) {
        updateBetButtonLayout();
    }

    @Override
    public void show() {

        Gdx.input.setInputProcessor(dragController);

        betButtonTexture = new Texture(Gdx.files.internal("buttons/TEX_BUTTON_64x32_BetDown.png"));

        float btnWidth = betButtonTexture.getWidth();
        float btnHeight = betButtonTexture.getHeight();
        baseWindowWidth = game.getWorldWidth();
        baseWindowHeight = game.getWorldHeight();
        baseButtonWidth = btnWidth;
        baseButtonHeight = btnHeight;

        betButton = new BetScreenButton(
                betButtonTexture,
                (game.getWorldWidth() - btnWidth) / 2f, game.getWorldHeight() - btnHeight,
                btnWidth, btnHeight,
                () -> {
                    // DO NOT CALL this.dispose() HERE, SOME ASSETS ARE STILL IN USE (e.g., the
                    // sprite batch)
                    game.setScreen(Roulette.getInstance().getGameScreen());
                });

        updateBetButtonLayout();
    }

    private void updateBetButtonLayout() {
        if (betButton == null || betButtonTexture == null) {
            return;
        }

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float widthScale = screenWidth / Math.max(1f, baseWindowWidth) * 4f;
        float heightScale = screenHeight / Math.max(1f, baseWindowHeight) * 4f;
        float scale = Math.max(0.5f, Math.min(2.5f, Math.min(widthScale, heightScale)));

        float btnWidth = baseButtonWidth * scale;
        float btnHeight = baseButtonHeight * scale;

        betButton.setSize(btnWidth, btnHeight);
        betButton.setPosition((screenWidth - btnWidth) / 2f, screenHeight - btnHeight);
    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        bettingTable.dispose();
        betButtonTexture.dispose();
    }
}

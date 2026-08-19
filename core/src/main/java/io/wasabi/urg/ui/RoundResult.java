package io.wasabi.urg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.util.tweens.Tween;

public class RoundResult {

    private static final float WIDTH = 760f;
    private static final float HEIGHT = 650f;

    private static final float CENTER_X = -WIDTH/2f;
    private static final float CENTER_Y = 0f;
    private static final float OFFSCREEN_Y = -1500f;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;

    private final Rectangle cashOutButton = new Rectangle();

    private Tween tween;

    private float y = OFFSCREEN_Y;

    private int quota;
    private int chips;
    private int baseReward;
    private int unusedSpinBonus;
    private int totalReward;

    private boolean visible;

    public RoundResult(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch) {
        this.shapeRenderer = shapeRenderer;
        this.spriteBatch = spriteBatch;
    }

    public void show(int quota, int chips, int baseReward, int unusedSpinBonus, int totalReward) {
        this.quota = quota;
        this.chips = chips;
        this.baseReward = baseReward;
        this.unusedSpinBonus = unusedSpinBonus;
        this.totalReward = totalReward;

        visible = true;

        y = OFFSCREEN_Y;

        tween = new Tween(1f, OFFSCREEN_Y, CENTER_Y, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
    }

    public void hide() {
        tween = new Tween(1f, y, OFFSCREEN_Y, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
    }

    public boolean isHidden() {
        return !visible || y <= OFFSCREEN_Y + 1f;
    }

    public void update(float delta) {
        if (!visible) {
            return;
        }

        if (tween != null && !tween.isComplete()) {
            y = tween.update(delta);
        }
    }

    public void render() {
        if (!visible) {
            return;
        }

        float left = CENTER_X;
        float bottom = y - HEIGHT / 2f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Main board
        shapeRenderer.setColor(0.12f, 0.12f, 0.12f, 1f);
        shapeRenderer.rect(left, bottom-300f, WIDTH, HEIGHT+300f);

        // Header
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(left, bottom + HEIGHT - 90f, WIDTH, 90f
        );

        // Cash out button
        float buttonWidth = 300f;
        float buttonHeight = 90f;

        float buttonX =
            left + (WIDTH - buttonWidth) / 2f;

        float buttonY =
            bottom + 50f;

        cashOutButton.set(buttonX, buttonY, buttonWidth, buttonHeight);

        shapeRenderer.setColor(0.25f, 0.6f, 0.3f, 1f);

        shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);

        shapeRenderer.end();

        // Text
        spriteBatch.begin();
        spriteBatch.setTransformMatrix(new com.badlogic.gdx.math.Matrix4().setToTranslation(0, 0, 0));

        BitmapFont font = FontManager.getInstance().getFontByName("Placeholder");

        font.draw(spriteBatch, "ROUND COMPLETE", left + 250f, bottom + HEIGHT - 30f);

        font.draw(spriteBatch, "QUOTA", left + 80f, bottom + HEIGHT - 150f);

        font.draw(spriteBatch, chips + " / " + quota, left + 400f, bottom + HEIGHT - 150f);

        font.draw(spriteBatch, "ROUND REWARD", left + 80f, bottom + HEIGHT - 240f);

        font.draw(spriteBatch, "+" + baseReward, left + 400f, bottom + HEIGHT - 240f);

        font.draw(spriteBatch, "UNUSED SPIN BONUS", left + 80f, bottom + HEIGHT - 330f);

        font.draw(spriteBatch, "+" + unusedSpinBonus, left + 400f, bottom + HEIGHT - 330f);

        font.draw(spriteBatch, "TOTAL", left + 80f, bottom + HEIGHT - 420f);

        font.draw(spriteBatch, "+" + totalReward, left + 400f, bottom + HEIGHT - 420f);

        font.draw(spriteBatch, "CASH OUT", buttonX + 90f, buttonY + 55f);

        spriteBatch.end();
    }

    public boolean handleInput() {
        if (!visible || !Gdx.input.justTouched()) {
            return false;
        }

        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);

        Roulette.getInstance().getCamera().unproject(touch);

        return cashOutButton.contains(touch.x, touch.y);
    }

    public float getY() {
        return y;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}

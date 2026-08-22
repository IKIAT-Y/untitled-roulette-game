package io.wasabi.urg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

import io.wasabi.urg.elements.betting.Bet;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RoundManager;
import io.wasabi.urg.state.RunState;
import io.wasabi.urg.util.tweens.Tween;

/** Displays the current round quota and animates the vertical quota progress bar. */
public final class QuotaTracker {
    private static float BAR_X = 22f;
    private static final float BAR_Y = 62f;
    private static final float BAR_WIDTH = 40f;
    private static final float BAR_HEIGHT_PADDING = 124f;
    private static final float BAR_BORDER = 12f;
    private static final float TEXT_X_OFFSET = 35f;
    private static final float TEXT_TOP_PADDING = 67f;
    private static final float TEXT_SCALE = 1.25f;

    private static final float PROGRESS_ANIMATION_SPEED = 4f;

    private static final float BLUE_R = 0.20f;
    private static final float BLUE_G = 0.60f;
    private static final float BLUE_B = 0.85f;
    private static final float OFFSCREEN_X = -778f;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final RunState runState;
    private final RoundManager roundManager;

    private final Matrix4 uiProjection = new Matrix4();
    private final Matrix4 previousShapeProjection = new Matrix4();
    private final Matrix4 previousSpriteProjection = new Matrix4();
    private final Matrix4 previousSpriteTransform = new Matrix4();
    private final Matrix4 identityTransform = new Matrix4().idt();

    private float displayedProgress;
    private float targetProgress;
    private int lastQuota = -1;
    private boolean spinStarted = false;

    private Tween tween;

    public QuotaTracker(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch, RunState runState, RoundManager roundManager) {
        this.shapeRenderer = shapeRenderer;
        this.spriteBatch = spriteBatch;
        this.runState = runState;
        this.roundManager = roundManager;
    }

    /** Advances the bar towards the player's current quota progress. */
    public void update(float delta) {

        int quota = roundManager.getCurrentConfig().getQuota();
        int chips = getAvailableChips();

        if (spinStarted && runState.getActiveBets().isEmpty()) {
        spinStarted = false;
    }

        targetProgress = quota <= 0
                ? 0f
                : MathUtils.clamp((float) chips / quota, 0f, 1f);

        if (quota != lastQuota) {
            lastQuota = quota;
            displayedProgress = 0f;
        }

        float animationAmount =
                MathUtils.clamp(delta * PROGRESS_ANIMATION_SPEED, 0f, 1f);

        displayedProgress =
                MathUtils.lerp(displayedProgress, targetProgress, animationAmount);

        // Avoid leaving a tiny gap at the very top when the quota has been reached.
        if (targetProgress >= 1f && displayedProgress > 0.999f) {
            displayedProgress = 1f;
        }

        if (tween != null && !tween.isComplete()) {
            BAR_X = tween.update(delta);
        }
    }

    public void onSpinStarted() {
    spinStarted = true;
    }

   private int getAvailableChips() {
    int chips = runState.getChips();

    if (spinStarted) {
        for (Bet bet : runState.getActiveBets()) {
            chips -= bet.getAmount();

        }
    }

    return Math.max(0, chips);
    }


    public void render() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        uiProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);

        previousShapeProjection.set(shapeRenderer.getProjectionMatrix());
        previousSpriteProjection.set(spriteBatch.getProjectionMatrix());
        previousSpriteTransform.set(spriteBatch.getTransformMatrix());

        shapeRenderer.setProjectionMatrix(uiProjection);
        spriteBatch.setProjectionMatrix(uiProjection);
        spriteBatch.setTransformMatrix(identityTransform);

        renderProgressBar(screenHeight);
        renderQuotaText(screenWidth, screenHeight);

        shapeRenderer.setProjectionMatrix(previousShapeProjection);
        spriteBatch.setProjectionMatrix(previousSpriteProjection);
        spriteBatch.setTransformMatrix(previousSpriteTransform);

    }

    private void renderProgressBar(float screenHeight) {
        float barHeight = Math.max(100f, screenHeight - BAR_HEIGHT_PADDING);
        float fillHeight = barHeight * displayedProgress;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background keeps the bar visible while it is filling.
        shapeRenderer.setColor(0.18f, 0.18f, 0.18f, 1f);
        shapeRenderer.rect(BAR_X, BAR_Y, BAR_WIDTH, barHeight);

        // Blue fill grows from the bottom towards the quota.
        shapeRenderer.setColor(0.20f, 0.60f, 0.85f, 1f);
        shapeRenderer.rect(
                BAR_X + BAR_BORDER,
                BAR_Y + BAR_BORDER,
                BAR_WIDTH - BAR_BORDER * 2f,
                Math.max(0f, fillHeight - BAR_BORDER * 2f));

        shapeRenderer.end();
    }

    private void renderQuotaText(float screenWidth, float screenHeight) {
        int chips = getAvailableChips();
        int quota = roundManager.getCurrentConfig().getQuota();

        int percentage = quota <= 0 ? 0 : Math.round((float) chips / quota * 100f);

        // Keep the text beside the bar rather than over it.
        float textX = BAR_X + BAR_WIDTH + TEXT_X_OFFSET;
        float textY = screenHeight - TEXT_TOP_PADDING;

        BitmapFont font = FontManager.getInstance().getFontByName("Placeholder");

        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;

        font.getData().setScale(TEXT_SCALE);

        spriteBatch.begin();

        // Heading.
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(spriteBatch, "QUOTA", textX, textY);

        // Current money / target quota.
        font.draw(spriteBatch, "$" + chips + " / $" + quota, textX, textY - 38f);

        // Percentage uses exactly the same blue as the progress bar.
        font.setColor(BLUE_R, BLUE_G, BLUE_B, 1f);
        font.draw(spriteBatch, percentage + "%", textX, textY - 76f);

        spriteBatch.end();

        // Restore the shared font so other UI elements are unaffected.
        font.getData().setScale(oldScaleX, oldScaleY);
        font.setColor(1f, 1f, 1f, 1f);
    }

    public void hide() { tween = new Tween(1f, BAR_X, OFFSCREEN_X, Tween.TweenStyle.QUAD, Tween.TweenDirection.IN); }
    public void show() { tween = new Tween(0.7f, BAR_X, 22f, Tween.TweenStyle.CIRCULAR, Tween.TweenDirection.OUT); }
}

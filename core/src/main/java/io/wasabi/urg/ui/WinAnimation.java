package io.wasabi.urg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import io.wasabi.urg.elements.betting.WinBreakdown;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.util.tweens.Tween;

/**
 * Plays the "you won" reveal near the top of the screen as a single running
 * total: it starts at the bet amount, then each stage (flat bonus, tile
 * multiplier, global multiplier) counts the number up/across to its new
 * value while showing what was just applied off to the right, color-coded
 * by stage. Once every stage has landed it holds on the final total, then
 * flies it into the quota tracker and fires {@code onImpact} so the caller
 * can actually add it to the quota at the moment it lands.
 *
 * One instance is reused across wins; call {@link #start} to (re)play it.
 * {@link #update(float)} / {@link #render()} are no-ops while inactive, so
 * it's safe to call them unconditionally from the screen's main loop.
 */
public class WinAnimation {
    private enum Phase {
        BET, STEPPING, FLYING, DONE
    }

    private static final class Step {
        final String appliedLabel;
        final Color highlightColor;
        final float newValue;

        Step(String appliedLabel, Color highlightColor, float newValue) {
            this.appliedLabel = appliedLabel;
            this.highlightColor = highlightColor;
            this.newValue = newValue;
        }
    }

    // Tuned by eye, same spirit as BettingTable's layout constants.
    private static final float STEP_DURATION = 0.55f;
    private static final float FLY_DURATION = 0.5f;

    private static final float POP_DURATION = 0.3f;
    private static final float MAIN_POP_SCALE = 1.15f;
    private static final float APPLIED_POP_SCALE = 1.4f;
    private static final float FLY_START_SCALE = 1.2f;
    private static final float FLY_END_SCALE = 0.5f;
    private static final float FLY_ARC_HEIGHT = 60f;

    private static final float BASE_FONT_SCALE = 1.1f;
    private static final float APPLIED_OFFSET_X = 170f;

    private static final float HIGHLIGHT_PAD_X = 18f;
    private static final float HIGHLIGHT_PAD_Y = 10f;
    private static final float HIGHLIGHT_ALPHA = 0.45f;

    private static final Color MAIN_HIGHLIGHT_COLOR = new Color(0f, 0f, 0f, 1f);
    private static final Color FLAT_COLOR = new Color(0.20f, 0.45f, 0.95f, 1f);
    private static final Color PAYOUT_MULT_COLOR = new Color(0.15f, 0.85f, 0.15f, 1f);
    private static final Color TILE_MULT_COLOR = new Color(0.95f, 0.55f, 0.10f, 1f);
    private static final Color GLOBAL_MULT_COLOR = new Color(0.85f, 0.15f, 0.15f, 1f);

    private final BitmapFont font = FontManager.getInstance().getFontByName("Placeholder");
    private final SpriteBatch spriteBatch = RendererManager.getInstance().getSpriteBatch();
    private final ShapeRenderer shapeRenderer = RendererManager.getInstance().getShapeRenderer();
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private Phase phase = Phase.DONE;
    private float phaseElapsed;
    private WinBreakdown breakdown;
    private Runnable onImpact;

    private Vector2 anchor = new Vector2();
    private Vector2 flyTarget = new Vector2();

    private List<Step> steps = new ArrayList<>();
    private int stepIndex;

    // The single running number shown at anchor — starts at the stake, ends
    // at the final total.
    private float displayedValue;
    private Tween mainValueTween;
    private Tween mainScaleTween;
    private float mainScale = 1f;

    // "What was just applied" label to the right of the main number.
    private String appliedLabel;
    private Color appliedColor;
    private Tween appliedPopTween;
    private float appliedScale = 1f;

    // Flight of the final total toward the quota bar.
    private Tween flyXTween;
    private Tween flyYTween;
    private Tween flyScaleTween;
    private Vector2 currentFlyPosition = new Vector2();
    private float currentFlyScale = 1f;

    /**
     * @param anchor     where the running total sits (top-center of the
     *                   screen, in world/UI space — same space GameScreen
     *                   already draws the sprite batch UI in). The applied
     *                   label appears to the right of this.
     * @param flyTarget  where the final total should smash into — the quota
     *                   bar's position.
     * @param onImpact   called exactly once, the frame the total lands on
     *                   {@code flyTarget} — this is where the caller should
     *                   actually apply it to the quota display.
     */
    public void start(WinBreakdown breakdown, Vector2 anchor, Vector2 flyTarget, Runnable onImpact) {
        if (breakdown.isEmpty()) {
            SoundManager.getInstance().playSound("loseBet");
            onImpact.run();
            return; // nothing won, nothing to animate
        }
        this.breakdown = breakdown;
        this.anchor = anchor;
        this.flyTarget = flyTarget;
        this.onImpact = onImpact;

        buildSteps();

        this.phase = Phase.BET;
        this.phaseElapsed = 0f;
        this.displayedValue = breakdown.getWinningStake();
        this.stepIndex = 0;
        this.appliedLabel = null;
        this.mainValueTween = null;
        this.mainScaleTween = new Tween(POP_DURATION, MAIN_POP_SCALE, 1f, Tween.TweenStyle.BACK, Tween.TweenDirection.OUT);
    }

    /**
     * Lays out every "add/multiply this, here's the new running total" step
     * in reveal order. Flat bonus is skipped entirely while it's always 0
     * (see {@link WinBreakdown}) rather than showing a "+0" that means
     * nothing yet — once flat tile bonuses exist this only needs a nonzero
     * value from {@code RunState}, nothing here changes.
     */
    private void buildSteps() {
        steps.clear();
        float running = breakdown.getWinningStake();

        SoundManager.getInstance().playSound("tileSelect");

        running += breakdown.getFlatBonus();
        steps.add(new Step("+" + breakdown.getFlatBonus(), FLAT_COLOR, running));

        running *= breakdown.getPayoutMultiplier();
        steps.add(new Step("x" + trimTrailingZero(breakdown.getPayoutMultiplier()), PAYOUT_MULT_COLOR, running));

        running *= breakdown.getTileMultiplier();
        steps.add(new Step("x" + trimTrailingZero(breakdown.getTileMultiplier()), TILE_MULT_COLOR, running));

        // Snap the last step to the real rounded total rather than the
        // float-accumulated running value, so the number that lands matches
        // exactly what RunState.applyWinBreakdown will actually add.
        steps.add(new Step("x" + trimTrailingZero(breakdown.getGlobalMultiplier()), GLOBAL_MULT_COLOR,
            breakdown.getFinalTotal()));
    }

    public boolean isActive() {
        return phase != Phase.DONE;
    }

    public void update(float delta) {
        if (phase == Phase.DONE) {
            return;
        }

        if (mainScaleTween != null && !mainScaleTween.isComplete()) {
            mainScale = mainScaleTween.update(delta);
        }
        if (appliedPopTween != null && !appliedPopTween.isComplete()) {
            appliedScale = appliedPopTween.update(delta);
        }
        if (mainValueTween != null && !mainValueTween.isComplete()) {
            displayedValue = mainValueTween.update(delta);
        }
        if (phase == Phase.FLYING) {
            currentFlyPosition.set(flyXTween.update(delta), flyYTween.update(delta));
            currentFlyScale = flyScaleTween.update(delta);
            // Small upward arc layered on top of the straight tween so the
            // total feels thrown rather than just sliding, peaking at the
            // midpoint of the flight and settling back down by impact.
            float t = flyXTween.getAlpha();
            currentFlyPosition.y += FLY_ARC_HEIGHT * (1f - Math.abs(2f * t - 1f));
        }

        phaseElapsed += delta;
        float duration = durationFor(phase);

        if (phaseElapsed < duration) {
            return;
        }

        phaseElapsed -= duration;
        advancePhase();
    }

    private float durationFor(Phase p) {
        if (Objects.equals(p, Phase.FLYING)) {
            return FLY_DURATION;
        }
        return STEP_DURATION;
    }

    private void advancePhase() {
        switch (phase) {
            case BET:
                phase = Phase.STEPPING;
                stepIndex = 0;
                beginStep();
                SoundManager.getInstance().playSound("score1");
                break;
            case STEPPING:
                displayedValue = steps.get(stepIndex).newValue; // snap, avoid float drift
                stepIndex++;
                if (stepIndex < steps.size()) {
                    beginStep();
                    // Play the appropriate score sound based on the step index
                    switch (stepIndex) {
                        case 1:
                            SoundManager.getInstance().playSound("score2");
                            break;
                        case 2:
                            SoundManager.getInstance().playSound("score3");
                            break;
                        case 3:
                            SoundManager.getInstance().playSound("score4");
                            break;
                    }
                } else {
                    phase = Phase.FLYING;
                    flyXTween = new Tween(FLY_DURATION, anchor.x, flyTarget.x,
                        Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
                    flyYTween = new Tween(FLY_DURATION, anchor.y, flyTarget.y,
                        Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
                    flyScaleTween = new Tween(FLY_DURATION, FLY_START_SCALE, FLY_END_SCALE,
                        Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
                    currentFlyPosition.set(anchor);
                    currentFlyScale = FLY_START_SCALE;
                    break;
                }
                break;
            case FLYING:
                phase = Phase.DONE;
                if (onImpact != null) {
                    SoundManager.getInstance().playSound("winBet");
                    onImpact.run();
                }
                break;
            default:
                phase = Phase.DONE;
        }
    }

    private void beginStep() {
        Step step = steps.get(stepIndex);
        appliedLabel = step.appliedLabel;
        appliedColor = step.highlightColor;
        mainValueTween = new Tween(STEP_DURATION, displayedValue, step.newValue,
            Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
        mainScaleTween = new Tween(POP_DURATION, MAIN_POP_SCALE, 1f, Tween.TweenStyle.BACK, Tween.TweenDirection.OUT);
        appliedPopTween = new Tween(POP_DURATION, APPLIED_POP_SCALE, 1f, Tween.TweenStyle.BACK, Tween.TweenDirection.OUT);
    }

    public void render() {
        if (phase == Phase.DONE) {
            return;
        }

        if (phase == Phase.FLYING) {
            drawTextWithHighlight(mainLabel(), currentFlyPosition, currentFlyScale, MAIN_HIGHLIGHT_COLOR);
            return;
        }

        drawTextWithHighlight(mainLabel(), anchor, mainScale, MAIN_HIGHLIGHT_COLOR);

        if (appliedLabel != null) {
            Vector2 appliedPos = new Vector2(anchor.x + APPLIED_OFFSET_X, anchor.y);
            drawTextWithHighlight(appliedLabel, appliedPos, appliedScale, appliedColor);
        }
    }

    private String mainLabel() {
        return "$" + Math.round(displayedValue);
    }

    private String trimTrailingZero(float value) {
        if (value == Math.round(value)) {
            return String.valueOf(Math.round(value));
        }
        return String.format("%.2f", value);
    }

    /** White text on a translucent, color-coded highlight box, both centered on {@code pos}. */
    private void drawTextWithHighlight(String text, Vector2 pos, float scale, Color highlightColor) {
        float originalScaleX = font.getScaleX();
        float originalScaleY = font.getScaleY();
        Color originalColor = font.getColor().cpy();

        font.getData().setScale(BASE_FONT_SCALE * scale, BASE_FONT_SCALE * scale);
        glyphLayout.setText(font, text);
        float boxWidth = glyphLayout.width + HIGHLIGHT_PAD_X * 2f;
        float boxHeight = glyphLayout.height + HIGHLIGHT_PAD_Y * 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(highlightColor.r, highlightColor.g, highlightColor.b, HIGHLIGHT_ALPHA);
        shapeRenderer.rect(pos.x - boxWidth / 2f, pos.y - glyphLayout.height - HIGHLIGHT_PAD_Y, boxWidth, boxHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        font.setColor(Color.WHITE);
        spriteBatch.begin();
        font.draw(spriteBatch, text, pos.x - boxWidth / 2f, pos.y, boxWidth, Align.center, false);
        spriteBatch.end();

        font.getData().setScale(originalScaleX, originalScaleY);
        font.setColor(originalColor);
    }
}

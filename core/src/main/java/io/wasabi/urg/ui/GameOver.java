package io.wasabi.urg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.util.tweens.Tween;

public class GameOver {
    private static final float WIDTH = 760f;
    private static final float HEIGHT = 480f;
    private static final float CENTER_X = -WIDTH / 2f;
    private static final float OFFSCREEN_Y = -1500f;

    // How dark the red screen filter gets once it has fully faded in.
    private static final float RED_FILTER_MAX_ALPHA = 0.25f;

    private static final float HEADER_HEIGHT = 84f;
    private static final float OUTER_PAD = 24f;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final Viewport viewport;
    private final NinePatch patch;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private Tween panelTween;
    private Tween fadeTween;
    private float y = OFFSCREEN_Y;
    private float redFade = 0f;
    private boolean visible;

    public GameOver(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch, Viewport viewport) {
        this.shapeRenderer = shapeRenderer;
        this.spriteBatch = spriteBatch;
        this.viewport = viewport;
        Texture texture = new Texture(Gdx.files.internal("ui/CorneredPatch.png"));
        this.patch = new NinePatch(texture, 10, 10, 10, 10);
        this.font = FontManager.getInstance().getFontByName("Placeholder");
    }

    public void show() {
        visible = true;
        y = OFFSCREEN_Y;
        redFade = 0f;
        panelTween = new Tween(1f, OFFSCREEN_Y, 0f, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
        fadeTween = new Tween(1f, 0f, RED_FILTER_MAX_ALPHA, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
    }

    public void hide() {
        if (!visible) {
            return;
        }

        panelTween = new Tween(1f, y, OFFSCREEN_Y, Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
        fadeTween = new Tween(1f, redFade, 0f, Tween.TweenStyle.QUAD, Tween.TweenDirection.IN);
    }

    public void update(float delta) {
        if (!visible) {
            return;
        }

        if (panelTween != null && !panelTween.isComplete()) {
            y = panelTween.update(delta);
        }

        if (fadeTween != null && !fadeTween.isComplete()) {
            redFade = fadeTween.update(delta);
        }
    }

    public void render() {
        if (!visible) {
            return;
        }

        drawRedFilter();

        float bottom = y - HEIGHT / 2f;
        float panelBottom = bottom;
        float panelTop = panelBottom + HEIGHT;

        spriteBatch.begin();
        spriteBatch.setTransformMatrix(new com.badlogic.gdx.math.Matrix4().setToTranslation(0, 0, 0));

        // Soft drop shadow behind the whole panel for depth.
        spriteBatch.setColor(0f, 0f, 0f, 0.35f);
        patch.draw(spriteBatch, CENTER_X - OUTER_PAD, panelBottom - OUTER_PAD - 6f,
            WIDTH + OUTER_PAD * 2f, HEIGHT + OUTER_PAD * 2f);

        // Thin bright outline just outside the panel body for a "framed" look.
        spriteBatch.setColor(0.55f, 0.16f, 0.16f, 0.9f);
        patch.draw(spriteBatch, CENTER_X - 4f, panelBottom - 4f, WIDTH + 8f, HEIGHT + 8f);

        // Main panel body.
        spriteBatch.setColor(0.14f, 0.14f, 0.17f, 1f);
        patch.draw(spriteBatch, CENTER_X, panelBottom, WIDTH, HEIGHT);

        // Header strip that holds the title.
        spriteBatch.setColor(0.42f, 0.13f, 0.13f, 1f);
        patch.draw(spriteBatch, CENTER_X, panelTop - HEADER_HEIGHT, WIDTH, HEADER_HEIGHT);

        // Thin accent divider under the header.
        spriteBatch.setColor(0.85f, 0.24f, 0.24f, 1f);
        patch.draw(spriteBatch, CENTER_X, panelTop - HEADER_HEIGHT - 4f, WIDTH, 4f);

        spriteBatch.setColor(1f, 1f, 1f, 1f);

        layout.setText(font, "GAME OVER");
        font.draw(spriteBatch, "GAME OVER", CENTER_X + (WIDTH - layout.width) / 2f, panelTop - 30f);

        layout.setText(font, "YOU FAILED TO HIT THE QUOTA");
        font.draw(spriteBatch, "YOU FAILED TO HIT THE QUOTA", CENTER_X + (WIDTH - layout.width) / 2f,
            panelTop - HEADER_HEIGHT - 55f);

        spriteBatch.setColor(1f, 1f, 1f, 1);
        layout.setText(font, "CLICK ANYWHERE TO CONTINUE");
        font.draw(spriteBatch, "CLICK ANYWHERE TO CONTINUE", CENTER_X + (WIDTH - layout.width) / 2f,
            panelBottom + 46f);

        spriteBatch.end();
    }

    private void drawRedFilter() {
        float pad = 200f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float left = -worldWidth / 2f - pad;
        float bottom = -worldHeight / 2f - pad;

        // ShapeRenderer does not enable alpha blending on its own, so without this the
        // filter's alpha is ignored and it renders fully opaque instead of fading in.
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.68f, 0.10f, 0.10f, redFade);
        shapeRenderer.rect(left, bottom, worldWidth + pad * 2f, worldHeight + pad * 2f);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public boolean isVisible() {
        return visible;
    }

    public float getY() {
        return y;
    }
}

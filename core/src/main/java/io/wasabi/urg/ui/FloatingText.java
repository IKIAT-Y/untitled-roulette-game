package io.wasabi.urg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;

public class FloatingText extends GameObject {
    private static final float DURATION = 1.5f;   // total lifetime, in seconds
    private static final float RISE_SPEED = 40f;  // world units per second
    private static final float FADE_START = 0.6f; // fraction of DURATION when fading begins

    private static final float PADDING_X = 12f;
    private static final float PADDING_Y = 8f;

    private final SpriteBatch SPRITE_BATCH;
    private final ShapeRenderer SHAPE_RENDERER;

    private final String text;
    private final float startX;
    private final float startY;
    private final Color color;
    private float elapsed = 0f;

    public FloatingText(String text, float x, float y) {
        this(text, x, y, Color.WHITE);
    }

    public FloatingText(String text, float x, float y, Color color) {
        this.text = text;
        this.startX = x;
        this.startY = y;
        this.color = color;
        this.SPRITE_BATCH = RendererManager.getInstance().getSpriteBatch();
        this.SHAPE_RENDERER = RendererManager.getInstance().getShapeRenderer();
    }

    public void update(float delta) {
        elapsed += delta;
    }

    public boolean isExpired() {
        return elapsed >= DURATION;
    }

    public void render() {
        float progress = Math.min(1f, elapsed / DURATION);
        float y = startY + RISE_SPEED * elapsed;

        float alpha = progress < FADE_START ? 1f : 1f - (progress - FADE_START) / (1f - FADE_START);

        BitmapFont font = FontManager.getInstance().getFontByName("Placeholder");
        GlyphLayout layout = new GlyphLayout(font, text);

        float bgX = startX - layout.width / 2f - PADDING_X;
        float bgY = y - layout.height - PADDING_Y;
        float bgWidth = layout.width + PADDING_X * 2f;
        float bgHeight = layout.height + PADDING_Y * 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Filled);
        SHAPE_RENDERER.setColor(0f, 0f, 0f, 0.6f * alpha);
        SHAPE_RENDERER.rect(bgX, bgY, bgWidth, bgHeight);
        SHAPE_RENDERER.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        Color previous = font.getColor().cpy();
        font.setColor(color.r, color.g, color.b, alpha);

        SPRITE_BATCH.begin();
        font.draw(SPRITE_BATCH, text, startX - layout.width / 2f, y);
        SPRITE_BATCH.end();

        font.setColor(previous);
    }
}

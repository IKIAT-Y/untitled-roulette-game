package io.wasabi.urg.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;

public class Tooltip {
    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final ShapeRenderer SHAPE_RENDERER = RENDERER_MANAGER.getShapeRenderer();
    private static final SpriteBatch SPRITE_BATCH = RENDERER_MANAGER.getSpriteBatch();

    private static final Roulette GAME = Roulette.getInstance();
    private static final Viewport VIEWPORT = GAME.getViewport();

    private static final Texture TEXTURE = new Texture(Gdx.files.internal("ui/CorneredPatch.png"));

    public class Line {
        private String text;
        private Color col;
        private Color backgroundColor;
        private BitmapFont font;
        private final GlyphLayout layout = new GlyphLayout();

        public Line(BitmapFont font, String text) {
            this(font, text, Color.BLACK);
        }

        public Line(BitmapFont font, String text, Color col) {
            this.text = text;
            this.col = col;
            this.font = font;
        }

        public GlyphLayout update(float lineWidth, boolean wrap) {
            layout.setText(font, text, col, lineWidth, Align.center, wrap);
            return layout;
        }

        public void draw(float x, float y, float lineWidth, float lineHeight) {
            font.setColor(col);
            font.draw(SPRITE_BATCH, text, x, y + lineHeight / 2f + layout.height / 2f, lineWidth, Align.center, true);
        }

        public GlyphLayout getLayout() { return layout; }
        public void setText(String text) { this.text = text; }
        public void setColor(Color col) { this.col = col; }
        public void setBackgroundColor(Color col) { this.backgroundColor = col; }
        public Color getBackgroundColor() { return this.backgroundColor; }
        public float getLineHeight() { return font.getLineHeight(); }
    }

    private boolean visible = false;
    private float x, y;
    private float anchorX, anchorY;
    private float width, height;
    private float padding = 2.5f;
    private float innerPadding = 10f;
    private float elementPadding = 5.0f;

    private BitmapFont font_16px = FontManager.getInstance().getFontByName("Terminus16PXBold");
    private BitmapFont font_12px = FontManager.getInstance().getFontByName("Terminus12PXBold");
    private float fontPaddingX = 5f;
    private float fontPaddingY = 4f;

    private NinePatch patch;

    private Line title = new Line(font_16px, "");
    private Line description = new Line(font_12px, "");
    private List<Line> types = new ArrayList<>();

    // visual changes
    private boolean descVisible = true;

    public Tooltip(float anchorX, float anchorY) {
        patch = new NinePatch(TEXTURE, 10, 10, 10, 10);
        //tex.dispose();

        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setDescriptionVisible(boolean bool) {
        descVisible = bool;
        updateSizes();
    }

    public void setAnchorPoints(float x, float y, boolean skipCheck) {
        this.anchorX = x;
        this.anchorY = y;
    }

    public void setTitle(String text) {
        setTitle(text, Color.BLACK);
    }

    public void setTitle(String text, Color color) {
        title.setText(text);
        title.setColor(color);
        updateSizes();
    }

    public void setDescription(String text) {
        setDescription(text, Color.BLACK);
    }

    public void setDescription(String text, Color color) {
        description.setText(text);
        description.setColor(color);
        updateSizes();
    }

    public void addType(String text, Color col, Color bgCol) {
        Line type = new Line(font_16px, text, col);
        type.setBackgroundColor(bgCol);
        types.add(type);
        updateSizes();
    }

    public void removeType(String text) {
        types.removeIf(line -> line.text.equals(text));
        updateSizes();
    }

    private void updateSizes() {
        GlyphLayout titleLayout = title.update(width, false);
        width = titleLayout.width + fontPaddingX * 2 + innerPadding * 2 + padding * 2;
        height = titleLayout.height + fontPaddingY * 2 + innerPadding * 2 + padding * 2;

        if (descVisible) {
            GlyphLayout descriptionLayout = description.update(titleLayout.width, true);
            height += descriptionLayout.height + fontPaddingY * 2 + elementPadding;
        }

        for (Line line : types) {
            height += elementPadding;
            GlyphLayout layout = line.update(titleLayout.width, false);
            width = Math.max(width, layout.width + fontPaddingX * 2 + innerPadding * 2 + padding * 2);
            height += layout.height + fontPaddingY * 2;
        }
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void show() {
        if (!visible) {
            visible = true;
            GAME.getRunState().setTooltip(this);
        }
    }

    public void hide() {
        visible = false;
    }

    public void render() {
        if (visible) {
            SPRITE_BATCH.begin();

            float cX = x - width * anchorX;
            float cY = y - height * anchorY;

            float patchX = cX + innerPadding + padding;
            float patchTextX = cX + innerPadding + fontPaddingX + padding;
            // drop shadow
            SPRITE_BATCH.setColor(0.10f, 0.10f, 0.13f, 1f);
            patch.draw(SPRITE_BATCH, cX, cY - padding, width, height);

            // white outline
            SPRITE_BATCH.setColor(1, 1, 1, 1);
            patch.draw(SPRITE_BATCH, cX, cY, width, height);

            // black inner
            SPRITE_BATCH.setColor(0.10f, 0.10f, 0.13f, 1f);
            patch.draw(SPRITE_BATCH, cX + padding, cY + padding, width - padding * 2, height - padding * 2);

            float innerWidth = width - innerPadding * 2 - padding * 2;
            float lineHeight = title.getLineHeight() + fontPaddingY * 2;
            float titleY = cY + height - lineHeight - innerPadding;
            // title
            SPRITE_BATCH.setColor(1, 1, 1, 1);
            patch.draw(SPRITE_BATCH, patchX, titleY, innerWidth, lineHeight);
            title.draw(patchTextX, titleY, innerWidth - fontPaddingX * 2, lineHeight);

            // description
            float descHeight = description.getLayout().height + fontPaddingY * 2;
            float descY = titleY - descHeight - elementPadding;
            if (descVisible) {
                patch.draw(SPRITE_BATCH, patchX, descY, innerWidth, descHeight);
                description.draw(patchTextX, descY, innerWidth - fontPaddingX * 2, descHeight);
            } else {
                descY = titleY - elementPadding;
            }

            float typesY = descY;
            for (Line line : types) {
                typesY -= elementPadding;
                float typeHeight = line.getLayout().height + fontPaddingY * 2;
                typesY -= typeHeight;
                SPRITE_BATCH.setColor(line.getBackgroundColor());
                patch.draw(SPRITE_BATCH, patchX, typesY, innerWidth, typeHeight);
                line.draw(patchTextX, typesY, innerWidth - fontPaddingX * 2, typeHeight);
            }

            SPRITE_BATCH.setColor(1, 1, 1, 1);
            SPRITE_BATCH.end();
        }
    }
}

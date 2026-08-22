package io.wasabi.urg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import io.wasabi.urg.managers.FontManager;

/** Computes fixed card-slot positions for the player's cards, laid out in a centered row. */
public final class CardLayout {
    public static final float CARD_WIDTH = 96f;
    public static final float HAND_Y = 250f; // shift upwards
    public static final float HAND_X = 460f; // shifts to right

    public static final float HAND_WIDTH = 300f;

    private static final Texture SLOT_TEXTURE = new Texture(Gdx.files.internal("ui/CorneredPatch.png"));
    private static final NinePatch SLOT_PATCH = new NinePatch(SLOT_TEXTURE, 10, 10, 10, 10);

    private static final float OUTLINE_PADDING = 2.5f;
    private static final Color COL_SHADOW = new Color(0f, 0f, 0f, 0.3f);

    private CardLayout() {}

    public static Vector2 getSlotPosition(int index, int count, float worldWidth) {
        float x = HAND_X + getOffset(count) + index * getStride(count);
        return new Vector2(x, HAND_Y);
    }

    private static float getIdealPadding(int count) {
        if (count <= 0) {
            return HAND_WIDTH;
        }
        return (HAND_WIDTH - count * CARD_WIDTH) / (count + 1);
    }

    private static float getOffset(int count) {
        if (count <= 0) {
            return 0f;
        }
        return Math.max(0f, getIdealPadding(count));
    }

    private static float getStride(int count) {
        if (count <= 1) {
            return CARD_WIDTH;
        }

        float idealPadding = getIdealPadding(count);
        if (idealPadding >= 0f) {
            return CARD_WIDTH + idealPadding;
        }

        float gap = (HAND_WIDTH - count * CARD_WIDTH) / (count - 1);
        return CARD_WIDTH + gap;
    }

    public static void renderBackPanel(SpriteBatch spriteBatch, int maxHandSize, int handSize) {
        float panelX = HAND_X - 20f;
        float panelY = HAND_Y - 20f;
        float panelWidth = 340f;
        float panelHeight = 200f;

        spriteBatch.setColor(COL_SHADOW);
        SLOT_PATCH.draw(spriteBatch, panelX, panelY - OUTLINE_PADDING, panelWidth, panelHeight);
        spriteBatch.setColor(Color.WHITE);


        BitmapFont font = FontManager.getInstance().getFontByName("Terminus16PXBold");
        if (font != null) {
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, "CARDS: " + handSize + "/" + maxHandSize, panelX + (panelWidth/2) - 40f, panelY + panelHeight - 18f);
        }
    }

    public static int getClosestIndex(float cardX, int count, float worldWidth) {
        if (count <= 1) {
            return 0;
        }

        float stride = getStride(count);
        int index = Math.round((cardX - HAND_X - getOffset(count)) / stride);
        return Math.max(0, Math.min(count - 1, index));
    }

    /* Deprecated
    public static void renderSlotPanels(SpriteBatch spriteBatch, float worldWidth) {
        for (int i = 0; i < MAX_HAND_SIZE; i++) {
            Vector2 slot = getSlotPosition(i, MAX_HAND_SIZE, worldWidth);
            drawSlotPanel(spriteBatch, slot.x, slot.y, CARD_WIDTH, CARD_HEIGHT);
        }
    }

    private static void drawSlotPanel(SpriteBatch spriteBatch, float x, float y, float width, float height) {
        spriteBatch.setColor(COL_SHADOW);
        SLOT_PATCH.draw(spriteBatch, x, y - OUTLINE_PADDING, width, height);

        spriteBatch.setColor(COL_OUTLINE);
        SLOT_PATCH.draw(spriteBatch, x, y, width, height);

        spriteBatch.setColor(COL_FILL);
        SLOT_PATCH.draw(
            spriteBatch,
            x + OUTLINE_PADDING,
            y + OUTLINE_PADDING,
            width - OUTLINE_PADDING * 2,
            height - OUTLINE_PADDING * 2
        );
        spriteBatch.setColor(Color.WHITE);
    }
    */
}

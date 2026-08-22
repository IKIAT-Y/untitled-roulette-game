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
public final class CharmLayout {
    public static final int MAX_HAND_SIZE = 4;
    public static final float CARD_WIDTH = 64f;
    public static final float CARD_HEIGHT = 64f;
    public static final float CARD_SPACING = 24f;
    public static final float HAND_Y = 75f; // place charms underneath the cards
    public static final float HAND_X = 390f; // shifts to right

    private static final Texture SLOT_TEXTURE = new Texture(Gdx.files.internal("ui/CorneredPatch.png"));
    private static final NinePatch SLOT_PATCH = new NinePatch(SLOT_TEXTURE, 10, 10, 10, 10);

    private static final float OUTLINE_PADDING = 2.5f;
    private static final Color COL_SHADOW = new Color(0.10f, 0.10f, 0.13f, 0.65f);
    private static final Color COL_OUTLINE = new Color(1f, 1f, 1f, 0.9f);
    private static final Color COL_FILL = new Color(0.13f, 0.19f, 0.17f, 0.55f);
    private static final Color COL_BACKING = new Color( 0.29020f, 0.22745f, 0.18039f, 1f );

    private CharmLayout() {}

    public static Vector2 getSlotPosition(int index, int count, float worldWidth) {
        float totalWidth = count * CARD_WIDTH + Math.max(0, count - 1) * CARD_SPACING;
        float x = HAND_X + index * (CARD_WIDTH + CARD_SPACING);
        return new Vector2(x, HAND_Y);
    }

    public static void renderSlotPanels(SpriteBatch spriteBatch, float worldWidth) {
        for (int i = 0; i < MAX_HAND_SIZE; i++) {
            Vector2 slot = getSlotPosition(i, MAX_HAND_SIZE, worldWidth);
            drawSlotPanel(spriteBatch, slot.x, slot.y, CARD_WIDTH, CARD_HEIGHT);
        }
    }

    public static void renderRightBackPanel(SpriteBatch spriteBatch, float worldWidth, float worldHeight) {
        float panelX = HAND_X - 20f;
        float panelY = HAND_Y - 20f;
        float panelWidth = 390f;
        float panelHeight = 130f;

        spriteBatch.setColor(COL_SHADOW);
        SLOT_PATCH.draw(spriteBatch, panelX, panelY - OUTLINE_PADDING, panelWidth, panelHeight);

        spriteBatch.setColor(COL_OUTLINE);
        SLOT_PATCH.draw(spriteBatch, panelX, panelY, panelWidth, panelHeight);

        spriteBatch.setColor(COL_BACKING);
        SLOT_PATCH.draw(
            spriteBatch,
            panelX + OUTLINE_PADDING,
            panelY + OUTLINE_PADDING,
            panelWidth - OUTLINE_PADDING * 2,
            panelHeight - OUTLINE_PADDING * 2
        );
        spriteBatch.setColor(Color.WHITE);

        BitmapFont font = FontManager.getInstance().getFontByName("Terminus16PXBold");
        if (font != null) {
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, "CHARMS", panelX + (panelWidth/2) - 20f, panelY + panelHeight - 18f);
        }
    }

    public static int getClosestIndex(float cardX, int count, float worldWidth) {
        float totalWidth = count * CARD_WIDTH + Math.max(0, count - 1) * CARD_SPACING;
        float stride = CARD_WIDTH + CARD_SPACING;
        int index = Math.round((cardX - HAND_X) / stride);
        return Math.max(0, Math.min(count - 1, index));
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
}

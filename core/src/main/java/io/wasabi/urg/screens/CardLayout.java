package io.wasabi.urg.screens;

import com.badlogic.gdx.math.Vector2;

/** Computes fixed card-slot positions for the player's cards, laid out in a centered row. */
public final class CardLayout {
    public static final int MAX_HAND_SIZE = 4;
    public static final float CARD_WIDTH = 96f;
    public static final float CARD_HEIGHT = 128f;
    public static final float CARD_SPACING = 24f;
    public static final float HAND_Y = 50f; // shift upwards
    public static final float HAND_X = 300f; // shifts to right

    private CardLayout() {}

    public static Vector2 getSlotPosition(int index, int count, float worldWidth) {
        float totalWidth = count * CARD_WIDTH + Math.max(0, count - 1) * CARD_SPACING;
        float x = HAND_X + index * (CARD_WIDTH + CARD_SPACING);
        return new Vector2(x, HAND_Y);
    }

    public static int getClosestIndex(float cardX, int count, float worldWidth) {
        float totalWidth = count * CARD_WIDTH + Math.max(0, count - 1) * CARD_SPACING;
        float stride = CARD_WIDTH + CARD_SPACING;
        int index = Math.round((cardX - HAND_X) / stride);
        return Math.max(0, Math.min(count - 1, index));
    }
}

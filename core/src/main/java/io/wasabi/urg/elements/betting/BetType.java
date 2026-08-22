package io.wasabi.urg.elements.betting;

public enum BetType {

    STRAIGHT(36f, 1),
    SPLIT(18f, 2),
    STREET(12f, 3),
    CORNER(9f, 4),
    SIX_LINE(6f, 6),
    COLUMN(3f, -1), DOZEN(3f, -1),
    RED(2f, -1), BLACK(2f, -1),
    ODD(2f, -1), EVEN(2f, -1),
    HIGH(2f, -1), LOW(2f, -1);

    public float payoutMultiplier;

    // use -1 for variable pockets required
    public final int requiredPockets;

    BetType(float payoutMultiplier, int requiredPockets) {
        this.payoutMultiplier = payoutMultiplier;
        this.requiredPockets = requiredPockets;
    }
}

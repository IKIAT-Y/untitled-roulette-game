package io.wasabi.urg.elements.betting;

public enum ChipDenomination {

    ONE(0.01f),
    FIVE(0.05f),
    TEN(0.10f),
    TWENTY_FIVE(0.25f),
    FIFTY(0.50f),
    HUNDRED(1f);

    // Chip denominations are based off percentage of balance
    // 1% of balance is the minimum bet, 100% is the maximum bet
    public final float value;

    ChipDenomination(float value) {
        this.value = value;
    }
}

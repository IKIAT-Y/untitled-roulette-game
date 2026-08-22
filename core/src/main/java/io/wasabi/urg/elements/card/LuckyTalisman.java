package io.wasabi.urg.elements.card;

import io.wasabi.urg.elements.game.Tile;

public class LuckyTalisman extends Card {
    public LuckyTalisman() { super(Rarity.UNCOMMON); }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        triggerDisplay();
        return 1.5f;
    }

}

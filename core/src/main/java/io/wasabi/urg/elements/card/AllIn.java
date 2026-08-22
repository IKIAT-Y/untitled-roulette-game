package io.wasabi.urg.elements.card;

import io.wasabi.urg.elements.game.Tile;

public class AllIn extends Card {
    public AllIn() { super(Rarity.UNCOMMON); }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        return chipBalance > 0 && totalStaked == chipBalance ? 2f : 1f;
    }
}

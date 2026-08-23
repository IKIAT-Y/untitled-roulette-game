package io.wasabi.urg.elements.card;

import java.util.ArrayList;
import java.util.List;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class GreenCard extends Card {

    public GreenCard() {
        super(Rarity.COMMON);
        this.price = 3;
        this.sellPrice = 1;
        tooltip.setTitle("Green Card");
        tooltip.setDescription("[GREEN]Green [BLACK]tiles give [RED]3x [BLACK]payout");
    }

    @Override
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) {
        if (winningTile.getType().isGreen()) {
            return 3f;
        }
        return 1f;
    }
}

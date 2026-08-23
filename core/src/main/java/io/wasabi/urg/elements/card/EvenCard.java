package io.wasabi.urg.elements.card;

import java.util.List;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class EvenCard extends Card {

    public EvenCard() {
        super(Rarity.COMMON);
        this.price = 3;
        this.sellPrice = 1;
        tooltip.setTitle("Even Card");
        tooltip.setDescription("Even numbered tiles give [BLUE]+10 [BLACK]bonus chip payout when scored");
    }

    @Override
    public float getFlatBonus(Tile winningTile, int totalStaked, int chipBalance) {
        if (winningTile.getNumber() % 2 == 0) {
            return 10f;
        }
        return 0f;
    }
}

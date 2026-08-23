package io.wasabi.urg.elements.card;
import java.util.ArrayList;
import java.util.List;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class RedCard extends Card {

    public RedCard() {
        super(Rarity.COMMON);
        this.price = 3;
        this.sellPrice = 1;
        tooltip.setTitle("Red Card");
        tooltip.setDescription("Red tiles give [BLUE]+10 [BLACK]bonus chip payout when scored");
    }

    @Override
    public float getFlatBonus(Tile winningTile, int totalStaked, int chipBalance) {
        if (winningTile.getType().isRed()) {
            return 10f;
        }
        return 0f;
    }
}

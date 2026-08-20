package io.wasabi.urg.elements.card;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.tiles.VoidTile;

public class MysteriousFragment extends Card {
    public MysteriousFragment() {
        super(Rarity.RARE);
        tooltip.setTitle("Mysterious Fragment");
        tooltip.setDescription(
                "At round start, turn a random tile [PURPLE]VOID[BLACK]. "
                        + "Void tiles give [RED]3x [BLACK]payout and are destroyed when landed on");
    }

    @Override
    public void roundStartEffect() {
        List<Tile> tiles = new ArrayList<>(Roulette.getInstance().getRunState().getTiles());
        tiles.removeIf(tile -> tile.getType() instanceof VoidTile);
        if (!tiles.isEmpty()) {
            Tile voidTile = tiles.get(MathUtils.random(tiles.size() - 1));
            voidTile.setType(new VoidTile(voidTile.getType(), voidTile));
        }
    }
}

package io.wasabi.urg.elements.card;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class MysteriousFragment extends Card {
    private final List<Tile> voidTiles = new ArrayList<>();
    private int activeRound = -1;

    public MysteriousFragment() { super(Rarity.RARE); }

    @Override
    public void roundStartEffect() {
        int roundNumber = Roulette.getInstance().getRoundManager().getOverallRoundNumber();
        if (roundNumber != activeRound) {
            activeRound = roundNumber;
            voidTiles.clear();
        }

        List<Tile> tiles = new ArrayList<>(Roulette.getInstance().getRunState().getTiles());
        tiles.removeAll(voidTiles);
        if (!tiles.isEmpty()) {
            Tile voidTile = tiles.get(MathUtils.random(tiles.size() - 1));
            voidTile.setBetMultiplier(voidTile.getBetMultiplier() * 3f);
            voidTile.addIndicator(Tile.Indicator.VOID);
            voidTiles.add(voidTile);
        }
    }

    @Override
    public void afterSpinEffect() {
        Tile landedTile = Roulette.getInstance().getRunState().getLastTile();
        if (voidTiles.remove(landedTile)) {
            landedTile.removeIndicator(Tile.Indicator.VOID);
            Roulette.getInstance().getRunState().removeTile(landedTile);
        }
    }

    @Override
    public void roundEndEffect() {
        for (Tile voidTile : voidTiles) {
            voidTile.setBetMultiplier(voidTile.getBetMultiplier() / 3f);
            voidTile.removeIndicator(Tile.Indicator.VOID);
        }
        voidTiles.clear();
    }
}

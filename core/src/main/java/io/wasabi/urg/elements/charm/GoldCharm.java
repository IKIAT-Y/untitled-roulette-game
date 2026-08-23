package io.wasabi.urg.elements.charm;

import com.badlogic.gdx.graphics.Color;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.tiles.GoldTile;
import io.wasabi.urg.elements.tiles.TileType;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.ui.FloatingText;

import java.util.List;

public class GoldCharm extends Charm {

    public GoldCharm() {
        super();
        tooltip.setTitle("Gold Charm");
        tooltip.setDescription("Choose one tile and enchant it with Golden. Gain 4 [#FFCB1FFF]TICKETS [BLACK]when landing on this tile.");
    }

    @Override
    public void consume() {
        if (requirements()) {
            super.consume();
            List<Tile> selectedTiles = Roulette.getInstance().getRunState().getSelectedTiles();
            for (Tile tile : selectedTiles) {
                TileType originalType = tile.getType();
                GoldTile goldType = new GoldTile();
                goldType.setColour(originalType.getColour());
                goldType.setNumber(originalType.getNumber());
                goldType.setBetMultiplier(originalType.getBetMultiplier());
                tile.setType(goldType);
            }
            Roulette.getInstance().getRunState().clearSelectedTiles();
            removeAndReturnToPool();
            SoundManager.getInstance().playSound("charmConsume");
        }
    }

    @Override
    public boolean requirements() {
        if (Roulette.getInstance().getGameScreen().getWheel().isSpinning()) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("You cannot use charms while the wheel is spinning!", getX(), getY(), Color.RED, 1f));
            SoundManager.getInstance().playSound("error");
            return false;
        }

        List<Tile> selectedTiles = Roulette.getInstance().getRunState().getSelectedTiles();
        if (selectedTiles.isEmpty()) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("Select one tile!", getX(), getY(), Color.RED, 1f));
            SoundManager.getInstance().playSound("error");
        } else if (selectedTiles.size() > 1) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("You can only select one tile!", getX(), getY(), Color.RED, 1f));
            SoundManager.getInstance().playSound("error");
        } else if (selectedTiles.get(0).getType() instanceof GoldTile) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("This tile is already golden!", getX(), getY(), Color.RED, 1f));
            SoundManager.getInstance().playSound("error");
        } else {
            return true;
        }
        return false;
    }
}

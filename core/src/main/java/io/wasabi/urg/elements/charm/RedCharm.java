package io.wasabi.urg.elements.charm;

import com.badlogic.gdx.graphics.Color;
import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.tiles.TileType;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.ui.FloatingText;

import java.util.List;

public class RedCharm extends AbstractCharm {

    public RedCharm() {
        super();
        tooltip.setTitle("Red Charm");
        tooltip.setDescription("Choose up to two tiles, turn them into red tiles.");
    }

    @Override
    public void consume() {
        if (requirements()) {
            List<Tile> selectedTiles = Roulette.getInstance().getRunState().getSelectedTiles();
            for (Tile tile : selectedTiles) {
                tile.setColor(TileType.TileColour.RED);
            }
            Roulette.getInstance().getRunState().clearSelectedTiles();
            removeAndReturnToPool();
            SoundManager.getInstance().playSound("charmConsume");
        }
    }

    @Override
    public boolean requirements() {
        if (Roulette.getInstance().getGameScreen().getWheel().isSpinning()) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("You cannot use charms while the wheel is spinning!", getX(), getY(), Color.RED));
            SoundManager.getInstance().playSound("error");
            return false;
        }

        List<Tile> selectedTiles = Roulette.getInstance().getRunState().getSelectedTiles();
        if (selectedTiles.isEmpty()) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("Select at least one tile!", getX(), getY(), Color.RED));
            SoundManager.getInstance().playSound("error");
        } else if (selectedTiles.size() > 2) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("You can only select up to two tiles!", getX(), getY(), Color.RED));
            SoundManager.getInstance().playSound("error");
        } else {
            return true;
        }
        return false;
    }
}

package io.wasabi.urg.elements.charm;

import com.badlogic.gdx.graphics.Color;
import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.tiles.TileType;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.ui.FloatingText;

import java.util.List;
import java.util.Random;

public class ScrambledCharm extends AbstractCharm {

    public ScrambledCharm() {
        super();
        tooltip.setTitle("Scrambled Charm");
        tooltip.setDescription("Choose up to four tiles, randomise their number between 0 and 36");
    }

    @Override
    public void consume() {
        if (requirements()) {
            List<Tile> selectedTiles = Roulette.getInstance().getRunState().getSelectedTiles();
            Random random = new Random();
            for (Tile tile : selectedTiles) {
                int randomNumber = random.nextInt(37); // Generates a random number between 0 and 36
                TileType type = tile.getType();
                type.setNumber(randomNumber);
                tile.setType(type);
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
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("Select at least one tile!", getX(), getY(), Color.RED, 1f));
            SoundManager.getInstance().playSound("error");
        } else if (selectedTiles.size() > 4) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("You can only select up to four tiles!", getX(), getY(), Color.RED, 1f));
            SoundManager.getInstance().playSound("error");
        } else {
            return true;
        }
        return false;
    }

}

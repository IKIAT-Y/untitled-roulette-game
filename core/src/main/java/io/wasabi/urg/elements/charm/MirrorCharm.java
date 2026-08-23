package io.wasabi.urg.elements.charm;

import com.badlogic.gdx.graphics.Color;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.tiles.DefaultTile;
import io.wasabi.urg.elements.tiles.GoldTile;
import io.wasabi.urg.elements.tiles.MetallicTile;
import io.wasabi.urg.elements.tiles.NullTile;
import io.wasabi.urg.elements.tiles.NumberlessTile;
import io.wasabi.urg.elements.tiles.StripedTile;
import io.wasabi.urg.elements.tiles.TileType;
import io.wasabi.urg.elements.tiles.VoidTile;
import io.wasabi.urg.managers.SoundManager;
import io.wasabi.urg.ui.FloatingText;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

public class MirrorCharm extends Charm {

    public MirrorCharm() {
        super();
        tooltip.setTitle("Mirror Charm");
        tooltip.setDescription("Choose two tiles, one will randomly become the other.");
    }

    @Override
    public void consume() {
        if (requirements()) {
            super.consume();
            List<Tile> selectedTiles = Roulette.getInstance().getRunState().getSelectedTiles();
            Random random = new Random();

            Tile source = selectedTiles.get(random.nextBoolean() ? 0 : 1);
            Tile target = source == selectedTiles.get(0) ? selectedTiles.get(1) : selectedTiles.get(0);

            TileType sourceType = source.getType();
            try {
                TileType targetType = sourceType.getClass().getDeclaredConstructor().newInstance();
                targetType.setColour(sourceType.getColour());
                targetType.setNumber(sourceType.getNumber());
                target.setType(targetType);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to create a new instance of " + sourceType.getClass().getSimpleName(), e);
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
        if (selectedTiles.size() < 2) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("Select two tiles!", getX(), getY(), Color.RED, 1f));
            SoundManager.getInstance().playSound("error");
        } else if (selectedTiles.size() > 2) {
            Roulette.getInstance().getGameScreen().addParticle(new FloatingText("You can only select two tiles!", getX(), getY(), Color.RED, 1f));
            SoundManager.getInstance().playSound("error");
        } else {
            return true;
        }
        return false;
    }
}

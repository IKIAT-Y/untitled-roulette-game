package io.wasabi.urg.elements.boss;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.elements.tiles.DefaultTile;
import io.wasabi.urg.elements.tiles.GoldTile;
import io.wasabi.urg.elements.tiles.NullTile;
import io.wasabi.urg.elements.tiles.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Gamer extends Boss {

    public Gamer() {
        super("The Gamer", "He is a gamer.", "Add 5 null tiles to the table, these tiles give no payout.");
    }

    @Override
    public void roundStartEffect() {
        List<Tile> tiles = Roulette.getInstance().getRunState().getTiles();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            TileType type = new NullTile();

            type.setColour(TileType.TileColour.GREEN);
            Tile tile = new Tile(Roulette.getInstance().getGameScreen().getWorld(), type, Vector2.Zero, 200f, 50f);
            int randomIndex = random.nextInt(tiles.size() + 1);
            tiles.add(randomIndex, tile);

        }
    }

    @Override
    public void roundEndEffect() {
        List<Tile> tiles = Roulette.getInstance().getRunState().getTiles();
        tiles.removeIf(tile -> tile.getType() instanceof NullTile);
    }
}

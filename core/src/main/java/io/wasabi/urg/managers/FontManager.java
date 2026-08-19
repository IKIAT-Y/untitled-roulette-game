package io.wasabi.urg.managers;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import io.wasabi.urg.Roulette;

public class FontManager {
    private static final FontManager INSTANCE = new FontManager();

    private Roulette game;

    private boolean initialized;

    // Fonts
    private Map<String, String> fontPaths = new HashMap<String, String>() {{
        put("Placeholder", "fonts/placeholder.fnt");
    }};

    private Map<String, BitmapFont> fonts = new HashMap<>();

    private FontManager() {}

    public static FontManager getInstance() {
        return INSTANCE;
    }

    public void initialize(Roulette game) {
        if (initialized) return;
        initialized = true;

        this.game = game;

        // Initialize fonts
        for (Map.Entry<String, String> entry : fontPaths.entrySet()) {
            BitmapFont font = new BitmapFont(Gdx.files.internal(entry.getValue()));
            // Linear filtering keeps glyphs readable when a caller scales this font
            // down (e.g. BettingTable shrinks it to fit inside a pocket cell) — set
            // once here rather than repeatedly by every draw call that uses it.
            font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
            fonts.put(entry.getKey(), font);
        }
    }

    // Get functions
    public BitmapFont getFontByName(String name) {
        for (Map.Entry<String, BitmapFont> entry : fonts.entrySet()) {
            if (name.equals(entry.getKey())) return entry.getValue();
        }
        return null;
    }
}

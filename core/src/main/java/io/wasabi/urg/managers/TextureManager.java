package io.wasabi.urg.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static final TextureManager INSTANCE = new TextureManager();

    private static final String CARD_DIR = "cards/";

    private final Map<String, Texture> cardTextures = new HashMap<>();
    private boolean initialized;

    private TextureManager() {}

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        // May be useful for preloading textures in the future
    }

    public Texture getCardTexture(String name) {
        Texture texture = cardTextures.get(name);
        if (texture != null) {
            return texture;
        }

        FileHandle file = Gdx.files.internal(CARD_DIR + name + ".png");
        if (!file.exists()) {
            throw new IllegalArgumentException("No card texture found for: " + name
                + " (expected assets/" + CARD_DIR + name + ".png)");
        }

        texture = new Texture(file);
        cardTextures.put(name, texture);
        return texture;
    }

    public void dispose() {
        for (Texture texture : cardTextures.values()) {
            texture.dispose();
        }
        cardTextures.clear();
    }
}

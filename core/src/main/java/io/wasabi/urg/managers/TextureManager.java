package io.wasabi.urg.managers;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

public class TextureManager {
    private static final TextureManager INSTANCE = new TextureManager();

    // store item type and directory
    private HashMap<String, String> ITEM_MAPPER = new HashMap<>();

    private final Map<String, Texture> textures = new HashMap<>();
    private boolean initialized;

    private TextureManager() {
        ITEM_MAPPER.put("card", "cards/");
        ITEM_MAPPER.put("charm", "charms/");
    }

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        // May be useful for preloading textures in the future
    }

    public Texture getTexture(String name, String itemType) {
        String dir = ITEM_MAPPER.get(itemType);

        if (dir == null) {
            throw new IllegalArgumentException("Invalid item type!");
        }

        Texture texture = textures.get(name);
        if (texture != null) {
            return texture;
        }

        FileHandle file = Gdx.files.internal(dir + name + ".png");
        if (!file.exists()) {
            throw new IllegalArgumentException("No card texture found for: " + name
                + " (expected assets/" + dir + name + ".png)");
        }

        texture = new Texture(file);
        textures.put(name, texture);
        return texture;
    }

    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
    }
}

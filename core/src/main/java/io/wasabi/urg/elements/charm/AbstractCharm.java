package io.wasabi.urg.elements.charm;

import com.badlogic.gdx.graphics.Texture;

import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.managers.TextureManager;
import io.wasabi.urg.util.tweens.Tween;

public class AbstractCharm extends GameObject{

    private Texture sprite;
    private float x, y;
    private float width, height;
    private boolean dragging = false;
    private float targetX, targetY;
    private boolean hasTarget = false;

    private Tween tweenX;
    private Tween tweenY;
    private static final float SNAP_DURATION = 0.25f;

    public AbstractCharm() {
        this.x = 0;
        this.y = 0;
        this.width = 32;
        this.height = 32;

        loadSprite();
    }

    /*
     * Load the sprite for this card from the TextureManager.
     * The card's class name is used to determine the texture file name.
     * So use the class name of the card as the texture file name (without the .png extension).
     */
    private void loadSprite() {
        this.sprite = TextureManager.getInstance().getTexture(getClass().getSimpleName(), "charm");
    }
    
    public void update(float delta) {

    }

    public void render() {

    }

    public void dispose() {

    }
}

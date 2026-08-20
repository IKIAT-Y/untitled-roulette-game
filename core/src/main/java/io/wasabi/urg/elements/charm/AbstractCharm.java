package io.wasabi.urg.elements.charm;

import com.badlogic.gdx.graphics.Texture;

import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.TextureManager;
import io.wasabi.urg.util.tweens.Tween;

public class AbstractCharm extends GameObject{

    private Texture texture;
    private float x, y;
    private float width, height;
    private boolean dragging = false;
    private float targetX, targetY;
    private boolean hasTarget = false;

    private Tween tweenX;
    private Tween tweenY;
    private static final float SNAP_DURATION = 0.25f;

    protected AbstractCharm() {
        this.x = 0;
        this.y = 0;
        this.width = 64;
        this.height = 64;

        loadSprite();
    }

    /*
     * Load the sprite for this card from the TextureManager.
     * The card's class name is used to determine the texture file name.
     * So use the class name of the card as the texture file name (without the .png extension).
     */
    private void loadSprite() {
        this.texture = TextureManager.getInstance().getTexture(getClass().getSimpleName(), "charm");
    }
    
    public void roundStartEffect() {}
    public void beforeSpinEffect() {}
    public void afterSpinEffect() {}
    public void roundEndEffect() {}

    public void update(float delta) {
        if (dragging) return;

        if (tweenX != null && !tweenX.isComplete()) {
            x = tweenX.update(delta);
        }
        if (tweenY != null && !tweenY.isComplete()) {
            y = tweenY.update(delta);
        }
    }

    @Override
    public void render() {
        RendererManager.getInstance().getSpriteBatch().draw(texture, x, y, width, height);
    }

    public boolean contains(float worldX, float worldY) {
        return worldX >= x && worldX <= x + width
            && worldY >= y && worldY <= y + height;
    }

    public void setTargetPosition(float newTargetX, float newTargetY) {
        if (hasTarget
            && Math.abs(newTargetX - targetX) < 0.01f
            && Math.abs(newTargetY - targetY) < 0.01f) {
            return;
        }

        hasTarget = true;
        targetX = newTargetX;
        targetY = newTargetY;
        tweenX = new Tween(SNAP_DURATION, x, targetX, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
        tweenY = new Tween(SNAP_DURATION, y, targetY, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isDragging() { return dragging; }

    public void setDragging(boolean dragging) {
        boolean wasDragging = this.dragging;
        this.dragging = dragging;

        if (wasDragging && !dragging) {
            hasTarget = false;
        }
        
    }
}

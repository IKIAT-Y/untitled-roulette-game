package io.wasabi.urg.elements.card;

import com.badlogic.gdx.graphics.Texture;

import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.TextureManager;
import io.wasabi.urg.util.tweens.Tween;

public abstract class Card extends GameObject {

    public enum Rarity {
        // price in tickets
        COMMON(10), UNCOMMON(15), RARE(20);

        private final int price;

        Rarity(int price) { this.price = price; }

        public int getPrice() { return price; }
    }

    protected Rarity cardRarity;
    private Texture sprite;
    private float x, y;
    private float width, height;
    private boolean dragging = false;
    private float targetX, targetY;
    private boolean hasTarget = false;

    private Tween tweenX;
    private Tween tweenY;
    private static final float SNAP_DURATION = 0.25f;

    protected Card(Rarity rarity) {
        this.cardRarity = rarity;

        this.x = 0;
        this.y = 0;
        this.width = 96;
        this.height = 128;

        loadSprite();
    }

    /*
     * Load the sprite for this card from the TextureManager.
     * The card's class name is used to determine the texture file name.
     * So use the class name of the card as the texture file name (without the .png extension).
     */
    private void loadSprite() {
        this.sprite = TextureManager.getInstance().getTexture(getClass().getSimpleName(), "card");
    }

    public void roundStartEffect() {}
    public void beforeSpinEffect() {}
    public void afterSpinEffect() {}
    public void roundEndEffect() {}
    public float getPayoutMultiplier(Tile winningTile, int totalStaked, int chipBalance) { return 1f; }
    public int getEffectTriggerMultiplier() { return 1; }
    public void afterCardEffects(String effectType) {}
    public void removedEffect() {}

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
        RendererManager.getInstance().getSpriteBatch().draw(sprite, x, y, width, height);
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
    public Rarity getRarity() { return cardRarity; }
    public int getPrice() { return cardRarity.getPrice(); }
    public int getSellPrice() { return getPrice() / 2; }
    public String getDisplayName() { return getClass().getSimpleName(); }
    public boolean isDragging() { return dragging; }

    public void setDragging(boolean dragging) {
        boolean wasDragging = this.dragging;
        this.dragging = dragging;

        if (wasDragging && !dragging) {
            hasTarget = false;
        }
    }
}

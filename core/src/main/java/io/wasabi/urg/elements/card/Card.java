package io.wasabi.urg.elements.card;

import io.wasabi.urg.elements.GameObject;

public abstract class Card extends GameObject {

    protected enum Rarity {
        COMMON, UNCOMMON, RARE
    }

    protected Rarity cardRarity;

    protected Card(Rarity rarity) {
        this.cardRarity = rarity;
    }

    public void roundStartEffect() {
        // Default implementation does nothing
    }

    public void beforeSpinEffect() {
        // Default implementation does nothing
    }

    public void afterSpinEffect() {
        // Default implementation does nothing
    }

    public void roundEndEffect() {
        // Default implementation does nothing
    }

    public void render() {
        // Default implementation does nothing
    }
}

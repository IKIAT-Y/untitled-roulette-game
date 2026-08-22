package io.wasabi.urg.elements;

/** A game element that can be updated, rendered, and disposed by the GameManager. */
public abstract class GameObject {

    /** Advances this object's state. Objects without changing state can use this default. */
    public void update(float delta) {
    }

    /** Draws this object with the renderer shared by the GameManager. */
    public abstract void render();

    /** Releases resources owned by this object. */
    public void dispose() {
    }
}

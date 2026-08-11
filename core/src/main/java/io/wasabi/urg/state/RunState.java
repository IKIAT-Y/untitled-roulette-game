package io.wasabi.urg.state;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import io.wasabi.urg.elements.GameObject;

/** Stores the player's progress and inventory for the current run. */
public final class RunState {
    private int chips;
    private int score;

    private final Array<GameObject> tiles = new Array<>();
    private final Array<GameObject> ownedCards = new Array<>();
    private final Array<GameObject> ownedCharms = new Array<>();
    private final IntArray chipHistory = new IntArray();

    public int getChips() {
        return chips;
    }

    public void addChips(int amount) {
        requireNonNegative(amount, "amount");
        chips += amount;
    }

    public boolean spendChips(int amount) {
        requireNonNegative(amount, "amount");
        if (amount > chips) {
            return false;
        }

        chips -= amount;
        return true;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int amount) {
        requireNonNegative(amount, "amount");
        score += amount;
    }

    public void addTile(GameObject tile) {
        addUnique(tiles, tile);
    }

    public boolean removeTile(GameObject tile) {
        return tiles.removeValue(tile, true);
    }

    public Array<GameObject> getTiles() {
        return new Array<>(tiles);
    }

    public void addCard(GameObject card) {
        addUnique(ownedCards, card);
    }

    public boolean removeCard(GameObject card) {
        return ownedCards.removeValue(card, true);
    }

    public boolean ownsCard(GameObject card) {
        return ownedCards.contains(card, true);
    }

    public Array<GameObject> getOwnedCards() {
        return new Array<>(ownedCards);
    }

    public void addCharm(GameObject charm) {
        addUnique(ownedCharms, charm);
    }

    public boolean removeCharm(GameObject charm) {
        return ownedCharms.removeValue(charm, true);
    }

    public boolean ownsCharm(GameObject charm) {
        return ownedCharms.contains(charm, true);
    }

    public Array<GameObject> getOwnedCharms() {
        return new Array<>(ownedCharms);
    }

    // This part is for the end of round graph.
    public void recordRoundBalance() {
        chipHistory.add(chips);
    }

    public IntArray getChipHistory() {
        return new IntArray(chipHistory);
    }

    public void reset(int startingChips) {
        requireNonNegative(startingChips, "startingChips");

        chips = startingChips;
        score = 0;
        tiles.clear();
        ownedCards.clear();
        ownedCharms.clear();
        chipHistory.clear();
        chipHistory.add(startingChips);
    }

    private void addUnique(Array<GameObject> collection, GameObject gameObject) {
        if (gameObject == null) {
            throw new IllegalArgumentException("gameObject cannot be null");
        }
        if (!collection.contains(gameObject, true)) {
            collection.add(gameObject);
        }
    }

    private void requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
    }
}

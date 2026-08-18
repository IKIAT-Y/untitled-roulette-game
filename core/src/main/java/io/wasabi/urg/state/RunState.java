package io.wasabi.urg.state;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.IntArray;

import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.game.Tile;

/** Stores the player's progress and inventory for the current run. */
public final class RunState {
    private int chips;
    private int score;
    private int tickets;

    private Tile lastTile = null; // The last tile the player landed on, used for certain card effects.

    private final List<Tile> tiles = new ArrayList<>();
    private final List<Card> ownedCards = new ArrayList<>();
    private final List<GameObject> ownedCharms = new ArrayList<>();
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

    public int getTickets() {
        return tickets;
    }

    public void addTickets(int amount) {
        requireNonNegative(amount, "amount");
        tickets += amount;
    }

    public boolean spendTickets(int amount) {
        requireNonNegative(amount, "amount");
        if (amount > tickets) {
            return false;
        }

        tickets -= amount;
        return true;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int amount) {
        requireNonNegative(amount, "amount");
        score += amount;
    }

    public void addTile(Tile tile) {
        addUnique(tiles, tile);
    }

    public boolean removeTile(Tile tile) {
        if (tiles.contains(tile)) {
            return tiles.remove(tile);
        }
        return false;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void addCard(Card card) {
        addUnique(ownedCards, card);
    }

    public boolean removeCard(Card card) {
        if (ownsCard(card)) {
            return ownedCards.remove(card);
        }
        return false;
    }

    public boolean ownsCard(Card card) {
        return ownedCards.contains(card);
    }

    public List<Card> getOwnedCards() {
        return ownedCards;
    }

    public void addCharm(GameObject charm) {
        addUnique(ownedCharms, charm);
    }

    public boolean removeCharm(GameObject charm) {
        if (ownsCharm(charm)) {
            return ownedCharms.remove(charm);
        }
        return false;
    }

    public boolean ownsCharm(GameObject charm) {
        return ownedCharms.contains(charm);
    }

    public List<GameObject> getOwnedCharms() {
        return ownedCharms;
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
        tickets = 0;
        tiles.clear();
        ownedCards.clear();
        ownedCharms.clear();
        chipHistory.clear();
        chipHistory.add(startingChips);
    }

    private <T> void addUnique(List<T> collection, T gameObject) {
        if (gameObject == null) {
            throw new IllegalArgumentException("gameObject cannot be null");
        }
        if (!collection.contains(gameObject)) {
            collection.add(gameObject);
        }
    }

    private void requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
    }

    public void setLastTile(Tile tile) { this.lastTile = tile; }

    public Tile getLastTile() { return lastTile; }

    public void triggerCardEffects(String effectType) {
        switch (effectType) {
            case "roundStart":
                for (Card card : ownedCards) {
                    card.roundStartEffect();
                }
                break;
            case "beforeSpin":
                for (Card card : ownedCards) {
                    card.beforeSpinEffect();
                }
                break;
            case "afterSpin":
                for (Card card : ownedCards) {
                    card.afterSpinEffect();
                }
                break;
            case "roundEnd":
                for (Card card : ownedCards) {
                    card.roundEndEffect();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown effect type: " + effectType);
        }
    }
}

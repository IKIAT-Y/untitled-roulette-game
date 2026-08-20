package io.wasabi.urg.state;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.IntArray;

import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.elements.betting.Bet;
import io.wasabi.urg.elements.boss.Boss;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.charm.AbstractCharm;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.ui.Tooltip;

/** Stores the player's progress and inventory for the current run. */
public final class RunState {
    public static final int MAX_OWNED_CARDS = 4;
    public static final int MAX_OWNED_CHARMS = 4;
    private int chips;
    private int score;
    private int tickets;

    private Tile lastTile = null; // The last tile the player landed on, used for certain card effects.
    private Tooltip activeTooltip = null;

    private final List<Tile> tiles = new ArrayList<>();
    private final List<Card> ownedCards = new ArrayList<>();
    private final List<AbstractCharm> ownedCharms = new ArrayList<>();
    private final IntArray chipHistory = new IntArray();

    private Boss boss = null; // The current boss for the run, if any.

    // Must live here to persist across screens.
    private final List<Bet> activeBets = new ArrayList<>();

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        requireNonNegative(chips, "chips");
        this.chips = chips;
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

    public boolean addCard(Card card) {
        if (card == null || ownedCards.size() >= MAX_OWNED_CARDS || ownsCardType(card)) {
            return false;
        }
        ownedCards.add(card);
        return true;
    }

    public boolean removeCard(Card card) {
        if (ownsCard(card)) {
            card.removedEffect();
            return ownedCards.remove(card);
        }
        return false;
    }

    public void reorderCard(Card card, int newIndex) {
        if (!ownedCards.contains(card)) {
            return;
        }
        ownedCards.remove(card);
        newIndex = Math.max(0, Math.min(newIndex, ownedCards.size()));
        ownedCards.add(newIndex, card);
    }

    public boolean ownsCard(Card card) {
        return ownedCards.contains(card);
    }

    public boolean ownsCardType(Card card) {
        if (card == null) {
            return false;
        }
        for (Card ownedCard : ownedCards) {
            if (ownedCard.getClass() == card.getClass()) {
                return true;
            }
        }
        return false;
    }

    public boolean canAddCard(Card card) {
        return ownedCards.size() < MAX_OWNED_CARDS && !ownsCardType(card);
    }

    public List<Card> getOwnedCards() {
        return ownedCards;
    }

    // charms allow duplicates
    public void addCharm(AbstractCharm charm) {
        if (ownedCharms.size() + 1 <= MAX_OWNED_CHARMS) {
            ownedCharms.add(charm);
        }
    }

    public void reorderCharm(AbstractCharm charm, int newIndex) {
        if (!ownedCharms.contains(charm)) {
            return;
        }
        ownedCharms.remove(charm);
        newIndex = Math.max(0, Math.min(newIndex, ownedCharms.size()));
        ownedCharms.add(newIndex, charm);
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

    public List<AbstractCharm> getOwnedCharms() {
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
        lastTile = null;
        activeTooltip = null;
        boss = null;
        activeBets.clear();
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

    public void setLastTile(Tile tile) {
        this.lastTile = tile;
    }

    public Tile getLastTile() {
        return lastTile;
    }

    public void setTooltip(Tooltip tooltip) {
        this.activeTooltip = tooltip;
    }

    public Tooltip getActiveTooltip() {
        return activeTooltip;
    }

    public void addBet(Bet bet) {
        activeBets.add(bet);
    }

    public boolean removeBet(Bet bet) {
        return activeBets.remove(bet);
    }

    public List<Bet> getActiveBets() {
        return activeBets;
    }

    /**
     * Drops every pending bet without touching {@link #chips} — placing a bet never
     * deducts chips (see {@link io.wasabi.urg.elements.game.BettingTable#placeBet}),
     * so there's nothing to refund here.
     */
    public void clearActiveBets() {
        activeBets.clear();
    }

    /**
     * Settles every pending bet against the winning tile. This is the ONE place
     * chips actually change hands for a bet: stakes are reserved-but-not-deducted
     * while betting is open (see
     * {@link io.wasabi.urg.elements.game.BettingTable#placeBet}), so every stake is
     * subtracted here in bulk before winners' payouts (which already include their
     * returned stake — see {@link Bet#payout}) are added back.
     */
    public int resolveActiveBets() {
        if (lastTile == null) {
            return 0;
        }

        int totalStaked = 0;
        int totalPayout = 0;
        for (Bet bet : activeBets) {
            totalStaked += bet.getAmount();
            totalPayout += bet.payout(lastTile);
        }

        float payoutMultiplier = lastTile.getBetMultiplier();
        int triggerCount = getCardEffectTriggerCount();
        for (int trigger = 0; trigger < triggerCount; trigger++) {
            for (Card card : ownedCards) {
                payoutMultiplier *= card.getPayoutMultiplier(lastTile, totalStaked, chips);
            }
        }
        totalPayout = Math.round(totalPayout * payoutMultiplier);

        chips = chips - totalStaked + totalPayout;
        activeBets.clear();
        return totalPayout;
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
    }

    public Boss getBoss() {
        return boss;
    }

    public void triggerEffects(String effectType) {
        int triggerCount = getCardEffectTriggerCount();

        for (int trigger = 0; trigger < triggerCount; trigger++) {
            for (Card card : ownedCards) {
                switch (effectType) {
                    case "roundStart":
                        card.roundStartEffect();
                        if (boss != null) {
                            boss.roundStartEffect();
                        }
                        break;
                    case "beforeSpin":
                        card.beforeSpinEffect();
                        if (boss != null) {
                            boss.beforeSpinEffect();
                        }
                        break;
                    case "afterSpin":
                        card.afterSpinEffect();
                        if (boss != null) {
                            boss.afterSpinEffect();
                        }
                        break;
                    case "roundEnd":
                        card.roundEndEffect();
                        if (boss != null) {
                            boss.roundEndEffect();
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown effect type: " + effectType);
                }
            }
        }

        for (Card card : ownedCards) {
            card.afterCardEffects(effectType);
        }
    }

    private int getCardEffectTriggerCount() {
        int additionalTriggers = 0;
        int triggerMultiplier = 1;
        for (Card card : ownedCards) {
            additionalTriggers += card.getAdditionalEffectTriggers();
            triggerMultiplier *= card.getEffectTriggerMultiplier();
        }
        return (1 + additionalTriggers) * triggerMultiplier;
    }
}

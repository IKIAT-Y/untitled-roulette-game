package io.wasabi.urg.managers;

import java.util.ArrayList;
import java.util.List;

import io.wasabi.urg.elements.card.AllIn;
import io.wasabi.urg.elements.card.BlackCard;
import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.card.ExtraChange;
import io.wasabi.urg.elements.card.ExtraCredit;
import io.wasabi.urg.elements.card.FourLeafClover;
import io.wasabi.urg.elements.card.GoldenTicket;
import io.wasabi.urg.elements.card.GreenCard;
import io.wasabi.urg.elements.card.Infinite;
import io.wasabi.urg.elements.card.Jackpot;
import io.wasabi.urg.elements.card.LuckyTalisman;
import io.wasabi.urg.elements.card.OddCard;
import io.wasabi.urg.elements.card.Oneshot;
import io.wasabi.urg.elements.card.Ouroboros;

/** Owns the available cards and handles rarity-weighted shop draws. */
public final class CardPool {
    private final List<Card> commonCards = new ArrayList<>();
    private final List<Card> uncommonCards = new ArrayList<>();
    private final List<Card> rareCards = new ArrayList<>();

    public CardPool() {
        commonCards.add(new ExtraChange());
        commonCards.add(new ExtraCredit());
        commonCards.add(new BlackCard());
        commonCards.add(new GreenCard());
        commonCards.add(new OddCard());
        commonCards.add(new Jackpot());
        commonCards.add(new GoldenTicket());

        uncommonCards.add(new AllIn());
        uncommonCards.add(new FourLeafClover());
        uncommonCards.add(new LuckyTalisman());
        uncommonCards.add(new Ouroboros());

        rareCards.add(new Infinite());
        rareCards.add(new Oneshot());
    }

    public Card getRandomCard() {
        double roll = Math.random();
        if (roll < 0.05 && !rareCards.isEmpty()) {
            return drawFrom(rareCards);
        }
        if (roll < 0.3 && !uncommonCards.isEmpty()) {
            return drawFrom(uncommonCards);
        }
        if (!commonCards.isEmpty()) {
            return drawFrom(commonCards);
        }
        if (!uncommonCards.isEmpty()) {
            return drawFrom(uncommonCards);
        }
        return drawFrom(rareCards);
    }

    public void returnCard(Card card) {
        if (card == null) {
            return;
        }

        List<Card> pool;
        switch (card.getRarity()) {
            case COMMON:
                pool = commonCards;
                break;
            case UNCOMMON:
                pool = uncommonCards;
                break;
            case RARE:
                pool = rareCards;
                break;
            default:
                throw new IllegalStateException("Unknown card rarity");
        }

        if (!pool.contains(card)) {
            pool.add(card);
        }
    }

    private Card drawFrom(List<Card> pool) {
        if (pool.isEmpty()) {
            return null;
        }
        return pool.remove((int) (Math.random() * pool.size()));
    }
}

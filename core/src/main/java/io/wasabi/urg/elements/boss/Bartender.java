package io.wasabi.urg.elements.boss;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.betting.Bet;

import java.util.List;

public class Bartender extends Boss {

    public Bartender() {
        super("The Bartender", "He wants you to buy a drink.", "Upon spinning, lose half your unspent money.");
    }

    @Override
    public void beforeSpinEffect() {
        // Lose half of the player's unspent money
        int currentChips = Roulette.getInstance().getRunState().getChips();

        List<Bet> activeBets = Roulette.getInstance().getRunState().getActiveBets();
        for (Bet bet : activeBets) {
            currentChips -= bet.getAmount();
        }

        int chipsToLose = currentChips / 2;
        Roulette.getInstance().getRunState().spendChips(chipsToLose);
    }
}

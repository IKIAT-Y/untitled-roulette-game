package io.wasabi.urg.managers;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.state.RunState;

public class RoundManager {
    // Change as needed
    private static final int SPINS_PER_ROUND = 5;
    private static final int ROUNDS_PER_ACT = 5;
    private static final int TOTAL_ACTS = 3;
    private static final int BASE_TICKET_REWARD = 20;
    private static final int TICKETS_PER_UNUSED_SPIN = 2;

    private int act = 1;
    private int round = 1;
    private RoundConfig currentConfig;
    private int spinsRemaining = 5;
    private boolean gameOver;
    private boolean runComplete;
    private final RunState runState;

    public RoundManager(RunState runState) {
        this.runState = runState;
        this.currentConfig = buildConfig();
    }

    private RoundConfig buildConfig() {
        int quota = QuotaCalculator.calculate(act, round);

        // Boss Round
        if (round == ROUNDS_PER_ACT) {
            return new RoundConfig(act, round, true, quota, 1f, spinsRemaining);
        }

        // Normal Round
        return new RoundConfig(act, round, false, quota, 1f, spinsRemaining);
    }

    public void startRound() {
        if (gameOver || runComplete) {
            return;
        }

        spinsRemaining = SPINS_PER_ROUND;
        currentConfig = buildConfig();
        runState.triggerCardEffects("roundStart");
        printQuotaStatus();
    }

    public void recordSpin() {
        if (gameOver || runComplete || spinsRemaining <= 0) {
            return;
        }

        spinsRemaining--;
        runState.triggerCardEffects("afterSpin");
        printQuotaStatus();

        // Temp Debug for checking quota and spins remaining
        System.out.println("Act: " + act + ", Round: " + round + ", Quota: " + currentConfig.getQuota() + ", Chips: " + runState.getChips() + ", Boss round: " + currentConfig.isBossRound());
        System.out.println("Spins remaining: " + spinsRemaining);

        if (runState.getChips() >= currentConfig.getQuota()) {
            System.out.println("winner");
            advance();
        } else if (spinsRemaining <= 0) {
            System.out.println("loser");
            gameOver();
        }
    }

    public void advance() {
        runState.triggerCardEffects("roundEnd");
        runState.recordRoundBalance();

        if (round == ROUNDS_PER_ACT && act == TOTAL_ACTS) {
            runComplete = true;
            System.out.println("Run complete: final quota reached.");
            return;
        }

        // Reset tile multiplier for the next round
        Roulette.getInstance().getScreen().getWheel().resetTileMultipliers();

        if (round == ROUNDS_PER_ACT) {
            act++;
            round = 1;
        } else {
            round++;
        }

        Roulette.getInstance().getScreen().enterResultScreen(
            runState.getChips(),
            currentConfig.getQuota(),
            BASE_TICKET_REWARD,
            spinsRemaining*TICKETS_PER_UNUSED_SPIN,
            BASE_TICKET_REWARD + (spinsRemaining*TICKETS_PER_UNUSED_SPIN)
        );
    }

    public void gameOver() {
        gameOver = true;
        System.out.println("Game over: quota not reached.");
    }
    // Temporary fixed numbers we will have to change depending on how much we are planning to make the upgrades.
    public void awardTickets() {
        int ticketsAwarded = BASE_TICKET_REWARD
            + (spinsRemaining * TICKETS_PER_UNUSED_SPIN);
        runState.addTickets(ticketsAwarded);
        System.out.println(
            "Quota complete: awarded " + ticketsAwarded
                + " tickets. Total tickets: " + runState.getTickets()
        );
    }

// temporary way to check whether the quota has been reached or not, will be replaced with a proper UI later
    private void printQuotaStatus() {
        System.out.println(
            "Act " + act
                + "\nRound " + round
                + "\nBoss round: " + currentConfig.isBossRound()
                + "\nQuota: " + runState.getChips() + " / " + currentConfig.getQuota()
                + "\nTickets: " + runState.getTickets()
        );
    }

    public RoundConfig getCurrentConfig() { return currentConfig; }
    public int getSpinsRemaining() { return spinsRemaining; }
    public void setSpinsRemaining(int spinsRemaining) { this.spinsRemaining = spinsRemaining; }
    public int getAct() { return act; }
    public int getRound() { return round; }
    public boolean isGameOver() { return gameOver; }
    public boolean isRunComplete() { return runComplete; }
}

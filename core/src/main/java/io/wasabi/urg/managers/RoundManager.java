package io.wasabi.urg.managers;

import io.wasabi.urg.state.RunState;

public class RoundManager {
    // Change as needed
    private static final int SPINS_PER_ROUND = 5;
    private static final int ROUNDS_PER_ACT = 5;
    private static final int TOTAL_ACTS = 3;

    private int act = 1;
    private int round = 1;
    private RoundConfig currentConfig;
    private int spinsRemaining = 5;
    private final RunState runState;

    public RoundManager(RunState runState) {
        this.runState = runState;
        this.currentConfig = buildConfig();
    }

    private RoundConfig buildConfig() {
        int quota = QuotaCalculator.calculate(act, round);

        // Boss Round
        if (round == ROUNDS_PER_ACT) {
            return new RoundConfig(act, round, true, quota, 2f, spinsRemaining);
        }

        // Normal Round
        return new RoundConfig(act, round, false, quota, 1f, spinsRemaining);
    }

    public void startRound() {
        spinsRemaining = SPINS_PER_ROUND;
        currentConfig = buildConfig();
        runState.triggerCardEffects("roundStart");
    }

    public void recordSpin() {
        spinsRemaining--;
        runState.triggerCardEffects("afterSpin");

        // Temp Debug
        System.out.println("Act: " + act + ", Round: " + round + ", Quota: " + currentConfig.getQuota() + ", Chips: " + runState.getChips());
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

        if (round == ROUNDS_PER_ACT) {
            act++;
            round = 1;
        } else {
            round++;
        }
    }

    public void gameOver() {
        // Handle game over logic here
    }

    public RoundConfig getCurrentConfig() { return currentConfig; }
    public int getSpinsRemaining() { return spinsRemaining; }
    public int getAct() { return act; }
    public int getRound() { return round; }
}

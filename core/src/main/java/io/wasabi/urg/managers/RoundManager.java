package io.wasabi.urg.managers;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.boss.Bartender;
import io.wasabi.urg.elements.boss.Boss;
import io.wasabi.urg.elements.boss.Gamer;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.state.RunState;

import java.util.ArrayList;
import java.util.List;

public class RoundManager {
    // Change as needed
    private static final int SPINS_PER_ROUND = 5;
    private static final int ROUNDS_PER_ACT = 5;
    private static final int TOTAL_ACTS = 3;
    private static final int BASE_TICKET_REWARD = 20;
    private static final int TICKETS_PER_UNUSED_SPIN = 2;
    private static final int STARTING_CHIPS = 100;

    private int act = 1;
    private int round = 1;
    private RoundConfig currentConfig;
    private int spinsRemaining = 5;
    private boolean gameOver;
    private boolean runComplete;
    private final RunState runState;

    private final List<Boss> act1Bosses = new ArrayList<Boss>();

    public RoundManager(RunState runState) {
        this.runState = runState;
        this.currentConfig = buildConfig();
        initializeBossPool();
    }

    public void reset() {
        act = 1;
        round = 1;
        spinsRemaining = SPINS_PER_ROUND;
        gameOver = false;
        runComplete = false;
    }

    private void initializeBossPool() {
        act1Bosses.clear();

        // Act 1
        act1Bosses.add(new Bartender());
        act1Bosses.add(new Gamer());
    }

    private Boss selectRandomBossForAct(int act) {
        List<Boss> bossPool;
        if (act == 1) {
            bossPool = act1Bosses;
        } else {
            bossPool = new ArrayList<>();
        }

        if (!bossPool.isEmpty()) {
            int randomIndex = (int) (Math.random() * bossPool.size());
            Boss selectedBoss = bossPool.get(randomIndex);
            bossPool.remove(randomIndex);
            return selectedBoss;
        } else {
            return null;
        }
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

        if (currentConfig.isBossRound()) {
            Boss boss = selectRandomBossForAct(act);
            runState.setBoss(boss);
        } else {
            runState.setBoss(null);
        }

        runState.setChips(STARTING_CHIPS);
        runState.triggerEffects("roundStart");
        printQuotaStatus();
    }

    public void recordSpin(boolean freeSpin) {
        if (gameOver || runComplete || (!freeSpin && spinsRemaining <= 0)) {
            return;
        }

        if (!freeSpin) {
            spinsRemaining--;
        }

        Tile lastTile = runState.getLastTile();
        if (lastTile != null) {
            lastTile.onLanded();
        }

        Roulette.getInstance().getRunState().triggerEffects("afterSpin");

        printQuotaStatus();

        // Temp Debug for checking quota and spins remaining
        System.out.println("Act: " + act + ", Round: " + round + ", Quota: " + currentConfig.getQuota() + ", Chips: " + runState.getChips() + ", Boss round: " + currentConfig.isBossRound());
        System.out.println("Spins remaining: " + spinsRemaining);

        if (runState.getChips() >= currentConfig.getQuota()) {
            // System.out.println("winner");
            advance();
        } else if (spinsRemaining <= 0 || runState.getChips() == 0) {
            // System.out.println("loser");
            gameOver();
        }
    }

    public void advance() {
        runState.triggerEffects("roundEnd");
        runState.recordRoundBalance();

        // Reset tile multiplier for the next round
        Roulette.getInstance().getGameScreen().getWheel().resetTileMultipliers();
        if (round == ROUNDS_PER_ACT) {
            act++;
            round = 1;
        } else {
            round++;
        }

        Roulette.getInstance().getGameScreen().enterResultScreen(
            runState.getChips(),
            currentConfig.getQuota(),
            BASE_TICKET_REWARD,
            spinsRemaining*TICKETS_PER_UNUSED_SPIN,
            BASE_TICKET_REWARD + (spinsRemaining*TICKETS_PER_UNUSED_SPIN)
        );
    }

    public void gameOver() {
        gameOver = true;
        // System.out.println("Game over: quota not reached.");
        if (Roulette.getInstance().getGameScreen() != null) {
            Roulette.getInstance().getGameScreen().showGameOver();
        }
    }
    // Temporary fixed numbers we will have to change depending on how much we are planning to make the upgrades.
    public void awardTickets() {
        int ticketsAwarded = BASE_TICKET_REWARD
            + (spinsRemaining * TICKETS_PER_UNUSED_SPIN);
        runState.addTickets(ticketsAwarded);
        // System.out.println(
        //     "Quota complete: awarded " + ticketsAwarded
        //         + " tickets. Total tickets: " + runState.getTickets()
        // );
    }

// temporary way to check whether the quota has been reached or not, will be replaced with a proper UI later
    private void printQuotaStatus() {
        // System.out.println(
        //     "Act " + act
        //         + "\nRound " + round
        //         + "\nBoss round: " + currentConfig.isBossRound()
        //         + "\nQuota: " + runState.getChips() + " / " + currentConfig.getQuota()
        //         + "\nTickets: " + runState.getTickets()
        // );
    }

    public RoundConfig getCurrentConfig() { return currentConfig; }
    public int getSpinsRemaining() { return spinsRemaining; }
    public void setSpinsRemaining(int spinsRemaining) { this.spinsRemaining = spinsRemaining; }
    public int getAct() { return act; }
    public void setAct(int act) { this.act = act; }
    public void setRound(int round) { this.round = round; }
    public int getRound() { return round; }
    public int getOverallRoundNumber() { return ((act - 1) * ROUNDS_PER_ACT) + round; }
    public boolean isGameOver() { return gameOver; }
    public boolean isRunComplete() { return runComplete; }
}

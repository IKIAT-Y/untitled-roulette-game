package io.wasabi.urg.managers;

public final class QuotaCalculator {
    // We can change this as we see fit, but this is the starting point for the quota calculation.
    private static final int STARTING_QUOTA = 120;
    private static final int ROUNDS_PER_ACT = 5;

    private QuotaCalculator() {
    }

    public static int calculate(int act, int round) {
        if (act < 1 || round < 1 || round > ROUNDS_PER_ACT) {
            throw new IllegalArgumentException("act and round must describe a valid round");
        }
            // for now th quota increases exponentially with each round. 
        int completedRounds = ((act - 1) * ROUNDS_PER_ACT) + (round - 1);
        return STARTING_QUOTA * (1 << completedRounds);
    }
}

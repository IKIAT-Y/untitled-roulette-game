package io.wasabi.urg.managers;

public final class QuotaCalculator {
    // We can change this as we see fit, but this is the starting point for the quota calculation.
    private static final int STARTING_QUOTA = 120;
    private static final int ROUNDS_PER_ACT = 5;

    static int[] actBaseQuotas = {100, 300, 1000}; // Base quotas for each act
    static float[] roundMultipliers = {1.25f, 1.5f, 2.0f, 2.75f, 3.0f}; // Multipliers for each round
    private QuotaCalculator() {
    }

    public static int calculate(int act, int round) {
        if (act < 1 || round < 1 || round > ROUNDS_PER_ACT) {
            throw new IllegalArgumentException("act and round must describe a valid round");
        }
        return (int) (actBaseQuotas[act - 1] * roundMultipliers[round - 1]);
    }
}

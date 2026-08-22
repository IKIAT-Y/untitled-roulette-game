package io.wasabi.urg.util.tweens.formulae;

import io.wasabi.urg.util.tweens.TweenFormula;

public class QuadInOut implements TweenFormula {
    @Override
    public float get(float x) {
        return x < 0.5f ? 2 * x * x : 1 - (float)Math.pow(-2 * x + 2, 2) / 2;
    }
}

package io.wasabi.urg.util.tweens.formulae;

import io.wasabi.urg.util.tweens.TweenFormula;

public class CircularOut implements TweenFormula {
    @Override
    public float get(float x) {
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
    }
}

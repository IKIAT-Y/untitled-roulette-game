package io.wasabi.urg.util.tweens.formulae;

import io.wasabi.urg.util.tweens.TweenFormula;

public class QuadOut implements TweenFormula {
    @Override
    public float get(float x) {
        return 1 - (1 - x) * (1 - x);
    }
}

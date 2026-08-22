package io.wasabi.urg.util.tweens.formulae;

import io.wasabi.urg.util.tweens.TweenFormula;

public class QuadIn implements TweenFormula {
    @Override
    public float get(float alpha) {
        return alpha * alpha;
    }
}

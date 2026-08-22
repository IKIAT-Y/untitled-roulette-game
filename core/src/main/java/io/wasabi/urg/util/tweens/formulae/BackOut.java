package io.wasabi.urg.util.tweens.formulae;

import io.wasabi.urg.util.tweens.TweenFormula;

public class BackOut implements TweenFormula {
    @Override
    public float get(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;

        return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
    }
}

package io.wasabi.urg.util.tweens;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

import io.wasabi.urg.util.tweens.formulae.QuadIn;
import io.wasabi.urg.util.tweens.formulae.QuadInOut;
import io.wasabi.urg.util.tweens.formulae.QuadOut;

public class Tween {
    public enum TweenStyle {
        QUAD,
    }
    public enum TweenDirection {
        IN, OUT, INOUT
    }

    private static final EnumMap<TweenStyle, EnumMap<TweenDirection, Class<? extends TweenFormula>>> TWEEN_CLASSES
    = new EnumMap<TweenStyle, EnumMap<TweenDirection, Class<? extends TweenFormula>>>(TweenStyle.class){{
        put(TweenStyle.QUAD, new EnumMap<TweenDirection, Class<? extends TweenFormula>>(TweenDirection.class) {{
            put(TweenDirection.IN, QuadIn.class);
            put(TweenDirection.OUT, QuadOut.class);
            put(TweenDirection.INOUT, QuadInOut.class);
        }});
    }};

    private float duration;
    private float time = 0;
    private float startValue;
    private float endValue;
    private TweenFormula formula;
    private boolean complete;

    public Tween(float duration, float startValue, float endValue, TweenStyle style, TweenDirection dir) {
        this.duration = duration;
        this.startValue = startValue;
        this.endValue = endValue;

        try {
            Class<? extends TweenFormula> formulaClass = TWEEN_CLASSES.get(style).get(dir);
            this.formula = formulaClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Invalid TweenStyle or TweenDirection!");
        }
    }

    private float lerp(float a, float b, float t) {
        return a * (1 - t) + b * t;
    }

    /**
     * Updates the tween and returns the current value
     * @param delta
     * @return the alpha value after interpolation
     */
    public float update(float delta) {
        if (complete) return endValue;

        time += delta;
        float alpha = formula.get(time / duration);

        if (time >= duration) {
            alpha = 1;
            complete = true;
        }

        return lerp(startValue, endValue, alpha);
    }

    /**
     * Return the current alpha value which represents the current progress in the tween.
     * @return the current alpha value
     */
    public float getAlpha() {
        return formula.get(time / duration);
    }

    /**
     * Resets the tween's internal timer to 0, restarting the tween.
     */
    public void reset() {
        time = 0;
        complete = false;
    }

    public boolean isComplete() {
        return complete;
    }
}

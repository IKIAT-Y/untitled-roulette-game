package io.wasabi.urg.elements.boss;

public class Boss {
    private final String name;
    private final String phrase;
    private final String description;

    public Boss(String name, String phrase, String description) {
        this.name = name;
        this.phrase = phrase;
        this.description = description;
    }
    public void roundStartEffect() {
        // Override roundStartEffect in subclasses to implement specific boss effects
    }
    public void beforeSpinEffect() {
        // Override beforeSpinEffect in subclasses to implement specific boss effects
    }
    public void afterSpinEffect() {
        // Override afterSpinEffect in subclasses to implement specific boss effects
    }
    public void roundEndEffect() {
        // Override roundEndEffect in subclasses to implement specific boss effects
    }
    public void charmConsumedEffect() {
        // Override charmConsumedEffect in subclasses to implement specific boss effects
    }

    public String getName() {
        return name;
    }

    public String getPhrase() {
        return phrase;
    }

    public String getDescription() {
        return description;
    }
}

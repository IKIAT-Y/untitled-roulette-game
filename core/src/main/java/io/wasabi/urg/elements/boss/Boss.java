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

    public void roundStartEffect() {}
    public void beforeSpinEffect() {}
    public void afterSpinEffect() {}
    public void roundEndEffect() {}
    public void charmConsumedEffect() {}

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

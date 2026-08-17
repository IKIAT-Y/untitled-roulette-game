package io.wasabi.urg.managers;

public final class RoundConfig {
    private final int act;
    private final int round;
    private final boolean bossRound;
    private final int quota;
    private final float quotaMult;
    private final int spinsAllowed;

    public RoundConfig(int act, int round, boolean bossRound, int quota, float quotaMult, int spinsAllowed) {
        this.act = act;
        this.round = round;
        this.bossRound = bossRound;
        this.quota = quota;
        this.quotaMult = quotaMult;
        this.spinsAllowed = spinsAllowed;
    }

    public int getAct() { return act; }
    public int getRound() { return round; }
    public boolean isBossRound() { return bossRound; }
    public int getQuota() { return (int) (quota * quotaMult); }
    public int getSpinsAllowed() { return spinsAllowed; }
}

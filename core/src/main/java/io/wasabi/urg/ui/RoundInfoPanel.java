package io.wasabi.urg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.betting.Bet;
import io.wasabi.urg.elements.boss.Boss;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.managers.RoundConfig;
import io.wasabi.urg.managers.RoundManager;
import io.wasabi.urg.state.RunState;
import io.wasabi.urg.util.tweens.Tween;

import java.util.List;

public class RoundInfoPanel {

    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final SpriteBatch SPRITE_BATCH = RENDERER_MANAGER.getSpriteBatch();

    private static final Texture TEXTURE = new Texture(Gdx.files.internal("ui/CorneredPatch.png"));
    private static final Texture TICKET_TEXTURE = new Texture(Gdx.files.internal("ticket.png"));

    // Colors
    private static final Color COL_SHADOW    = new Color(0.10f, 0.10f, 0.13f, 1f);
    private static final Color COL_OUTLINE   = Color.WHITE;
    private static final Color COL_PANEL_BG  = new Color(0.14f, 0.14f, 0.18f, 1f);
    private static final Color COL_BACKING   = new Color(0.21f, 0.44f, 0.35f, 1f); // panel behind everything
    private static final Color COL_NORMAL    = new Color(0.51f, 0.53f, 0.9f, 1f); // normal roundnd
    private static final Color COL_BOSS      = new Color(0.72f, 0.16f, 0.16f, 1f); // boss round
    private static final Color COL_SPINS     = new Color(0.22f, 0.47f, 0.86f, 1f);
    private static final Color COL_GOLD      = new Color(0.94f, 0.73f, 0.20f, 1f);
    private static final Color COL_TEXT      = Color.WHITE;
    private static final Color COL_TEXT_DIM  = new Color(0.78f, 0.78f, 0.82f, 1f);

    private final NinePatch patch;

    private final BitmapFont fontTitle = FontManager.getInstance().getFontByName("Placeholder");
    private final BitmapFont fontBody  = FontManager.getInstance().getFontByName("Placeholder");

    private final GlyphLayout layout = new GlyphLayout();

    // Layout
    private float x, y;               // top-left anchor of the whole panel
    private float width = 240f;
    private float padding = 2.5f;     // outline thickness
    private float sectionGap = 12f;   // gap between stacked boxes
    private float innerPadX = 10f;
    private float innerPadY = 18f;

    private Tween tween;
    private static final float OFFSCREEN_X = -1500f;

    // Data
    private String roundTitle = "Normal Round";
    private boolean bossRound = false;
    private String phrase = "";
    private String description = "";

    private int betAmount = 0;
    private int spinsRemaining = 0;
    private int money = 0;
    private int act = 1;
    private int round = 1;

    // Fixed sizing
    private float phraseBoxHeight = 80f;
    private float descriptionBoxHeight = 120f;
    private float phraseFontScale = 0.8f;
    private float descriptionFontScale = 0.7f;
    private float backingPadding = 32f;
    private float ticketIconSize = 32f;
    private float ticketIconGap = 6f;

    private RoundManager roundManager;
    private RunState runState;

    public RoundInfoPanel(float x, float y) {
        this.x = x;
        this.y = y;
        this.patch = new NinePatch(TEXTURE, 10, 10, 10, 10);

        this.roundManager = Roulette.getInstance().getRoundManager();
        this.runState = Roulette.getInstance().getRunState();
    }


    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public void setWidth(float width) { this.width = width; }

    public void setRoundType(String title) { this.roundTitle = title; }

    public void setBossInfo(String phrase, String description) {
        this.bossRound = true;
        this.phrase = phrase;
        this.description = description;
    }

    public void setRoundInfo(String phrase, String description) {
        this.bossRound = false;
        this.phrase = phrase;
        this.description = description;
    }

    public void setBetAmount(int amount) { this.betAmount = amount; }
    public void setSpinsRemaining(int spinsRemaining) { this.spinsRemaining = spinsRemaining; }
    public void setMoney(int money) { this.money = money; }
    public void setAct(int act) { this.act = act; }
    public void setRound(int round) { this.round = round; }

    public void setPhraseBoxHeight(float height) { this.phraseBoxHeight = height; }
    public void setDescriptionBoxHeight(float height) { this.descriptionBoxHeight = height; }
    public void setPhraseFontScale(float scale) { this.phraseFontScale = scale; }
    public void setDescriptionFontScale(float scale) { this.descriptionFontScale = scale; }
    public void setBackingPadding(float padding) { this.backingPadding = padding; }
    public void setTicketIconSize(float size) { this.ticketIconSize = size; }

    public void update(float delta) {
        Boss boss = this.runState.getBoss();
        RoundConfig config = this.roundManager.getCurrentConfig();

        if (config.isBossRound() && boss != null) {
            setRoundType(boss.getName());
            setBossInfo(boss.getPhrase(), boss.getDescription());
        } else {
            setRoundType("Normal Round");
            setRoundInfo("A normal round of roulette.", "Reach the quota before you run out of spins.");
        }

        List<Bet> activeBets = this.runState.getActiveBets();
        int betSum = activeBets.stream().mapToInt(Bet::getAmount).sum();
        setBetAmount(betSum);
        setMoney(this.runState.getTickets());
        setSpinsRemaining(this.roundManager.getSpinsRemaining());
        setAct(this.roundManager.getAct());
        setRound(this.roundManager.getRound());

        if (tween != null && !tween.isComplete()) {
            x = tween.update(delta);
        }
    }

    public void render() {
        SPRITE_BATCH.begin();

        layout.setText(fontTitle, roundTitle, COL_TEXT, width - innerPadX * 2, Align.center, true);
        float bannerHeight = layout.height + innerPadY * 2;
        float statRowHeight = fontTitle.getLineHeight() + fontBody.getLineHeight() + innerPadY * 3;
        float moneyBoxHeight = fontTitle.getLineHeight() + innerPadY * 2;
        float actRoundRowHeight = fontTitle.getLineHeight() + fontBody.getLineHeight() + innerPadY * 3;

        float contentHeight = bannerHeight + sectionGap
            + phraseBoxHeight + sectionGap
            + descriptionBoxHeight + sectionGap
            + statRowHeight + sectionGap
            + moneyBoxHeight + sectionGap
            + actRoundRowHeight;

        drawBackingPanel(y, contentHeight);

        float startY = y;

        startY = drawBanner(startY);

        startY -= sectionGap;
        startY = drawFixedTextBox(startY, phraseBoxHeight, phrase, fontBody, phraseFontScale, COL_TEXT, COL_PANEL_BG);

        startY -= sectionGap;
        startY = drawFixedTextBox(startY, descriptionBoxHeight, description, fontBody, descriptionFontScale, COL_TEXT_DIM, COL_PANEL_BG);

        startY -= sectionGap;
        startY = drawStatRow(startY);

        startY -= sectionGap;
        startY = drawMoneyBox(startY);

        startY -= sectionGap;
        drawActRoundRow(startY);

        SPRITE_BATCH.setColor(1, 1, 1, 1);
        SPRITE_BATCH.end();
    }

    private void drawBackingPanel(float contentTopY, float contentHeight) {
        float backingWidth = width + backingPadding * 2 + 500f;
        float backingHeight = contentHeight + backingPadding * 2 + 1000f;
        float backingX = x - backingPadding - 500f;
        float backingTopY = contentTopY + backingPadding;
        float backingBoxY = backingTopY - backingHeight + 500f;

        SPRITE_BATCH.setColor(COL_SHADOW);
        patch.draw(SPRITE_BATCH, backingX, backingBoxY - padding, backingWidth, backingHeight);

        SPRITE_BATCH.setColor(COL_OUTLINE);
        patch.draw(SPRITE_BATCH, backingX, backingBoxY, backingWidth, backingHeight);

        SPRITE_BATCH.setColor(COL_BACKING);
        patch.draw(SPRITE_BATCH, backingX + padding, backingBoxY + padding, backingWidth - padding * 2, backingHeight - padding * 2);
    }

    private float drawBox(float topY, float boxHeight, Color fill) {
        float boxY = topY - boxHeight;

        SPRITE_BATCH.setColor(COL_SHADOW);
        patch.draw(SPRITE_BATCH, x, boxY - padding, width, boxHeight);

        SPRITE_BATCH.setColor(COL_OUTLINE);
        patch.draw(SPRITE_BATCH, x, boxY, width, boxHeight);

        SPRITE_BATCH.setColor(fill);
        patch.draw(SPRITE_BATCH, x + padding, boxY + padding, width - padding * 2, boxHeight - padding * 2);

        return boxY;
    }

    private float drawBanner(float topY) {
        Color bannerColor = bossRound ? COL_BOSS : COL_NORMAL;

        layout.setText(fontTitle, roundTitle, COL_TEXT, width - innerPadX * 2, Align.center, true);
        float h = layout.height + innerPadY * 2;
        float boxY = drawBox(topY, h, bannerColor);

        fontTitle.setColor(COL_TEXT);
        fontTitle.draw(SPRITE_BATCH, roundTitle, x + innerPadX, boxY + h / 2f + layout.height / 2f,
            width - innerPadX * 2, Align.center, true);

        return boxY;
    }

    private float drawFixedTextBox(float topY, float boxHeight, String text, BitmapFont font, float fontScale, Color textColor, Color bg) {
        float textWidth = width - innerPadX * 2;
        float boxY = drawBox(topY, boxHeight, bg);

        float prevScaleX = font.getData().scaleX;
        float prevScaleY = font.getData().scaleY;
        font.getData().setScale(fontScale);

        layout.setText(font, text, textColor, textWidth, Align.center, true);
        font.setColor(textColor);
        font.draw(SPRITE_BATCH, text, x + innerPadX, boxY + boxHeight - innerPadY, textWidth, Align.center, true);

        font.getData().setScale(prevScaleX, prevScaleY);

        return boxY;
    }

    private float drawStatRow(float topY) {
        float h = fontTitle.getLineHeight() + fontBody.getLineHeight() + innerPadY * 3;
        float gap = 6f;
        float halfWidth = (width - gap) / 2f;
        float boxY = topY - h;

        // bet amount
        SPRITE_BATCH.setColor(COL_SHADOW);
        patch.draw(SPRITE_BATCH, x, boxY - padding, halfWidth, h);
        SPRITE_BATCH.setColor(COL_OUTLINE);
        patch.draw(SPRITE_BATCH, x, boxY, halfWidth, h);
        SPRITE_BATCH.setColor(COL_PANEL_BG);
        patch.draw(SPRITE_BATCH, x + padding, boxY + padding, halfWidth - padding * 2, h - padding * 2);
        drawStatText(x, halfWidth, boxY, h, "Bet Amount", "$" + betAmount, COL_GOLD);

        // spins
        float x2 = x + halfWidth + gap;
        SPRITE_BATCH.setColor(COL_SHADOW);
        patch.draw(SPRITE_BATCH, x2, boxY - padding, halfWidth, h);
        SPRITE_BATCH.setColor(COL_OUTLINE);
        patch.draw(SPRITE_BATCH, x2, boxY, halfWidth, h);
        SPRITE_BATCH.setColor(COL_PANEL_BG);
        patch.draw(SPRITE_BATCH, x2 + padding, boxY + padding, halfWidth - padding * 2, h - padding * 2);
        drawStatText(x2, halfWidth, boxY, h, "Spins", String.valueOf(spinsRemaining), COL_SPINS);

        return boxY;
    }

    private void drawStatText(float boxX, float boxWidth, float boxY, float boxH, String label, String value, Color valueColor) {
        float titleH = fontTitle.getLineHeight();

        fontBody.setColor(COL_TEXT_DIM);
        fontBody.getData().setScale(0.6f);
        fontBody.draw(SPRITE_BATCH, label, boxX, boxY + boxH - innerPadY, boxWidth, Align.center, false);

        fontTitle.setColor(valueColor);
        fontTitle.getData().setScale(1.2f);
        fontTitle.draw(SPRITE_BATCH, value, boxX, boxY + innerPadY + titleH, boxWidth, Align.center, false);
        fontTitle.getData().setScale(1f);
    }

    private float drawMoneyBox(float topY) {
        float lineH = fontTitle.getLineHeight();
        float h = lineH + innerPadY * 2;
        float boxY = drawBox(topY, h, COL_PANEL_BG);

        fontTitle.getData().setScale(1f);
        layout.setText(fontTitle, "$" + money);
        float textWidth = layout.width;

        float totalWidth = ticketIconSize + ticketIconGap + textWidth;
        float groupX = x + (width - totalWidth) / 2f;
        float iconY = boxY + h / 2f - ticketIconSize / 2f;

        SPRITE_BATCH.setColor(1, 1, 1, 1);
        SPRITE_BATCH.draw(TICKET_TEXTURE, groupX, iconY, ticketIconSize, ticketIconSize);

        fontTitle.setColor(COL_GOLD);
        fontTitle.draw(SPRITE_BATCH, "" + money, groupX + ticketIconSize + ticketIconGap,
            boxY + h / 2f + lineH / 2f - 5f, textWidth, Align.left, false);

        return boxY;
    }

    private void drawActRoundRow(float topY) {
        float h = fontTitle.getLineHeight() + fontBody.getLineHeight() + innerPadY * 3;
        float gap = 6f;
        float halfWidth = (width - gap) / 2f;
        float boxY = topY - h;

        // act
        SPRITE_BATCH.setColor(COL_SHADOW);
        patch.draw(SPRITE_BATCH, x, boxY - padding, halfWidth, h);
        SPRITE_BATCH.setColor(COL_OUTLINE);
        patch.draw(SPRITE_BATCH, x, boxY, halfWidth, h);
        SPRITE_BATCH.setColor(COL_PANEL_BG);
        patch.draw(SPRITE_BATCH, x + padding, boxY + padding, halfWidth - padding * 2, h - padding * 2);
        drawStatText(x, halfWidth, boxY, h, "Act", String.valueOf(act), COL_TEXT);

        // round
        float x2 = x + halfWidth + gap;
        SPRITE_BATCH.setColor(COL_SHADOW);
        patch.draw(SPRITE_BATCH, x2, boxY - padding, halfWidth, h);
        SPRITE_BATCH.setColor(COL_OUTLINE);
        patch.draw(SPRITE_BATCH, x2, boxY, halfWidth, h);
        SPRITE_BATCH.setColor(COL_PANEL_BG);
        patch.draw(SPRITE_BATCH, x2 + padding, boxY + padding, halfWidth - padding * 2, h - padding * 2);
        drawStatText(x2, halfWidth, boxY, h, "Round", String.valueOf(round), COL_TEXT);
    }

    public void hide() { tween = new Tween(1f, x, OFFSCREEN_X, Tween.TweenStyle.QUAD, Tween.TweenDirection.IN); }
    public void show() { tween = new Tween(1f, x, -700f, Tween.TweenStyle.QUAD, Tween.TweenDirection.OUT); }
}

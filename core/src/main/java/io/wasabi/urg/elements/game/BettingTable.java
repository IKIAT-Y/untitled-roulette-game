package io.wasabi.urg.elements.game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.GameObject;
import io.wasabi.urg.elements.betting.Bet;
import io.wasabi.urg.elements.betting.BetType;
import io.wasabi.urg.elements.betting.BetZone;
import io.wasabi.urg.elements.betting.BettingTableLayout;
import io.wasabi.urg.elements.betting.Chip;
import io.wasabi.urg.elements.betting.ChipDenomination;
import io.wasabi.urg.elements.betting.PocketColor;
import io.wasabi.urg.elements.betting.TableLayoutGenerator;
import io.wasabi.urg.managers.FontManager;
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.state.RunState;

public class BettingTable extends GameObject {
    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final ShapeRenderer SHAPE_RENDERER = RENDERER_MANAGER.getShapeRenderer();
    private static final SpriteBatch SPRITE_BATCH = RENDERER_MANAGER.getSpriteBatch();
    private static final FontManager FONT_MANAGER = FontManager.getInstance();
    private static final BitmapFont FONT = FONT_MANAGER.getFontByName("Placeholder");

    private static final float CHIP_RADIUS = 32f;
    private static final float CHIP_STACK_OFFSET = 10f;
    private static final float TRAY_GAP = 168f;
    private static final float TRAY_MARGIN = 90f;
    private static final float NUMBER_FONT_SCALE = 2f;
    // Outside boxes hold whole words ("BLACK", "19-36") rather than 1-2 digits, so
    // they need a smaller scale than the straight-zone numbers to fit the box —
    // tuned by eye against the placeholder font, not measured exactly.
    private static final float OUTSIDE_LABEL_FONT_SCALE = 1.2f;

    // Small, bounded cache (one entry per distinct pocket number ever drawn) so we
    // aren't allocating a fresh String every zone, every frame.
    private final Map<Integer, String> numberLabels = new HashMap<>();

    private final RunState runState;
    private final List<Tile> tiles;
    private final TableLayoutGenerator generator = new TableLayoutGenerator();

    private float posX;
    private float posY;

    private BettingTableLayout layout;
    private List<Tile> lastKnownTiles;

    // Backed by RunState, not owned here — see the comment on RunState.activeBets.
    // This
    // BettingTable instance is screen-local and gets recreated on screen
    // transitions; the
    // bets themselves must outlive that.
    private final List<Bet> activeBets;
    private final List<Chip> placedChips = new ArrayList<>();
    private final List<Chip> trayChips = new ArrayList<>();

    private BetZone hoveredZone;
    private Chip draggingChip;

    /**
     * Procedural betting table. Reads its tiles straight from
     * {@link RunState#getTiles()} — the
     * same list {@link Wheel} draws from — so the table always reflects the current
     * roguelike
     * pocket configuration without anything needing to push updates to it
     * explicitly (though
     * {@link #rebuildLayout()} is exposed for callers who want to force an
     * immediate rebuild
     * rather than waiting for the next {@link #update(float)}).
     *
     * Owns bet placement, the chip tray, and payout resolution. Input (drag/drop)
     * is handled by
     * {@link io.wasabi.urg.elements.betting.ChipDragController}, which calls back
     * into this class
     * for every actual state change.
     */
    public BettingTable() {
        this.runState = Roulette.getInstance().getRunState();
        this.tiles = runState.getTiles();
        this.activeBets = runState.getActiveBets();
        rebuildLayout();

        // Re-create chip visuals for any bets already in RunState (e.g. this
        // BettingTable
        // was just re-instantiated after a screen transition, or bets survived a
        // reset).
        for (Bet bet : activeBets) {
            Chip chip = new Chip(ChipDenomination.ONE, 0, 0, CHIP_RADIUS);
            chip.setBet(bet);
            int stackIndex = countChipsOnZone(bet.getZone());
            Vector2 anchor = bet.getZone().getChipAnchor();
            chip.setPosition(anchor.x, anchor.y + stackIndex * CHIP_STACK_OFFSET);
            placedChips.add(chip);
        }
    }

    public void setPosition(float x, float y) {
        this.posX = x;
        this.posY = y;
        rebuildLayout();
    }

    /**
     * Regenerates the grid + every bet zone from the current tile list, drops any
     * bets that no longer make sense (a covered tile was removed from the run), and
     * rebuilds the chip tray position to match the new table bounds.
     */
    public void rebuildLayout() {
        this.layout = generator.generate(tiles, posX, posY);
        this.lastKnownTiles = new ArrayList<>(tiles);
        invalidateOrphanedBets();
        rebuildTray();
    }

    private void invalidateOrphanedBets() {
        List<Bet> orphaned = new ArrayList<>();
        for (Bet bet : activeBets) {
            if (isOrphaned(bet.getZone())) {
                orphaned.add(bet);
            }
        }
        for (Bet bet : orphaned) {
            // No refund — placing a bet never deducted chips in the first place, see
            // placeBet().
            activeBets.remove(bet);
            placedChips.removeIf(chip -> chip.getBet() == bet);
        }
    }

    /**
     * A zone is orphaned once any of the distinct NUMBERS it covers has no
     * surviving tile at all. Straight zones on a duplicated number (see
     * TableLayoutGenerator) list more than one covered tile for that single
     * number — losing one duplicate shouldn't drop the bet as long as another tile
     * with that number is still on the wheel.
     */
    private boolean isOrphaned(BetZone zone) {
        Map<Integer, Boolean> numberSurvives = new HashMap<>();
        for (Tile covered : zone.getCoveredTiles()) {
            numberSurvives.merge(covered.getNumber(), tiles.contains(covered), Boolean::logicalOr);
        }
        return numberSurvives.containsValue(false);
    }

    private void rebuildTray() {
        trayChips.clear();
        Rectangle bounds = layout.getTableBounds();
        float trayY = bounds.y - TRAY_MARGIN - CHIP_RADIUS;
        ChipDenomination[] denominations = ChipDenomination.values();
        for (int i = 0; i < denominations.length; i++) {
            float trayX = bounds.x + CHIP_RADIUS + i * (CHIP_RADIUS * 2 + TRAY_GAP);
            trayChips.add(new Chip(denominations[i], trayX, trayY, CHIP_RADIUS));
        }
    }

    @Override
    public void update(float delta) {
        // Settlement clears logical bets in RunState. Remove their matching chip
        // visuals so an old chip cannot look like a live bet on the next spin.
        placedChips.removeIf(chip -> chip.getBet() != null
                && !activeBets.contains(chip.getBet()));

        // Cheap change-detection placeholder until the roguelike layer has a proper
        // "pockets changed" event to push. Fine at the tile counts this game deals
        // with.
        if (!tiles.equals(lastKnownTiles)) {
            rebuildLayout();
        }
    }

    public Chip getTrayChipAt(Vector2 point) {
        for (Chip chip : trayChips) {
            if (chip.contains(point))
                return chip;
        }
        return null;
    }

    public Chip getPlacedChipAt(Vector2 point) {
        for (Chip chip : placedChips) {
            if (chip.contains(point))
                return chip;
        }
        return null;
    }

    public BetZone findNearestZone(Vector2 point, float snapRadius) {
        BetZone nearest = null;
        float nearestDist = snapRadius;
        for (BetZone zone : layout.getBetZones()) {
            float dist = zone.distanceTo(point);
            if (dist <= nearestDist) {
                nearest = zone;
                nearestDist = dist;
            }
        }
        return nearest;
    }

    /** Spawns a fresh chip for dragging — the tray itself is never depleted. */
    public Chip beginDragFromTray(ChipDenomination denomination, Vector2 point) {
        Chip chip = new Chip(denomination, point.x, point.y, CHIP_RADIUS);
        chip.setDragging(true);
        draggingChip = chip;
        return chip;
    }

    /**
     * Picks an already-placed chip back up. No refund needed — placing a bet never
     * deducted chips in the first place, see {@link #placeBet}. If the player drops
     * it back on a zone it's re-tracked there; if they drop it off the table it's
     * simply gone from {@link #activeBets}.
     */
    public Chip beginDragFromPlaced(Chip chip) {
        Bet bet = chip.getBet();
        if (bet == null)
            return null;

        activeBets.remove(bet);
        placedChips.remove(chip);
        chip.setBet(null);
        chip.setDragging(true);
        draggingChip = chip;
        return chip;
    }

    /**
     * Places (or re-places) a bet. Chip denominations are a percentage of the
     * player's real balance ({@link RunState#getChips()}) — deliberately NOT of
     * balance-minus-already-placed-bets, so every chip keeps a stable value for the
     * whole betting round instead of shrinking as more bets go down. Actually
     * spending the chips happens once, in bulk, at
     * {@link RunState#resolveActiveBets()}
     * — placing a bet here only ever reserves against the player's real balance
     * minus what's already reserved by other pending bets, it never mutates
     * {@link RunState#getChips()} itself.
     */
    public void placeBet(BetZone zone, Chip chip) {
        int balance = runState.getChips();
        // At low balances a straight Math.round can floor small percentages (ONE,
        // FIVE) to zero chips, silently discarding the chip instead of placing a real
        // bet. Once the player has any chips at all, every denomination should place
        // at least 1.
        int amount = balance <= 0 ? 0 : Math.max(1, Math.round(chip.getDenomination().value * balance));
        int available = balance - totalCommitted();
        if (amount <= 0 || amount > available) {
            discardChip(chip);
            return;
        }

        Bet bet = new Bet(zone, amount);
        chip.setBet(bet);
        chip.setDragging(false);

        int stackIndex = countChipsOnZone(zone);
        Vector2 anchor = zone.getChipAnchor();
        chip.setPosition(anchor.x, anchor.y + stackIndex * CHIP_STACK_OFFSET);

        activeBets.add(bet);
        placedChips.add(chip);
        draggingChip = null;
    }

    /**
     * Sum of every currently-pending bet's stake — reserved against the balance
     * but not yet deducted from it.
     */
    private int totalCommitted() {
        int total = 0;
        for (Bet bet : activeBets) {
            total += bet.getAmount();
        }
        return total;
    }

    /**
     * No stake to refund here — tray-sourced chips never charged anything, and
     * chips picked
     * up from an existing bet were already refunded in
     * {@link #beginDragFromPlaced}.
     */
    public void discardChip(Chip chip) {
        chip.setDragging(false);
        draggingChip = null;
    }

    private int countChipsOnZone(BetZone zone) {
        int count = 0;
        for (Chip chip : placedChips) {
            if (chip.getBet() != null && chip.getBet().getZone() == zone)
                count++;
        }
        return count;
    }

    public void setHoveredZone(BetZone hoveredZone) {
        this.hoveredZone = hoveredZone;
    }

    // ---------------------------------------------------------------------------------------
    // Round lifecycle
    // ---------------------------------------------------------------------------------------

    /**
     * Clears every active bet (player-initiated, before spinning). No refund needed
     * — placing a bet never deducted chips, see {@link #placeBet}. Actual
     * resolution (the only point chips actually change hands) happens in
     * {@link RunState#resolveActiveBets()}, called from {@link Ball#finalizeStop()}
     * — this table doesn't need to be on screen for that to work.
     */
    public void clearBets() {
        runState.clearActiveBets();
        placedChips.clear();
    }

    public BettingTableLayout getLayout() {
        return layout;
    }

    public List<Bet> getActiveBets() {
        return activeBets;
    }

    @Override
    public void render() {
        if (layout == null)
            return;

        List<BetZone> straightZones = layout.getZonesOfType(BetType.STRAIGHT);
        List<BetZone> outsideZones = outsideCategoryZones();
        List<BetZone> dozenZones = layout.getZonesOfType(BetType.DOZEN);

        drawStraightZoneTextures(straightZones);

        SHAPE_RENDERER.begin(ShapeType.Filled);
        for (BetZone zone : outsideZones) {
            SHAPE_RENDERER.setColor(colorForOutsideType(zone.getType()));
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            SHAPE_RENDERER.rect(r.x, r.y, r.width, r.height);
        }
        SHAPE_RENDERER.setColor(Color.FOREST);
        for (BetZone zone : dozenZones) {
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            SHAPE_RENDERER.rect(r.x, r.y, r.width, r.height);
        }
        SHAPE_RENDERER.end();

        drawZoneLabels(straightZones, outsideZones, dozenZones);

        SHAPE_RENDERER.begin(ShapeType.Line);
        SHAPE_RENDERER.setColor(Color.WHITE);
        for (BetZone zone : straightZones) {
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            SHAPE_RENDERER.rect(r.x, r.y, r.width, r.height);
        }
        for (BetZone zone : outsideZones) {
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            SHAPE_RENDERER.rect(r.x, r.y, r.width, r.height);
        }
        for (BetZone zone : dozenZones) {
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            SHAPE_RENDERER.rect(r.x, r.y, r.width, r.height);
        }

        // Snap-point markers for the remaining bet types that don't have a dedicated
        // shape of their own yet (split/corner/street/six-line/column) — just an
        // anchor point to snap a chip to.
        SHAPE_RENDERER.setColor(Color.LIGHT_GRAY);
        for (BetZone zone : layout.getBetZones()) {
            if (zone.getType() == BetType.STRAIGHT || zone.getType() == BetType.DOZEN
                    || isOutsideCategoryType(zone.getType()))
                continue;
            Vector2 a = zone.getChipAnchor();
            SHAPE_RENDERER.circle(a.x, a.y, 4f);
        }

        if (hoveredZone != null) {
            SHAPE_RENDERER.setColor(Color.YELLOW);
            Rectangle r = hoveredZone.getHitArea().getBoundingRectangle();
            SHAPE_RENDERER.rect(r.x, r.y, r.width, r.height);
        }
        SHAPE_RENDERER.end();

        // Sprite batch chips
        for (Chip chip : trayChips) {
            chip.draw(SPRITE_BATCH, textureFor(chip.getDenomination()));
        }
        for (Chip chip : placedChips) {
            chip.draw(SPRITE_BATCH, textureFor(chip.getDenomination()));
        }
        if (draggingChip != null) {
            draggingChip.draw(SPRITE_BATCH, textureFor(draggingChip.getDenomination()));
        }
    }

    /**
     * Draws each straight zone's own tile texture, stretched to fill its cell —
     * see {@link #pickStraightZoneTexture} for which tile represents the zone
     * when its number is duplicated. Generic on purpose: whatever texture a tile
     * reports (see {@link Tile#getTexture()}) is what gets drawn, so a new tile
     * type never needs a new branch here to render correctly on the table.
     */
    private void drawStraightZoneTextures(List<BetZone> straightZones) {
        SPRITE_BATCH.begin();
        for (BetZone zone : straightZones) {
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            Texture texture = pickStraightZoneTexture(zone);
            SPRITE_BATCH.draw(texture, r.x, r.y, r.width, r.height);
        }
        SPRITE_BATCH.end();
    }

    /**
     * Picks which of a straight zone's covered tiles (more than one when its
     * number is duplicated — see TableLayoutGenerator's class javadoc) supplies
     * the texture drawn for the whole zone:
     * <ol>
     * <li>If any covered tile is "special" (not conventionally red/black/green —
     * {@link TableLayoutGenerator#getColor} returns {@link PocketColor#SPECIAL}),
     * use the LATEST such tile (last one in the zone's covered-tile order, which
     * follows tile-list order — i.e. the most recently added).</li>
     * <li>Otherwise use whichever base colour (RED/BLACK/GREEN) has the most
     * covered tiles, breaking ties by whichever colour reaches that count
     * first.</li>
     * </ol>
     * This only affects the flat rectangle rendered on the table — it never
     * changes which tiles the zone actually covers, so e.g. a red 8 and a black 8
     * sharing this zone stay independently betable via the RED/BLACK outside
     * zones (see TableLayoutGenerator#buildOutsideCategoryZones) regardless of
     * which one's texture wins here.
     */
    private Texture pickStraightZoneTexture(BetZone zone) {
        List<Tile> covered = zone.getCoveredTiles();

        Tile latestSpecial = null;
        for (Tile tile : covered) {
            if (generator.getColor(tile) == PocketColor.SPECIAL) {
                latestSpecial = tile;
            }
        }
        if (latestSpecial != null) {
            return latestSpecial.getTexture();
        }

        Map<PocketColor, Integer> counts = new EnumMap<>(PocketColor.class);
        Tile bestRepresentative = null;
        int bestCount = 0;
        for (Tile tile : covered) {
            int count = counts.merge(generator.getColor(tile), 1, Integer::sum);
            if (count > bestCount) {
                bestCount = count;
                bestRepresentative = tile;
            }
        }
        return bestRepresentative.getTexture();
    }

    /**
     * Draws every straight zone's pocket number and every outside/dozen zone's
     * label, shrunk to fit and centered in their box. FONT is shared with other
     * renderers (e.g. Tile, which draws with it at the default scale/color) —
     * every tweak made here is saved beforehand and restored afterward so it can't
     * leak into whatever draws with FONT next. All three groups share one
     * SpriteBatch begin/end — changing the font's scale mid-batch is fine, it only
     * affects the vertices of draws that come after it.
     */
    private void drawZoneLabels(List<BetZone> straightZones, List<BetZone> outsideZones,
            List<BetZone> dozenZones) {
        float originalScaleX = FONT.getScaleX();
        float originalScaleY = FONT.getScaleY();
        Color originalColor = FONT.getColor().cpy();

        FONT.setColor(Color.WHITE);

        SPRITE_BATCH.begin();

        FONT.getData().setScale(NUMBER_FONT_SCALE, NUMBER_FONT_SCALE);
        // BitmapFont#draw's y is the TOP of the text, so centering vertically means
        // pushing the top edge up by half the (scaled) glyph height from the box's
        // midpoint.
        float straightHalfHeight = FONT.getCapHeight() / 2f;
        for (BetZone zone : straightZones) {
            Tile tile = zone.getCoveredTiles().get(0);
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            FONT.draw(SPRITE_BATCH, labelFor(tile.getNumber()), r.x,
                    r.y + r.height / 2f + straightHalfHeight, r.width, Align.center, false);
        }

        // Dozen boxes are at least as roomy as outside boxes (see
        // TableLayoutConfig.DOZEN_STRIP_HEIGHT vs OUTSIDE_BOX_HEIGHT), so both share
        // the same smaller word-sized scale.
        FONT.getData().setScale(OUTSIDE_LABEL_FONT_SCALE, OUTSIDE_LABEL_FONT_SCALE);
        float wordHalfHeight = FONT.getCapHeight() / 2f;
        for (BetZone zone : outsideZones) {
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            FONT.draw(SPRITE_BATCH, outsideLabelFor(zone), r.x,
                    r.y + r.height / 2f + wordHalfHeight, r.width, Align.center, false);
        }
        for (BetZone zone : dozenZones) {
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            FONT.draw(SPRITE_BATCH, numberRangeLabel(zone), r.x,
                    r.y + r.height / 2f + wordHalfHeight, r.width, Align.center, false);
        }

        SPRITE_BATCH.end();

        FONT.getData().setScale(originalScaleX, originalScaleY);
        FONT.setColor(originalColor);
    }

    private String labelFor(int number) {
        return numberLabels.computeIfAbsent(number, String::valueOf);
    }

    /**
     * RED/BLACK/EVEN/ODD are fixed labels; LOW/HIGH show the actual number range
     * they cover (derived from the zone's own covered tiles) rather than a
     * hardcoded "1-18"/"19-36", since that range is rank-based and shifts as the
     * roguelike layer changes what numbers exist — see TableLayoutGenerator.
     */
    private String outsideLabelFor(BetZone zone) {
        switch (zone.getType()) {
            case RED:
                return "RED";
            case BLACK:
                return "BLACK";
            case EVEN:
                return "EVEN";
            case ODD:
                return "ODD";
            case LOW:
            case HIGH:
                return numberRangeLabel(zone);
            default:
                return "";
        }
    }

    private String numberRangeLabel(BetZone zone) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Tile tile : zone.getCoveredTiles()) {
            min = Math.min(min, tile.getNumber());
            max = Math.max(max, tile.getNumber());
        }
        return min + "-" + max;
    }

    private List<BetZone> outsideCategoryZones() {
        List<BetZone> zones = new ArrayList<>();
        for (BetZone zone : layout.getBetZones()) {
            if (isOutsideCategoryType(zone.getType())) {
                zones.add(zone);
            }
        }
        return zones;
    }

    private boolean isOutsideCategoryType(BetType type) {
        switch (type) {
            case LOW:
            case HIGH:
            case EVEN:
            case ODD:
            case RED:
            case BLACK:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the color to fill an outside-category zone with. RED/BLACK are
     * colored, everything else is dark green.
     */
    private Color colorForOutsideType(BetType type) {
        switch (type) {
            case RED:
                return Color.RED;
            case BLACK:
                return Color.BLACK;
            default:
                return Color.FOREST;
        }
    }

    private Texture textureFor(ChipDenomination denomination) {
        switch (denomination) {
            case ONE:
                return new Texture(Gdx.files.internal("chips/TEX_Chip_64x64_1White.png"));
            case FIVE:
                return new Texture(Gdx.files.internal("chips/TEX_Chip_64x64_5Red.png"));
            case TEN:
                return new Texture(Gdx.files.internal("chips/TEX_Chip_64x64_10Green.png"));
            case TWENTY_FIVE:
                return new Texture(Gdx.files.internal("chips/TEX_Chip_64x64_25Blue.png"));
            case FIFTY:
                return new Texture(Gdx.files.internal("chips/TEX_Chip_64x64_50Black.png"));
            case HUNDRED:
                return new Texture(Gdx.files.internal("chips/TEX_Chip_64x64_100Purple.png"));
            default:
                return new Texture(Gdx.files.internal("chips/TEX_Chip_64x64_Default.png"));
        }
    }

    @Override
    public void dispose() {
        // ShapeRenderer is owned/disposed by RendererManager, not this class.
    }
}

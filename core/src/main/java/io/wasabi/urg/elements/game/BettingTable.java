package io.wasabi.urg.elements.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

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
import io.wasabi.urg.managers.RendererManager;
import io.wasabi.urg.state.RunState;

public class BettingTable extends GameObject {
    private static final RendererManager RENDERER_MANAGER = RendererManager.getInstance();
    private static final ShapeRenderer SHAPE_RENDERER = RENDERER_MANAGER.getShapeRenderer();
    private static final SpriteBatch SPRITE_BATCH = RENDERER_MANAGER.getSpriteBatch();

    private static final float CHIP_RADIUS = 8f;
    private static final float CHIP_STACK_OFFSET = 3f;
    private static final float TRAY_GAP = 6f;
    private static final float TRAY_MARGIN = 12f;

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
     * Regenerates the grid + every bet zone from the current tile list, refunds any
     * bets
     * that no longer make sense (a covered tile was removed from the run), and
     * rebuilds the
     * chip tray position to match the new table bounds.
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
            for (Tile covered : bet.getZone().getCoveredTiles()) {
                if (!tiles.contains(covered)) {
                    orphaned.add(bet);
                    break;
                }
            }
        }
        for (Bet bet : orphaned) {
            runState.addChips(bet.getAmount());
            activeBets.remove(bet);
            placedChips.removeIf(chip -> chip.getBet() == bet);
        }
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
     * Picks an already-placed chip back up, refunding its stake immediately. If the
     * player
     * drops it back on a zone the stake is re-charged there; if they drop it off
     * the table
     * it simply stays refunded.
     */
    public Chip beginDragFromPlaced(Chip chip) {
        Bet bet = chip.getBet();
        if (bet == null)
            return null;

        runState.addChips(bet.getAmount());
        activeBets.remove(bet);
        placedChips.remove(chip);
        chip.setBet(null);
        chip.setDragging(true);
        draggingChip = chip;
        return chip;
    }

    public void placeBet(BetZone zone, Chip chip) {
        int amount = Math.round(chip.getDenomination().value * runState.getChips());
        if (amount <= 0 || !runState.spendChips(amount)) {
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
     * Refunds and clears every active bet (player-initiated, before spinning).
     * Actual
     * resolution after a spin happens in {@link RunState#resolveActiveBets()},
     * called from
     * {@link Ball#finalizeStop()} — this table doesn't need to be on screen for
     * that to work.
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

        SHAPE_RENDERER.begin(ShapeType.Filled);
        for (BetZone zone : layout.getZonesOfType(BetType.STRAIGHT)) {
            Tile tile = zone.getCoveredTiles().get(0);
            SHAPE_RENDERER.setColor(colorFor(generator.getColor(tile)));
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            SHAPE_RENDERER.rect(r.x, r.y, r.width, r.height);
        }
        SHAPE_RENDERER.end();

        SHAPE_RENDERER.begin(ShapeType.Line);
        SHAPE_RENDERER.setColor(Color.WHITE);
        for (BetZone zone : layout.getZonesOfType(BetType.STRAIGHT)) {
            Rectangle r = zone.getHitArea().getBoundingRectangle();
            SHAPE_RENDERER.rect(r.x, r.y, r.width, r.height);
        }

        // Snap-point markers for every non-straight bet (split/corner/street/six-line/
        // column/dozen/outside) — first-pass visual feedback until these get real art.
        SHAPE_RENDERER.setColor(Color.LIGHT_GRAY);
        for (BetZone zone : layout.getBetZones()) {
            if (zone.getType() == BetType.STRAIGHT)
                continue;
            Vector2 a = zone.getChipAnchor();
            SHAPE_RENDERER.circle(a.x, a.y, 2f);
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

    private Color colorFor(PocketColor color) {
        switch (color) {
            case RED:
                return Color.RED;
            case BLACK:
                return Color.BLACK;
            case GREEN:
                return Color.GREEN;
            default:
                return Color.GRAY;
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

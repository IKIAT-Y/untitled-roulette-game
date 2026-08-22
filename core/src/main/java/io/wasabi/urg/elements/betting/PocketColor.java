package io.wasabi.urg.elements.betting;

/**
 * A single summarising colour category for a tile, used where exactly one
 * answer is needed (e.g. picking one texture to represent a straight-bet zone
 * — see {@link TableLayoutGenerator#getColor}).
 *
 * This deliberately does NOT decide RED/BLACK outside-bet membership — a tile
 * can independently count as red, black, both (a striped tile), or neither at
 * once (see {@link io.wasabi.urg.elements.game.Tile#isRed()}/{@code isBlack()}/
 * {@code isGreen()}), which a single value can't represent. Bucketing for those
 * bets checks the tile's booleans directly instead.
 */
public enum PocketColor {
    RED,
    BLACK,
    GREEN,
    /** Not conventionally red, black, or green — a tile type with its own distinct look. */
    SPECIAL
}

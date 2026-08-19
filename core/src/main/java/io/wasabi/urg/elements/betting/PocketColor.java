package io.wasabi.urg.elements.betting;

/**
 * The betting-relevant colour category of a tile.
 *
 * This is intentionally separate from whatever colour
 * {@link io.wasabi.urg.elements.game.Tile}
 * renders itself with. Tile currently only exposes a number, so this is derived
 * externally
 * (see {@link TableLayoutGenerator#getColor}) rather than stored on Tile
 * itself. If Tile later grows a real colour/type field, that derivation is the
 * one place to update.
 */
public enum PocketColor {
    RED,
    BLACK,
    GREEN
}

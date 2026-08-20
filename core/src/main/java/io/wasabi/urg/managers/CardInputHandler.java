package io.wasabi.urg.managers;

import java.util.List;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.elements.card.Card;
import io.wasabi.urg.elements.game.Tile;
import io.wasabi.urg.state.RunState;
import io.wasabi.urg.ui.CardLayout;

public class CardInputHandler extends InputAdapter {
    private final RunState runState;
    private final Viewport viewport;

    private Card draggedCard;
    private final Vector2 dragOffset = new Vector2();

    public CardInputHandler(RunState runState, Viewport viewport) {
        this.runState = runState;
        this.viewport = viewport;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 world = screenToWorld(screenX, screenY);

        List<Card> cards = runState.getOwnedCards();
        for (int i = cards.size() - 1; i >= 0; i--) {
            Card card = cards.get(i);
            if (card.contains(world.x, world.y)) {
                draggedCard = card;
                draggedCard.setDragging(true);
                draggedCard.getTooltip().hide();
                dragOffset.set(world.x - card.getX(), world.y - card.getY());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector2 world = screenToWorld(screenX, screenY);

        List<Card> cards = runState.getOwnedCards();
        for (int i = cards.size() - 1; i >= 0; i--) {
            Card card = cards.get(i);
            if (card.contains(world.x, world.y)) {
                card.getTooltip().show();
                break;
            } else {
                card.getTooltip().hide();
            }
        }

        for (Tile tile : runState.getTiles()) {
            float[] verts = tile.getRegion().getVertices();
            int length = verts.length;
            // cut vertices and make anticlockwise
            float[] cutVerts = new float[] {
                verts[0], verts[1], verts[length - 4], verts[length - 3],
                verts[length - 2], verts[length - 1], verts[2], verts[3]
            };
            if (Intersector.isPointInPolygon(cutVerts, 0, cutVerts.length, world.x, world.y)) {
                tile.getTooltip().show();
                break;
            } else {
                tile.getTooltip().hide();
            }
        }

        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (draggedCard == null) return false;

        Vector2 world = screenToWorld(screenX, screenY);
        draggedCard.setPosition(world.x - dragOffset.x, world.y - dragOffset.y);

        // Reorder the hand live so other cards shift to make space
        List<Card> cards = runState.getOwnedCards();
        int currentIndex = cards.indexOf(draggedCard);
        int closestIndex = CardLayout.getClosestIndex(
            draggedCard.getX(), cards.size(), viewport.getWorldWidth());

        if (closestIndex != currentIndex) {
            runState.reorderCard(draggedCard, closestIndex);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (draggedCard == null) return false;

        draggedCard.setDragging(false);
        draggedCard.getTooltip().show();
        draggedCard = null;
        return true;
    }

    private Vector2 screenToWorld(int screenX, int screenY) {
        Vector3 world = viewport.unproject(new Vector3(screenX, screenY, 0));
        return new Vector2(world.x, world.y);
    }
}

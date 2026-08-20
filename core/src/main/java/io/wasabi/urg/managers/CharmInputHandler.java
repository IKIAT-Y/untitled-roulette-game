package io.wasabi.urg.managers;

import java.util.List;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.wasabi.urg.elements.charm.AbstractCharm;
import io.wasabi.urg.elements.game.Wheel;
import io.wasabi.urg.state.RunState;
import io.wasabi.urg.ui.CharmLayout;

public class CharmInputHandler extends InputAdapter {
    private final RunState runState;
    private final Viewport viewport;
    private final Wheel wheel;

    private AbstractCharm draggedCharm;
    private final Vector2 dragOffset = new Vector2();
    private boolean hoveringWheel;

    public CharmInputHandler(RunState runState, Viewport viewport, Wheel wheel) {
        this.runState = runState;
        this.viewport = viewport;
        this.wheel = wheel;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 world = screenToWorld(screenX, screenY);

        List<AbstractCharm> charms = runState.getOwnedCharms();
        for (int i = charms.size() - 1; i >= 0; i--) {
            AbstractCharm charm = charms.get(i);
            if (charm.contains(world.x, world.y)) {
                draggedCharm = charm;
                draggedCharm.setDragging(true);
                dragOffset.set(world.x - charm.getX(), world.y - charm.getY());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (draggedCharm == null) return false;

        Vector2 world = screenToWorld(screenX, screenY);
        draggedCharm.setPosition(world.x - dragOffset.x, world.y - dragOffset.y);
        hoveringWheel = wheel.containsPoint(world);

        // Reorder the hand live so other cards shift to make space
        List<AbstractCharm> charms = runState.getOwnedCharms();
        int currentIndex = charms.indexOf(draggedCharm);
        int closestIndex = CharmLayout.getClosestIndex(
            draggedCharm.getX(), charms.size(), viewport.getWorldWidth());

        if (closestIndex != currentIndex) {
            runState.reorderCharm(draggedCharm, closestIndex);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (draggedCharm == null) return false;

        AbstractCharm charm = draggedCharm;
        Vector2 world = screenToWorld(screenX, screenY);
        charm.setDragging(false);
        draggedCharm = null;
        hoveringWheel = false;

        if (wheel.containsPoint(world) && runState.removeCharm(charm)) {
            charm.activate();
        }
        return true;
    }

    public boolean isHoveringWheel(AbstractCharm charm) {
        return draggedCharm == charm && hoveringWheel;
    }

    private Vector2 screenToWorld(int screenX, int screenY) {
        Vector3 world = viewport.unproject(new Vector3(screenX, screenY, 0));
        return new Vector2(world.x, world.y);
    }
}

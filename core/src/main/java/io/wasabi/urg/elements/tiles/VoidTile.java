package io.wasabi.urg.elements.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.wasabi.urg.Roulette;
import io.wasabi.urg.elements.game.Tile;

public class VoidTile extends TileType {
    private static final Color RIM_COLOR = new Color(0.45f, 0.05f, 0.6f, 0.8f);
    private static final Texture VOID_TEXTURE = new Texture(Gdx.files.internal("tiles/VoidTile.png"));
    private final Tile tile;
    private final Mesh mesh;

    public VoidTile(TileType type, Tile tile) {
        this.tile = tile;
        this.setColour(type.getColour());
        this.setNumber(type.getNumber());
        this.textureVerticalSteps = 2;
        tooltip.setDescriptionVisible(true);
        tooltip.setDescription("Gain [RED]3x [BLACK]payout. This tile is destroyed when landed on");
        tooltip.addType("VOID", Color.WHITE, RIM_COLOR);

        mesh = new Mesh(false, 2000, 2000,
                new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
    }

    @Override
    public void setRegion(float[] vertices, short[] indices) {
        super.setRegion(vertices, indices);

        TextureRegion texRegion = new TextureRegion(VOID_TEXTURE);
        mesh.setVertices(textureWrapVertices(vertices, texRegion));
        mesh.setIndices(indices);
    }

    @Override
    public void drawOverlay() {
        VOID_TEXTURE.bind();
        mesh.render(POLY_BATCH.getShader(), GL20.GL_TRIANGLES);
    }

    @Override
    public void drawOutline() {
        PolygonRegion polyRegion = getRegion();
        if (polyRegion == null) {
            return;
        }

        float[] vertices = polyRegion.getVertices();
        ShapeRenderer renderer = RENDERER_MANAGER.getShapeRenderer();
        Gdx.gl.glLineWidth(1.2f);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(RIM_COLOR);
        for (int i = 0; i + 7 < vertices.length; i += 4) {
            renderer.line(vertices[i], vertices[i + 1], vertices[i + 4], vertices[i + 5]);
            renderer.line(vertices[i + 2], vertices[i + 3], vertices[i + 6], vertices[i + 7]);
        }

        int lastOuter = vertices.length - 2;
        renderer.line(vertices[0], vertices[1], vertices[2], vertices[3]);
        renderer.line(vertices[vertices.length - 4], vertices[vertices.length - 3],
                vertices[lastOuter], vertices[lastOuter + 1]);
        renderer.end();
        Gdx.gl.glLineWidth(1f);
    }

    @Override
    public void onLanded() {
        Roulette.getInstance().getRunState().removeTile(tile);
    }

    @Override
    public float getBetMultiplier() {
        return 3f;
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }
}

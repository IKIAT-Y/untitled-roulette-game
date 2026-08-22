package io.wasabi.urg.elements.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class NullTile extends TileType {
    private final static Texture NULL_TEXTURE = new Texture(Gdx.files.internal("tiles/NullTile.png"));

    private final Mesh mesh;

    public NullTile() {
        super();
        this.betMultiplier = 0f;

        mesh = new Mesh(false, 2000, 2000,
            new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
            new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
        );
    }

    @Override
    public void setRegion(float[] vertices, short[] indices) {
        super.setRegion(vertices, indices);
        TextureRegion texRegion = new TextureRegion(NULL_TEXTURE);
        mesh.setVertices(textureWrapVertices(vertices, texRegion));
        mesh.setIndices(indices);
    }

    @Override
    public void drawTextures() {
        //super.drawTextures();
    }

    @Override
    public void drawOverlay() {
        NULL_TEXTURE.bind();
        mesh.render(POLY_BATCH.getShader(), GL20.GL_TRIANGLES);
    }

    @Override
    public boolean isRed() {
        return false;
    }

    @Override
    public boolean isBlack() {
        return false;
    }

    // The base texture field is never actually drawn (drawTextures() is a no-op
    // here; the real look comes from the nullTexture mesh in drawOverlay()), so
    // anything pulling "this tile's texture" generically (e.g. the betting table)
    // needs this override to see the real pattern instead of a flat placeholder.
    @Override
    public Texture getTexture() {
        return nullTexture;
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }
}

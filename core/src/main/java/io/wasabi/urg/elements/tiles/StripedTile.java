package io.wasabi.urg.elements.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class StripedTile extends TileType {
    protected static Texture STRIPED_TEXTURE = new Texture(Gdx.files.internal("tiles/StripedTile.png"));

    private final Mesh mesh;

    public StripedTile() {
        super();
        this.betMultiplier = 1.5f;
        tooltip.setDescriptionVisible(true);
        tooltip.setDescription("Counts as both [RED]RED [BLACK]and BLACK");
        tooltip.addType("[RED]ST[BLACK]RI[RED]PE[BLACK]D", Color.BLACK, new Color(0xFFFFFFFF));

        mesh = new Mesh(false, 2000, 2000,
			new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
        );
    }

    @Override
    public void setRegion(float[] vertices, short[] indices) {
        super.setRegion(vertices, indices);
        TextureRegion texRegion = new TextureRegion(STRIPED_TEXTURE);
        mesh.setVertices(textureWrapVertices(vertices, texRegion));
        mesh.setIndices(indices);
    }

    @Override
    public void drawTextures() {
        //super.drawTextures();
    }

    @Override
    public void drawOverlay() {
        STRIPED_TEXTURE.bind();
        mesh.render(POLY_BATCH.getShader(), GL20.GL_TRIANGLES);
    }

    @Override
    public boolean isRed() {
        return true;
    }

    @Override
    public boolean isBlack() {
        return true;
    }

    // The base texture field is never actually drawn (drawTextures() is a no-op
    // here; the real look comes from the stripedTexture mesh in drawOverlay()), so
    // anything pulling "this tile's texture" generically (e.g. the betting table)
    // needs this override to see the real pattern instead of a flat placeholder.
    @Override
    public Texture getTexture() {
        return stripedTexture;
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }
}

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

import io.wasabi.urg.Roulette;

public class GoldTile extends TileType {
    protected Texture stripedTexture;

    private final Mesh mesh;

    public GoldTile() {
        super();
        this.betMultiplier = 1.5f;
        Color col = new Color(1f, 1f, 1f, 0.8f);
        this.textureColor = col.toFloatBits();

        stripedTexture = new Texture(Gdx.files.internal("tiles/GoldTile.png"));
        stripedTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.Repeat);

        mesh = new Mesh(false, 2000, 2000,
			new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
        );
    }

    @Override
    public void setRegion(float[] vertices, short[] indices) {
        super.setRegion(vertices, indices);
        TextureRegion texRegion = new TextureRegion(stripedTexture);
        mesh.setVertices(textureWrapVertices(vertices, texRegion));
        mesh.setIndices(indices);
    }

    @Override
    public void drawTextures() {
        super.drawTextures();
    }

    @Override
    public void drawOverlay() {
        stripedTexture.bind();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        mesh.render(POLY_BATCH.getShader(), GL20.GL_TRIANGLES);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void onLanded() {
        Roulette.getInstance().getRunState().addTickets(1);
    }
}

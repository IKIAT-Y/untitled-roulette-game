package io.wasabi.urg.elements.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MetallicTile extends TileType {
    protected Texture metallicTexture;
    protected PolygonRegion metallicRegion;

    public MetallicTile() {
        super();
        this.betMultiplier = 1.5f;

        metallicTexture = new Texture(Gdx.files.internal("tiles/MetallicTile.png"));
        metallicTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.Repeat);
    }

    @Override
    public void setRegion(float[] vertices, short[] indices) {
        super.setRegion(vertices, indices);
        metallicRegion = new PolygonRegion(new TextureRegion(metallicTexture), vertices, indices);
    }

    @Override
    public void drawTextures() {
        super.drawTextures();
        //POLY_BATCH.setColor(1, 1, 1, 0.6f);
        POLY_BATCH.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
        POLY_BATCH.draw(metallicRegion, 0, 0);
        POLY_BATCH.setColor(1, 1, 1, 1);
        POLY_BATCH.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}

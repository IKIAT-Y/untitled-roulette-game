package io.wasabi.urg.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public final class Background {
    private final SpriteBatch batch;
    private final Texture backgroundImg;
    private final Matrix4 previousProjection = new Matrix4();
    private final Matrix4 previousTransform = new Matrix4();
    private final Matrix4 screenProjection = new Matrix4();
    private final Matrix4 identityTransform = new Matrix4().idt();
    private float time = 0f;

    public Background(SpriteBatch batch) {
        this.batch = batch;

        backgroundImg = new Texture(Gdx.files.internal("ui/board.png"));
    }

    public void render(float delta) {
        previousProjection.set(batch.getProjectionMatrix());
        previousTransform.set(batch.getTransformMatrix());
        screenProjection.setToOrtho2D(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(screenProjection);
        batch.setTransformMatrix(identityTransform);

        time += delta;

        batch.begin();
        batch.draw(backgroundImg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        batch.setShader(null);
        batch.setProjectionMatrix(previousProjection);
        batch.setTransformMatrix(previousTransform);
    }

    public void dispose() { backgroundImg.dispose(); }
}

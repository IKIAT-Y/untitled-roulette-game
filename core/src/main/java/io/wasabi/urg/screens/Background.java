package io.wasabi.urg.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public final class Background {
    private final SpriteBatch batch;
    private final ShaderProgram shader;
    private final Texture white;
    private final Matrix4 previousProjection = new Matrix4();
    private final Matrix4 previousTransform = new Matrix4();
    private final Matrix4 screenProjection = new Matrix4();
    private final Matrix4 identityTransform = new Matrix4().idt();
    private float time = 0f;

    public Background(SpriteBatch batch) {
        this.batch = batch;
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(
            Gdx.files.internal("shaders/liquid.vert"),
            Gdx.files.internal("shaders/liquid.frag")
        );
        if (!shader.isCompiled()) throw new IllegalStateException(shader.getLog());

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        white = new Texture(pm);
        pm.dispose();
    }

    public void render(float delta) {
        previousProjection.set(batch.getProjectionMatrix());
        previousTransform.set(batch.getTransformMatrix());
        screenProjection.setToOrtho2D(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(screenProjection);
        batch.setTransformMatrix(identityTransform);

        time += delta;
        shader.bind();
        shader.setUniformf("u_time", time);
        shader.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setShader(shader);
        batch.begin();
        batch.draw(white, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        batch.setShader(null);
        batch.setProjectionMatrix(previousProjection);
        batch.setTransformMatrix(previousTransform);
    }

    public void dispose() { shader.dispose(); white.dispose(); }
}

/*
* solarsystem - A real-time solar system simulation.
* Copyright (C) 2023-2023 Filippo Barbari <filippo.barbari@gmail.com>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ledmington.solarsystem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ledmington.solarsystem.model.Body;
import com.ledmington.solarsystem.model.SolarSystem;
import com.ledmington.solarsystem.utils.FormatUtils;
import com.ledmington.solarsystem.utils.MiniLogger;

public final class MainScreen extends AbstractScreen implements InputProcessor {

    // 10'000km : 1
    private static final double scale = 1.0 / 10_000_000.0;
    private static final double inverse_scale = 1.0 / scale;
    private static final float VIEWPORT_WIDTH = (float) Constants.TARGET_RESOLUTION_WIDTH * (float) inverse_scale;
    private static final float VIEWPORT_HEIGHT = (float) Constants.TARGET_RESOLUTION_HEIGHT * (float) inverse_scale;
    private static final MiniLogger logger = MiniLogger.getLogger("MainScreen");

    private final PerspectiveCamera camera;
    private final float initialCameraSpeed = 0.1f;
    private float cameraSpeed = initialCameraSpeed;
    private final float minFOV = 20.0f; // minimum field of view in degrees
    private final float maxFOV = 90.0f; // maximum field of view in degrees

    private final Viewport viewport;
    private final Map<Body, ModelInstance> bodiesToModels = new HashMap<>();
    private final Environment environment;
    private boolean loading;
    private final String skyBoxFileName = Constants.MODELS_FOLDER + "/skybox.obj";
    private ModelInstance skyBox;
    private final BitmapFont font = toBeDisposed(new BitmapFont());

    public MainScreen() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camera = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(
                SolarSystem.EARTH.position().cpy().scl((float) scale).add(10.0f));
        camera.lookAt(SolarSystem.EARTH.position().cpy().scl((float) scale));
        camera.near = 0.1f;
        camera.far = 1_000_000.0f;
        camera.update();

        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);

        Gdx.input.setInputProcessor(this);

        // creating and adding the models
        for (final Body b : SolarSystem.planets()) {
            final float scaledRadius = (float) (b.radius() * scale);

            Material material;
            if (b.hasTexture()) {
                logger.debug(
                        "adding texture of %s (%s) to be loaded",
                        b.name().get(), b.texture().get());
                assetManager.load(b.texture().get(), Texture.class);
                material = new Material();
            } else {
                logger.debug(b.name().get() + " has no texture, using its color");
                material = new Material(
                        new ColorAttribute(ColorAttribute.Diffuse, b.color().get()));
            }
            final Model model = toBeDisposed(new ModelBuilder()
                    .createSphere(
                            scaledRadius,
                            scaledRadius,
                            scaledRadius,
                            20,
                            20,
                            material,
                            Usage.Position | Usage.Normal | Usage.TextureCoordinates));

            final ModelInstance instance =
                    new ModelInstance(model, (float) (b.position().x * scale), (float) (b.position().y * scale), (float)
                            (b.position().z * scale));
            bodiesToModels.put(b, instance);
        }

        logger.debug("adding skybox to be loaded");
        assetManager.load(skyBoxFileName, Model.class);
        loading = true;
    }

    private void doneLoading() {
        skyBox = new ModelInstance(assetManager.get(skyBoxFileName, Model.class));

        SolarSystem.planets().stream().filter(b -> b.hasTexture()).forEach(b -> {
            bodiesToModels
                    .get(b)
                    .materials
                    .first()
                    .set(new TextureAttribute(
                            TextureAttribute.Diffuse,
                            assetManager.get(b.texture().get(), Texture.class)));
        });
        loading = false;
    }

    @Override
    public void show() {
        // intentionally empty
    }

    @Override
    public void render(final float delta) {
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        viewport.apply(false);

        if (loading) {
            if (assetManager.update(Constants.ONE_OVER_SIXTY)) {
                doneLoading();
            } else {
                spriteBatch.begin();
                font.setColor(Color.WHITE);
                font.draw(
                        spriteBatch,
                        String.format("Loading assets: %3.1f %%", assetManager.getProgress() * 100.0f),
                        0.0f,
                        viewport.getScreenHeight() - 20.0f);
                spriteBatch.end();
            }
        }

        // rendering skybox
        if (skyBox != null) {
            modelBatch.begin(camera);
            skyBox.transform.setToTranslation(camera.position);
            skyBox.transform.scl(1_000.0f);
            modelBatch.render(skyBox);
            modelBatch.end();
        }

        spriteBatch.begin();
        font.setColor(Color.GREEN);
        font.draw(spriteBatch, String.format("FPS: %3.1f", 1 / delta), 0.0f, viewport.getScreenHeight());
        spriteBatch.end();

        final Map<Body, Vector2> bodyToLabelPosition = new HashMap<>();

        modelBatch.begin(camera);
        // rendering planets
        for (final Map.Entry<Body, ModelInstance> entry : bodiesToModels.entrySet()) {
            final Body b = entry.getKey();
            final ModelInstance instance = entry.getValue();
            if (isVisible(camera, instance)) {
                modelBatch.render(instance, environment);
                final Vector3 labelPosition = viewport.project(new Vector3(
                        (float) ((b.position().x + b.radius()) * scale),
                        (float) ((b.position().y + b.radius()) * scale),
                        0.0f));
                bodyToLabelPosition.put(b, new Vector2(labelPosition.x, labelPosition.y));
            }
        }
        modelBatch.end();

        for (final Entry<Body, Vector2> entry : bodyToLabelPosition.entrySet()) {
            final Body b = entry.getKey();
            final Vector2 labelPosition = entry.getValue();

            // draw red label
            spriteBatch.begin();
            font.setColor(1.0f, 0.0f, 0.0f, 1.0f);
            font.draw(spriteBatch, b.name().get(), labelPosition.x + 10, labelPosition.y + 10);
            spriteBatch.end();

            final Vector3 tmp = viewport.project(
                    new Vector3((float) (b.position().x * scale), (float) (b.position().y * scale), 0.0f));
            final Vector2 bodyPositionOnScreen = new Vector2(tmp.x, tmp.y);
            final float circleRadius = (float) (b.radius() * scale + 10.0);

            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            // draw white line from label to body
            shapeRenderer.line(bodyPositionOnScreen, labelPosition);

            // draw circle around body
            shapeRenderer.circle(bodyPositionOnScreen.x, bodyPositionOnScreen.y, circleRadius);
            shapeRenderer.end();
        }

        final Body closestBody = SolarSystem.planets().stream()
                .sorted(Comparator.comparing(
                        b -> b.position().cpy().scl((float) scale).dst(camera.position)))
                .findFirst()
                .orElseThrow();

        final long distance = (long)
                (closestBody.position().cpy().scl((float) scale).dst(camera.position) - closestBody.radius() * scale);
        final long distanceToSun =
                (long) (SolarSystem.SUN.position().cpy().scl((float) scale).dst(camera.position)
                        - SolarSystem.SUN.radius() * scale);
        final long distanceToEarth =
                (long) (SolarSystem.EARTH.position().cpy().scl((float) scale).dst(camera.position)
                        - SolarSystem.EARTH.radius() * scale);
        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.draw(
                spriteBatch,
                String.format("Closest body: %s (%s km)", closestBody.name().get(), FormatUtils.thousands(distance)),
                0.0f,
                60.0f);
        font.draw(
                spriteBatch,
                String.format("Distance to Sun: %s km", FormatUtils.thousands(distanceToSun)),
                0.0f,
                40.0f);
        font.draw(
                spriteBatch,
                String.format("Distance to Earth: %s km", FormatUtils.thousands(distanceToEarth)),
                0.0f,
                20.0f);
        spriteBatch.end();

        drawZoomSlider();

        handleInputs();

        bodyToLabelPosition.clear();
    }

    private void drawZoomSlider() {
        final float xPosition = viewport.getScreenWidth() * 0.98f;
        final float yBottom = viewport.getScreenHeight() - (viewport.getScreenHeight() * 0.98f);
        final float sliderWidth = 10.0f;
        final float sliderHeight = viewport.getScreenHeight() * 0.2f;
        final float knobRadius = sliderWidth * 1.1f;
        final float knobY = MathUtils.map(minFOV, maxFOV, yBottom + sliderHeight, yBottom, camera.fieldOfView);

        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 0.2f);
        shapeRenderer.arc(
                (xPosition + xPosition + sliderWidth) / 2, yBottom + sliderHeight, sliderWidth / 2, 0.0f, 180.0f, 10);
        shapeRenderer.rect(xPosition, yBottom, sliderWidth, sliderHeight);
        shapeRenderer.arc((xPosition + xPosition + sliderWidth) / 2, yBottom, sliderWidth / 2, 180.0f, 180.0f, 10);
        shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        shapeRenderer.circle((xPosition + xPosition + sliderWidth) / 2, knobY, knobRadius, 10);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    private void handleInputs() {
        // forward
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
            camera.position.add(camera.direction.cpy().scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }
        // backward
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
            camera.position.sub(camera.direction.cpy().scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }
        // left
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
            camera.position.add(camera.direction.cpy().rotate(camera.up, -90.0f).scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }
        // right
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
            camera.position.add(camera.direction.cpy().rotate(camera.up, 90.0f).scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }
        // up
        if (Gdx.input.isKeyPressed(Keys.SPACE)) {
            camera.position.add(camera.up.cpy().scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }
        // down
        if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
            camera.position.sub(camera.up.cpy().scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }

        // if the mouse is over a planet we highlight it
        for (Entry<Body, ModelInstance> entry : bodiesToModels.entrySet()) {
            final Body b = entry.getKey();
            final ModelInstance instance = entry.getValue();
            if (!isVisible(camera, instance)) {
                continue;
            }

            final Vector2 bodyPositionOnScreen =
                    viewport.project(new Vector2((float) (b.position().x * scale), (float) (b.position().y * scale)));
            bodyPositionOnScreen.y = viewport.getScreenHeight() - bodyPositionOnScreen.y;

            final double distanceFromCamera = (double) camera.position.dst(b.position());
            final float hitboxRadius =
                    MathUtils.clamp((float) (b.radius() * distanceFromCamera * scale), 10.0f, 100.0f);
            logger.debug("%s -> %f", bodyPositionOnScreen.toString(), hitboxRadius);

            if (bodyPositionOnScreen.dst(Gdx.input.getX(), Gdx.input.getY()) < hitboxRadius) {
                shapeRenderer.begin(ShapeType.Filled);
                shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 0.1f);
                shapeRenderer.circle(bodyPositionOnScreen.x, bodyPositionOnScreen.y, hitboxRadius);
                shapeRenderer.end();

                // if we click on the planet we look at it
                if (Gdx.input.isTouched()) {
                    camera.lookAt(b.position().cpy().scl((float) scale));
                }
            }
        }
    }

    /**
     * Returns true if the given model is visible by the given camera.
     * False otherwise.
     */
    private boolean isVisible(final Camera cam, final ModelInstance instance) {
        instance.transform.getTranslation(Vector3.Zero);
        return cam.frustum.pointInFrustum(Vector3.Zero);
    }

    public void resize(final int width, final int height) {
        viewport.update(width, height, false);
    }

    @Override
    public boolean keyDown(int keycode) {
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        cameraSpeed = initialCameraSpeed;
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // amountX is currently ignored

        camera.fieldOfView = Math.min(maxFOV, Math.max(minFOV, camera.fieldOfView + amountY));
        camera.update();

        return false;
    }
}

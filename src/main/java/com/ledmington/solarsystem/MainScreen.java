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
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ledmington.solarsystem.model.Body;
import com.ledmington.solarsystem.model.SolarSystem;
import com.ledmington.solarsystem.utils.MiniLogger;

public final class MainScreen extends AbstractScreen implements InputProcessor {

    // 10'000km : 1
    private static final double scale = 1.0 / 10_000_000.0;
    private static final MiniLogger logger = MiniLogger.getLogger("MainScreen");

    private final PerspectiveCamera camera;
    private final float initialCameraSpeed = 0.1f;
    private float cameraSpeed = initialCameraSpeed;
    private final float minFOV = 20.0f; // minimum field of view in degrees
    private final float maxFOV = 90.0f; // maximum field of view in degrees

    private final Viewport viewport;
    private final Renderable renderable;
    private final Map<Body, ModelInstance> bodiesToModels = new HashMap<>();
    private final Map<Body, ModelInstance> debuggingCopy = new HashMap<>();
    private final BitmapFont font = new BitmapFont();
    private final Environment environment;
    private boolean loading;
    private final String skyBoxFileName = Constants.MODELS_FOLDER + "/skybox.obj";
    private ModelInstance skyBox;

    public MainScreen() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camera = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(100.0f, 100.0f, 100.0f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 1_000_000.0f;
        camera.update();

        viewport = new FillViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);

        Gdx.input.setInputProcessor((InputProcessor) this);

        renderable = new Renderable();
        renderable.environment = null;
        renderable.worldTransform.idt();

        // creating and adding the models
        for (final Body b : SolarSystem.planets()) {
            final float scaledRadius = (float) (b.radius() * scale);

            Model model;
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
            model = new ModelBuilder()
                    .createSphere(
                            scaledRadius,
                            scaledRadius,
                            scaledRadius,
                            20,
                            20,
                            material,
                            Usage.Position | Usage.Normal | Usage.TextureCoordinates);

            final ModelInstance instance =
                    new ModelInstance(model, (int) (b.position().x * scale), (int) (b.position().y * scale), (int)
                            (b.position().z * scale));
            bodiesToModels.put(b, instance);
            debuggingCopy.put(b, instance);

            final NodePart blockPart = model.nodes.get(0).parts.get(0);
            blockPart.setRenderable(renderable);
        }

        logger.debug("adding skybox to be loaded");
        assetManager.load(skyBoxFileName, Model.class);
        loading = true;
    }

    private void doneLoading() {
        logger.debug("done loading");
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
        viewport.apply();

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
                        40.0f);
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

        /**
         * The bodies don't have a proper hitbox, we consider the circle we draw
         * around them as an hitbox.
         * The Vector3 stored as value is in fact the 2D position and the radius of the circle.
         */
        final Map<Body, Vector3> bodyToHitbox = new HashMap<>();

        modelBatch.begin(camera);
        // rendering planets
        for (final Map.Entry<Body, ModelInstance> entry : bodiesToModels.entrySet()) {
            final Body b = entry.getKey();
            final ModelInstance instance = entry.getValue();
            if (isVisible(camera, instance)) {
                modelBatch.render(instance, environment);
                final Vector3 labelPosition = camera.project(new Vector3(
                        (float) ((b.position().x + b.radius()) * scale),
                        (float) ((b.position().y + b.radius()) * scale),
                        (float) (b.position().z * scale)));
                bodyToLabelPosition.put(b, new Vector2(labelPosition.x, labelPosition.y));
            }
        }
        modelBatch.end();

        spriteBatch.begin();
        for (final Entry<Body, Vector2> entry : bodyToLabelPosition.entrySet()) {
            final Body b = entry.getKey();
            final Vector2 labelPosition = entry.getValue();
            // draw red label
            font.setColor(1.0f, 0.0f, 0.0f, 1.0f);
            font.draw(spriteBatch, b.name().get(), labelPosition.x + 10, labelPosition.y + 10);
        }
        spriteBatch.end();

        shapeRenderer.begin(ShapeType.Line);
        for (final Entry<Body, Vector2> entry : bodyToLabelPosition.entrySet()) {
            final Body b = entry.getKey();
            final Vector2 labelPosition = entry.getValue();
            final Vector3 tmp = camera.project(
                    new Vector3((float) (b.position().x * scale), (float) (b.position().y * scale), 0.0f));
            final Vector2 bodyPositionOnScreen = new Vector2(tmp.x, tmp.y);

            // draw white line from label to body
            shapeRenderer.line(bodyPositionOnScreen, labelPosition);

            // draw circle around body
            final float circleRadius = (float) (b.radius() * scale + 10);
            shapeRenderer.circle(bodyPositionOnScreen.x, bodyPositionOnScreen.y, circleRadius);

            bodyToHitbox.put(b, new Vector3(bodyPositionOnScreen.x, bodyPositionOnScreen.y, circleRadius));
        }
        shapeRenderer.end();

        final Body closestBody = SolarSystem.planets().stream()
                .sorted(Comparator.comparing(
                        b -> new Vector3(b.position()).scl((float) scale).dst(camera.position)))
                .findFirst()
                .orElseThrow();
        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.draw(
                spriteBatch,
                String.format(
                        "Closest body: %s (%e)",
                        closestBody.name().get(),
                        new Vector3(closestBody.position()).scl((float) scale).dst(camera.position)),
                0.0f,
                20.0f);
        spriteBatch.end();

        // float viewportX = (2.0f * Gdx.input.getX()) / viewport.getScreenWidth() - 1.0f;
        // float viewportY = (2.0f * (viewport.getScreenHeight() - Gdx.input.getY())) / viewport.getScreenHeight() -
        // 1.0f;
        // System.out.println(viewportX + " , " + viewportY);

        /*****************
         * HANDLE INPUTS *
         *****************/

        // if the mouse is over a planet we highlight it
        for (Entry<Body, Vector3> entry : bodyToHitbox.entrySet()) {
            final Body b = entry.getKey();
            final Vector2 bodyPositionOnScreen = new Vector2(entry.getValue().x, entry.getValue().y);
            final float hitboxRadius = entry.getValue().z;

            if (bodyPositionOnScreen.dst(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY())
                    < hitboxRadius) {
                shapeRenderer.begin(ShapeType.Filled);
                shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 0.1f);
                shapeRenderer.circle(bodyPositionOnScreen.x, bodyPositionOnScreen.y, hitboxRadius);
                shapeRenderer.end();

                // if we click on the planet we look at it
                if (Gdx.input.isTouched()) {
                    camera.lookAt(new Vector3(b.position()).scl((float) scale));
                }
            }
        }

        camera.update();

        /***********************
         * END HANDLING INPUTS *
         ***********************/
        bodyToLabelPosition.clear();
        bodyToHitbox.clear();
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
        viewport.update(width, height);
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
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
            camera.position.add(new Vector3(camera.direction).scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
            camera.position.sub(new Vector3(camera.direction).scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
            camera.position.add(
                    new Vector3(camera.direction).rotate(camera.up, -90.0f).scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
            camera.position.add(
                    new Vector3(camera.direction).rotate(camera.up, 90.0f).scl(cameraSpeed));
            cameraSpeed += initialCameraSpeed;
        }
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

        logger.debug("%f", camera.fieldOfView);

        camera.fieldOfView = Math.min(maxFOV, Math.max(minFOV, camera.fieldOfView + amountY));
        camera.update();

        return false;
    }
}

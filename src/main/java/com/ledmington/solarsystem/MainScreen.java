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
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.ledmington.solarsystem.model.Body;
import com.ledmington.solarsystem.model.SolarSystem;

public final class MainScreen implements Screen {

    // 10'000km : 1
    private static final double scale = 1.0 / 10_000_000.0;

    private final AssetManager assetManager;
    private final PerspectiveCamera camera;
    private final float initialCameraSpeed = 0.01f;
    private float cameraSpeed = initialCameraSpeed;
    private final CameraInputController camController;
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final Renderable renderable;
    private final ModelBatch modelBatch;
    private final Map<ModelInstance, Body> modelToBody = new HashMap<>();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Environment environment;
    private boolean loading;
    private final String skyBoxFileName = "models/skybox.obj";
    private ModelInstance skyBox;
    private final ShapeRenderer shapeRenderer;

    public MainScreen() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(100.0f, 100.0f, 100.0f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 1_000_000.0f;
        camera.update();

        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);

        renderable = new Renderable();
        renderable.environment = null;
        renderable.worldTransform.idt();

        // creating and adding the models
        for (final Body b : SolarSystem.planets) {
            final float scaledRadius = (float) (b.radius() * scale);
            System.out.println(b.name() + " -> " + scaledRadius + " m -> " + ((int) (b.position().x * scale)) + " m");

            final Model model = new ModelBuilder()
                    .createSphere(
                            scaledRadius,
                            scaledRadius,
                            scaledRadius,
                            20,
                            20,
                            new Material(new ColorAttribute(ColorAttribute.Diffuse, b.color())),
                            Usage.Position | Usage.Normal | Usage.TextureCoordinates);
            models.add(model);
            final ModelInstance instance =
                    new ModelInstance(model, (int) (b.position().x * scale), (int) (b.position().y * scale), (int)
                            (b.position().z * scale));
            instances.add(instance);
            modelToBody.put(instance, b);

            final NodePart blockPart = model.nodes.get(0).parts.get(0);
            blockPart.setRenderable(renderable);
        }

        modelBatch = new ModelBatch();

        shapeRenderer = new ShapeRenderer();

        assetManager = new AssetManager();
        assetManager.load(skyBoxFileName, Model.class);
        loading = true;
    }

    private void doneLoading() {
        skyBox = new ModelInstance(assetManager.get(skyBoxFileName, Model.class));

        loading = false;
    }

    @Override
    public void show() {
        // intentionally empty
    }

    @Override
    public void render(final float delta) {
        if (loading && assetManager.update()) {
            doneLoading();
        }

        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

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
        font.draw(spriteBatch, String.format("FPS: %3.1f", 1 / delta), 0.0f, 20.0f);
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
        for (final ModelInstance instance : instances) {
            if (isVisible(camera, instance)) {
                modelBatch.render(instance, environment);
                final Body b = modelToBody.get(instance);
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
            font.draw(spriteBatch, b.name(), labelPosition.x + 10, labelPosition.y + 10);
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

        final Body closestBody = SolarSystem.planets.stream()
                .sorted(Comparator.comparing(
                        b -> new Vector3(b.position()).scl((float) scale).dst(camera.position)))
                .findFirst()
                .orElseThrow();
        System.out.printf(
                "Closest body: %s (%e)\n",
                closestBody.name(),
                new Vector3(closestBody.position()).scl((float) scale).dst(camera.position));

        /*****************
         * HANDLE INPUTS *
         *****************/
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

        if (!Gdx.input.isKeyPressed(Keys.W)
                && !Gdx.input.isKeyPressed(Keys.A)
                && !Gdx.input.isKeyPressed(Keys.S)
                && !Gdx.input.isKeyPressed(Keys.D)
                && !Gdx.input.isKeyPressed(Keys.UP)
                && !Gdx.input.isKeyPressed(Keys.DOWN)
                && !Gdx.input.isKeyPressed(Keys.RIGHT)
                && !Gdx.input.isKeyPressed(Keys.LEFT)) {
            cameraSpeed = initialCameraSpeed;
        }

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

    @Override
    public void resize(final int width, final int height) {
        // intentionally empty
    }

    @Override
    public void pause() {
        // intentionally empty
    }

    @Override
    public void resume() {
        // intentionally empty
    }

    @Override
    public void hide() {
        // intentionally empty
    }

    @Override
    public void dispose() {
        font.dispose();
        models.forEach(m -> m.dispose());
        modelBatch.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }
}

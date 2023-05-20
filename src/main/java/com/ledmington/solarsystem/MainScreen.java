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
import java.util.function.Function;

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
    private static final float VIEWPORT_WIDTH = (float) Constants.TARGET_RESOLUTION_WIDTH * 0.1f;
    private static final float VIEWPORT_HEIGHT = (float) Constants.TARGET_RESOLUTION_HEIGHT * 0.1f;
    private static final MiniLogger logger = MiniLogger.getLogger("MainScreen");

    private final PerspectiveCamera camera;
    private final float initialCameraSpeed = 0.1f;
    private float cameraSpeed = initialCameraSpeed;
    private final float minFOV = 20.0f; // minimum field of view in degrees
    private final float maxFOV = 80.0f; // maximum field of view in degrees

    private final Viewport viewport;
    private final Map<Body, ModelInstance> bodiesToModels = new HashMap<>();
    private final Environment environment;
    private boolean loading;
    private final String skyBoxFileName = Constants.MODELS_FOLDER + "/skybox.obj";
    private ModelInstance skyBox;
    private final BitmapFont font = toBeDisposed(new BitmapFont());
    private final float solarsystemWidth = (float) (SolarSystem.PLUTO.position().x * scale);
    private float mouseX;
    private float mouseY;
    private boolean isTouched = false;

    public MainScreen() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camera = new PerspectiveCamera(45.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
                        b.name().orElseThrow(), b.texture().orElseThrow());
                assetManager.load(b.texture().orElseThrow(), Texture.class);
                material = new Material();
            } else {
                logger.debug(b.name().orElseThrow() + " has no texture, using its color");
                material = new Material(
                        new ColorAttribute(ColorAttribute.Diffuse, b.color().orElseThrow()));
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

        SolarSystem.planets().stream().filter(Body::hasTexture).forEach(b -> {
            bodiesToModels
                    .get(b)
                    .materials
                    .first()
                    .set(new TextureAttribute(
                            TextureAttribute.Diffuse,
                            assetManager.get(b.texture().orElseThrow(), Texture.class)));
        });
        loading = false;
    }

    @Override
    public void show() {
        // intentionally empty
    }

    @Override
    public void render(final float delta) {
        // set shorthand variables
        mouseX = (float) Gdx.input.getX();
        mouseY = (float) (viewport.getScreenHeight() - Gdx.input.getY());
        isTouched = Gdx.input.isTouched();

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
            final ModelInstance instance = bodiesToModels.get(b);
            if (!isVisible(camera, instance)) {
                continue;
            }

            // draw red label
            spriteBatch.begin();
            font.setColor(1.0f, 0.0f, 0.0f, 1.0f);
            font.draw(spriteBatch, b.name().orElseThrow(), labelPosition.x + 10, labelPosition.y + 10);
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

            // if the mouse is over a planet we highlight it
            if (bodyPositionOnScreen.dst(mouseX, mouseY) < circleRadius) {
                Gdx.gl.glEnable(GL30.GL_BLEND);
                Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
                shapeRenderer.begin(ShapeType.Filled);
                shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 0.6f);
                shapeRenderer.circle(bodyPositionOnScreen.x, bodyPositionOnScreen.y, circleRadius);
                shapeRenderer.end();
                Gdx.gl.glDisable(GL30.GL_BLEND);

                // if we click on the planet we look at it
                if (isTouched) {
                    camera.lookAt(b.position().cpy().scl((float) scale));
                }
            }
        }

        final Body closestBody = SolarSystem.planets().stream()
                .min(Comparator.comparing(
                        b -> b.position().cpy().scl((float) scale).dst(camera.position)))
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
                String.format(
                        "Closest body: %s (%s km)", closestBody.name().orElseThrow(), FormatUtils.thousands(distance)),
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

        drawMiniMap();

        drawZoomSlider();

        handleInputs();

        bodyToLabelPosition.clear();
    }

    private void drawMiniMap() {
        final float minimapWidth = Math.min(viewport.getScreenWidth() * 0.2f, viewport.getScreenHeight() * 0.2f);
        final float minimapHeight = minimapWidth;
        final float minimapXPosition = viewport.getScreenWidth() - minimapWidth;
        final float minimapYPosition = viewport.getScreenHeight() - minimapHeight;
        final float minimapXCenter = (2 * minimapXPosition + minimapWidth) / 2.0f;
        final float minimapYCenter = (2 * minimapYPosition + minimapHeight) / 2.0f;

        final Function<Float, Float> fromWorldToMinimapX = f -> MathUtils.map(
                -solarsystemWidth, solarsystemWidth, minimapXPosition, minimapXPosition + minimapWidth, f);
        final Function<Float, Float> fromWorldToMinimapY = f -> MathUtils.map(
                -solarsystemWidth, solarsystemWidth, minimapYPosition, minimapYPosition + minimapHeight, f);

        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 0.3f);
        shapeRenderer.rect(minimapXPosition, minimapYPosition, minimapWidth, minimapHeight);
        // drawing the sun
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.circle(minimapXCenter, minimapYCenter, 6.0f);
        // drawing mercury
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(
                fromWorldToMinimapX.apply((float) (SolarSystem.MERCURY.position().x * scale)),
                fromWorldToMinimapY.apply((float) (SolarSystem.MERCURY.position().y * scale)),
                2.0f);
        // drawing venus
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(
                fromWorldToMinimapX.apply((float) (SolarSystem.VENUS.position().x * scale)),
                fromWorldToMinimapY.apply((float) (SolarSystem.VENUS.position().y * scale)),
                2.0f);
        // drawing the earth
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.circle(
                fromWorldToMinimapX.apply((float) (SolarSystem.EARTH.position().x * scale)),
                fromWorldToMinimapY.apply((float) (SolarSystem.EARTH.position().y * scale)),
                2.0f);
        // drawing mars
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(
                fromWorldToMinimapX.apply((float) (SolarSystem.MARS.position().x * scale)),
                fromWorldToMinimapY.apply((float) (SolarSystem.MARS.position().y * scale)),
                2.0f);
        // drawing jupiter
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(
                fromWorldToMinimapX.apply((float) (SolarSystem.JUPITER.position().x * scale)),
                fromWorldToMinimapY.apply((float) (SolarSystem.JUPITER.position().y * scale)),
                3.0f);
        // drawing saturn
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(
                fromWorldToMinimapX.apply((float) (SolarSystem.SATURN.position().x * scale)),
                fromWorldToMinimapY.apply((float) (SolarSystem.SATURN.position().y * scale)),
                3.0f);
        // drawing uranus
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(
                fromWorldToMinimapX.apply((float) (SolarSystem.URANUS.position().x * scale)),
                fromWorldToMinimapY.apply((float) (SolarSystem.URANUS.position().y * scale)),
                3.0f);
        // drawing pluto
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(
                fromWorldToMinimapX.apply((float) (SolarSystem.PLUTO.position().x * scale)),
                fromWorldToMinimapY.apply((float) (SolarSystem.PLUTO.position().y * scale)),
                3.0f);
        shapeRenderer.end();

        // drawing camera
        final float fieldOfViewX =
                (float) viewport.getScreenWidth() / (float) viewport.getScreenHeight() * camera.fieldOfView;
        final Vector3 leftSide = camera.direction
                .cpy()
                .rotate(camera.up, -fieldOfViewX / 2)
                .setLength(10_000.0f)
                .sub(camera.position);
        final Vector3 rightSide = camera.direction
                .cpy()
                .rotate(camera.up, fieldOfViewX / 2)
                .setLength(10_000.0f)
                .sub(camera.position);

        logger.debug("%f; %f - %f; %f", leftSide.x, leftSide.y, rightSide.x, rightSide.y);

        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.line(
                fromWorldToMinimapX.apply(camera.position.x),
                fromWorldToMinimapY.apply(camera.position.y),
                fromWorldToMinimapX.apply(leftSide.x),
                fromWorldToMinimapY.apply(leftSide.y));
        shapeRenderer.line(
                fromWorldToMinimapX.apply(camera.position.x),
                fromWorldToMinimapY.apply(camera.position.y),
                fromWorldToMinimapX.apply(rightSide.x),
                fromWorldToMinimapY.apply(rightSide.y));
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    private void setCameraFOV(final float newFOV) {
        camera.fieldOfView = MathUtils.clamp(newFOV, minFOV, maxFOV);
        camera.update();
    }

    private void drawZoomSlider() {
        final float zoomSliderXPosition = viewport.getScreenWidth() * 0.98f;
        final float zoomSliderYPosition = viewport.getScreenHeight() - (viewport.getScreenHeight() * 0.98f);
        final float zoomSliderWidth = 10.0f;
        final float zoomSliderHeight = viewport.getScreenHeight() * 0.2f;
        final float zoomSliderKnobRadius = zoomSliderWidth * 1.1f;
        final float zoomSliderKnobYPosition = MathUtils.map(
                minFOV, maxFOV, zoomSliderYPosition + zoomSliderHeight, zoomSliderYPosition, camera.fieldOfView);
        final float zoomSliderXCenter = (2 * zoomSliderXPosition + zoomSliderWidth) / 2;

        float zoomSliderTransparency;
        float zoomSliderKnobTransparency;

        if (mouseX >= zoomSliderXCenter - zoomSliderKnobRadius
                && mouseX <= zoomSliderXCenter + zoomSliderKnobRadius
                && mouseY >= zoomSliderYPosition - zoomSliderWidth / 2
                && mouseY <= zoomSliderYPosition + zoomSliderHeight + zoomSliderWidth / 2) {
            zoomSliderTransparency = 0.4f;
            zoomSliderKnobTransparency = 1.0f;

            if (isTouched) {
                // zoom slider is clicked
                final float newFOV = MathUtils.map(
                        zoomSliderYPosition + zoomSliderHeight, zoomSliderYPosition, minFOV, maxFOV, mouseY);
                setCameraFOV(newFOV);
            }
        } else {
            zoomSliderTransparency = 0.2f;
            zoomSliderKnobTransparency = 0.8f;
        }

        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(1.0f, 1.0f, 1.0f, zoomSliderTransparency);
        shapeRenderer.arc(
                zoomSliderXCenter, zoomSliderYPosition + zoomSliderHeight, zoomSliderWidth / 2, 0.0f, 180.0f, 10);
        shapeRenderer.rect(zoomSliderXPosition, zoomSliderYPosition, zoomSliderWidth, zoomSliderHeight);
        shapeRenderer.arc(zoomSliderXCenter, zoomSliderYPosition, zoomSliderWidth / 2, 180.0f, 180.0f, 10);
        shapeRenderer.setColor(1.0f, 1.0f, 1.0f, zoomSliderKnobTransparency);
        shapeRenderer.circle(zoomSliderXCenter, zoomSliderKnobYPosition, zoomSliderKnobRadius, 10);
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

        setCameraFOV(camera.fieldOfView + amountY);

        return false;
    }
}

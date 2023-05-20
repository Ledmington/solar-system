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

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.ledmington.solarsystem.utils.MiniLogger;

/**
 * Default implementation of a Screen.
 * Only show() and render() are not implemented.
 * The other methods have an empty implementation.
 * Utilities:
 *  - each AbstractScreen instance has an AssetManager called "assetManager"
 *    which gets disposed automatically
 *  - each AbstractScreen instance has a MiniLogger called "logger"
 *  - each AbstractScreen instance has a ModelBatch called "modelBatch"
 *  - each AbstractScreen instance has a SpriteBatch called "spriteBatch"
 *  - each AbstractScreen instance has a ShapeRenderer called "shapeRenderer"
 *  - each AbstractScreen instance has a BitmapFont called "font"
 */
public abstract class AbstractScreen implements Screen {

    protected final AssetManager assetManager = new AssetManager();
    protected final MiniLogger logger;
    protected final ModelBatch modelBatch = new ModelBatch();
    protected final SpriteBatch spriteBatch = new SpriteBatch();
    protected final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final List<Disposable> disposableAssets = new LinkedList<>();

    protected AbstractScreen(final String loggerName) {
        this.logger = MiniLogger.getLogger(loggerName);
    }

    protected final <T extends Disposable> T toBeDisposed(final T asset) {
        this.disposableAssets.add(asset);
        return asset;
    }

    /**
     * Intentionally empty implementation. Do not call.
     */
    @Override
    public void resize(final int width, final int height) {}

    /**
     * Intentionally empty implementation. Do not call.
     */
    @Override
    public void pause() {}

    /**
     * Intentionally empty implementation. Do not call.
     */
    @Override
    public void resume() {}

    /**
     * Intentionally empty implementation. Do not call.
     */
    @Override
    public void hide() {}

    @Override
    public final void dispose() {
        logger.debug("disposing asset manager");
        assetManager.clear();
        logger.debug("disposing model batch");
        modelBatch.dispose();
        logger.debug("disposing sprite batch");
        spriteBatch.dispose();
        logger.debug("disposing shape renderer");
        shapeRenderer.dispose();
        logger.debug("disposing additional assets");
        this.disposableAssets.forEach(Disposable::dispose);

        /*
         * Tells the JVM to perform a GC after each screen change.
         * Apparently, this works better than just disposing the resources
         * as I understood from here:
         * https://stackoverflow.com/questions/36751909/java-with-libgdx-font-memory-leak
         *
         * But, as far as I understood from https://stackoverflow.com/questions/66540/when-does-system-gc-do-something,
         * the actual behavior of this call is basically implementation dependent:
         *  - some JVMs actually perform a full GC cycle
         *  - some JVMs interpret the call as a suggestion
         *  - some JVMs completely ignore it because they use a GC which runs continuously in background
         *
         * However, it's possible to disable it manually with '-XX:+DisableExplicitGC'.
         */
        System.gc();
    }
}

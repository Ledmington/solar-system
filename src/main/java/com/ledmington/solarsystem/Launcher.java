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

import java.io.File;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.ledmington.solarsystem.utils.MiniLogger;

public final class Launcher extends Game {

    private static final MiniLogger logger = MiniLogger.getLogger("Launcher");

    private Launcher() {}

    public static void main(final String[] args) {
        if (args.length > 0) {
            logger.info("No command line arguments needed. Ignoring them.");
        }

        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setWindowedMode(Constants.TARGET_RESOLUTION_WIDTH, Constants.TARGET_RESOLUTION_HEIGHT);
        config.setTitle("Solar System");
        config.useVsync(true);
        config.setIdleFPS(30);
        config.setForegroundFPS(60);
        config.setResizable(false);
        config.setWindowSizeLimits(1280, 720, -1, -1);
        config.setWindowIcon(FileType.Internal, Constants.IMAGES_FOLDER + File.separator + "icon.png");

        try {
            new Lwjgl3Application(new Launcher(), config);
        } catch (Throwable t) {
            logger.error(t);
        }
    }

    @Override
    public void create() {
        this.setScreen(new MainScreen());
    }
}

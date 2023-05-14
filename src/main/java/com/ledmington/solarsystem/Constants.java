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

public final class Constants {
    private Constants() {}

    public static final double GRAVITATIONAL_CONSTANT = 6.6743e-11;

    /**
     * One AU in meters.
     */
    public static final double oneAstronomicalUnit = 150_000_000_000.0;

    public static final String RESOURCES_DIR = System.getProperties().containsKey("resources.dir")
            ? System.getProperty("resources.dir")
            : "./src/main/resources";
    public static final String IMAGES_FOLDER = "images";
    public static final String MODELS_FOLDER = "models";
    public static final String DATA_FOLDER = "data";

    /**
     * The interval between two frames when rendering at 60 FPS.
     */
    public static final int ONE_OVER_SIXTY = (int) (1.0 / 60.0);
}

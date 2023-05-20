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
package com.ledmington.solarsystem.model;

import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.ImmutableList;
import com.ledmington.solarsystem.Constants;

public final class SolarSystem {
    private SolarSystem() {}

    public static final Body SUN = Body.builder()
            .name("sun")
            .radius(696_340_000)
            .mass(1.98847e30)
            .position(Vector3.Zero)
            .speed(Vector3.Zero)
            .color(Color.GOLD)
            .build();
    public static final Body MERCURY = Body.builder()
            .name("mercury")
            .radius(2_439_700)
            .mass(3.3011e23)
            .position(new Vector3((float) (0.307 * Constants.oneAstronomicalUnit), 0, 0))
            .speed(new Vector3(0, 47_360, 0))
            .texture(Constants.MODELS_FOLDER + "/mercury.jpg")
            .color(Color.RED)
            .build();
    public static final Body VENUS = Body.builder()
            .name("venus")
            .radius(6_051_800)
            .mass(4.8675e24)
            .position(new Vector3((float) (0.718 * Constants.oneAstronomicalUnit), 0, 0))
            .speed(new Vector3(0, 35_020, 0))
            .texture(Constants.MODELS_FOLDER + "/venus.jpg")
            .color(Color.CYAN)
            .build();
    public static final Body EARTH = Body.builder()
            .name("earth")
            .radius(6_371_000)
            .mass(5.9722e24)
            .position(new Vector3((float) (1.0 * Constants.oneAstronomicalUnit), 0, 0))
            .speed(new Vector3(0, 29_782.7f, 0))
            .texture(Constants.MODELS_FOLDER + "/earth.jpg")
            .color(Color.BLUE)
            .build();
    public static final Body MOON = Body.builder()
            .name("moon")
            .radius(1_737.5)
            .mass(7.346e22)
            .position(new Vector3(EARTH.position().x + 384_400_000.0f, 0, 0))
            .speed(new Vector3(0, 29_782.7f, 0))
            .texture(Constants.MODELS_FOLDER + "/moon.jpg")
            .build();
    public static final Body MARS = Body.builder()
            .name("mars")
            .radius(3_389_500)
            .mass(6.4171e23)
            .position(new Vector3((float) (1.382 * Constants.oneAstronomicalUnit), 0, 0))
            .speed(new Vector3(0, 24_070, 0))
            .texture(Constants.MODELS_FOLDER + "/mars.jpg")
            .color(Color.RED)
            .build();
    public static final Body PHOBOS = Body.builder()
            .name("phobos")
            .radius(11_010)
            .mass(1.0659e16)
            .position(Vector3.Zero)
            .speed(Vector3.Zero)
            .build();
    public static final Body DEIMOS = Body.builder()
            .name("deimos")
            .radius(6_200)
            .mass(1.476e15)
            .position(Vector3.Zero)
            .speed(Vector3.Zero)
            .build();
    public static final Body JUPITER = Body.builder()
            .name("jupiter")
            .radius(69_911_000)
            .mass(1.8982e27)
            .position(new Vector3((float) (5.2 * Constants.oneAstronomicalUnit), 0, 0))
            .speed(new Vector3(0, 13_070, 0))
            .texture(Constants.MODELS_FOLDER + "/jupiter.jpg")
            .color(Color.RED)
            .build();
    public static final Body IO = Body.builder()
            .name("io")
            .radius(1_821_600)
            .mass(8.932e22)
            .position(Vector3.Zero)
            .speed(Vector3.Zero)
            .build();
    public static final Body EUROPA = Body.builder()
            .name("europa")
            .radius(1_560_800)
            .mass(4.8e22)
            .position(Vector3.Zero)
            .speed(Vector3.Zero)
            .build();
    public static final Body GANYMEDE = Body.builder()
            .name("ganymede")
            .radius(2_634_100)
            .mass(1.482e23)
            .position(Vector3.Zero)
            .speed(Vector3.Zero)
            .build();
    public static final Body CALLISTO = Body.builder()
            .name("callisto")
            .radius(2_410_300)
            .mass(1.076e23)
            .position(Vector3.Zero)
            .speed(Vector3.Zero)
            .build();
    public static final Body SATURN = Body.builder()
            .name("saturn")
            .radius(58_232_000)
            .mass(5.6834e26)
            .position(new Vector3((float) (9.075 * Constants.oneAstronomicalUnit), 0, 0))
            .speed(new Vector3(0, 9_680, 0))
            .texture(Constants.MODELS_FOLDER + "/saturn.jpg")
            .color(Color.CYAN)
            .build();
    public static final Body ENCELADUS = Body.builder()
            .name("enceladus")
            .radius(252_100)
            .mass(1.08e20)
            .position(Vector3.Zero)
            .speed(Vector3.Zero)
            .build();
    public static final Body TITAN = Body.builder()
            .name("titan")
            .radius(2_574_730)
            .mass(1.345e23)
            .position(Vector3.Zero)
            .speed(Vector3.Zero)
            .build();
    public static final Body URANUS = Body.builder()
            .name("uranus")
            .radius(25_362_000)
            .mass(8.681e25)
            .position(new Vector3((float) (19.0 * Constants.oneAstronomicalUnit), 0, 0))
            .speed(new Vector3(0, 6_800, 0))
            .texture(Constants.MODELS_FOLDER + "/uranus.jpg")
            .color(Color.WHITE)
            .build();
    public static final Body NEPTUNE = Body.builder()
            .name("neptune")
            .radius(24_622_000)
            .mass(1.02413e26)
            .position(new Vector3((float) (30.0 * Constants.oneAstronomicalUnit), 0, 0))
            .speed(new Vector3(0, 5_430, 0))
            .texture(Constants.MODELS_FOLDER + "/neptune.jpg")
            .color(Color.WHITE)
            .build();
    public static final Body PLUTO = Body.builder()
            .name("pluto")
            .radius(1_188_300)
            .mass(1.303e22)
            .position(new Vector3((float) (39.5 * Constants.oneAstronomicalUnit), 0, 0))
            .speed(new Vector3(0, 4_743, 0))
            .color(Color.WHITE)
            .build();

    public static final List<Body> planets() {
        return ImmutableList.<Body>builder()
                .addAll(
                        // making sure that we are not making duplicates
                        Set.of(
                                SUN,
                                MERCURY,
                                VENUS,
                                EARTH,
                                MOON,
                                MARS,
                                PHOBOS,
                                DEIMOS,
                                JUPITER,
                                IO,
                                EUROPA,
                                GANYMEDE,
                                CALLISTO,
                                SATURN,
                                ENCELADUS,
                                TITAN,
                                // TODO add Uranus moons
                                URANUS,
                                // TODO add Neptune moons
                                NEPTUNE,
                                // TODO add Pluto moons
                                PLUTO))
                .build();
    }
}

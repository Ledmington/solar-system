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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.ImmutableList;
import com.ledmington.solarsystem.Constants;

public final class SolarSystem {
    private SolarSystem() {}

    public static final List<Body> planets = ImmutableList.<Body>builder()
            .add(Body.builder()
                    .name("Sun")
                    .radius(696_340_000)
                    .mass(1.98847e30)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .color(Color.GOLD)
                    .build())
            .add(Body.builder()
                    .name("Mercury")
                    .radius(2_439_700)
                    .mass(3.3011e23)
                    .position(new Vector3((float) (0.307 * Constants.oneAstronomicalUnit), 0, 0))
                    .speed(new Vector3(0, 47_360, 0))
                    .color(Color.BROWN)
                    .build())
            .add(Body.builder()
                    .name("Venus")
                    .radius(6_051_800)
                    .mass(4.8675e24)
                    .position(new Vector3((float) (0.718 * Constants.oneAstronomicalUnit), 0, 0))
                    .speed(new Vector3(0, 35_020, 0))
                    .build())
            .add(Body.builder()
                    .name("Earth")
                    .radius(6_371_000)
                    .mass(5.9722e24)
                    .position(new Vector3((float) (1.0 * Constants.oneAstronomicalUnit), 0, 0))
                    .speed(new Vector3(0, 29_782.7f, 0))
                    .color(Color.BLUE)
                    .build())
            .add(Body.builder()
                    .name("Moon")
                    .radius(1_737.5)
                    .mass(7.346e22)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .color(Color.GRAY)
                    .build())
            .add(Body.builder()
                    .name("Mars")
                    .radius(3_389_500)
                    .mass(6.4171e23)
                    .position(new Vector3((float) (1.382 * Constants.oneAstronomicalUnit), 0, 0))
                    .speed(new Vector3(0, 24_070, 0))
                    .color(Color.ORANGE)
                    .build())
            .add(Body.builder()
                    .name("Phobos")
                    .radius(11_010)
                    .mass(1.0659e16)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .build())
            .add(Body.builder()
                    .name("Deimos")
                    .radius(6_200)
                    .mass(1.476e15)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .build())
            .add(Body.builder()
                    .name("Jupiter")
                    .radius(69_911_000)
                    .mass(1.8982e27)
                    .position(new Vector3((float) (5.2 * Constants.oneAstronomicalUnit), 0, 0))
                    .speed(new Vector3(0, 13_070, 0))
                    .color(Color.RED)
                    .build())
            .add(Body.builder()
                    .name("Io")
                    .radius(1_821_600)
                    .mass(8.932e22)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .build())
            .add(Body.builder()
                    .name("Europa")
                    .radius(1_560_800)
                    .mass(4.8e22)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .build())
            .add(Body.builder()
                    .name("Ganymede")
                    .radius(2_634_100)
                    .mass(1.482e23)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .build())
            .add(Body.builder()
                    .name("Callisto")
                    .radius(2_410_300)
                    .mass(1.076e23)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .build())
            .add(Body.builder()
                    .name("Saturn")
                    .radius(58_232_000)
                    .mass(5.6834e26)
                    .position(new Vector3((float) (9.075 * Constants.oneAstronomicalUnit), 0, 0))
                    .speed(new Vector3(0, 9_680, 0))
                    .build())
            .add(Body.builder()
                    .name("Enceladus")
                    .radius(252_100)
                    .mass(1.08e20)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .build())
            .add(Body.builder()
                    .name("Titan")
                    .radius(2_574_730)
                    .mass(1.345e23)
                    .position(Vector3.Zero)
                    .speed(Vector3.Zero)
                    .build())
            .add(Body.builder()
                    .name("Uranus")
                    .radius(25_362_000)
                    .mass(8.681e25)
                    .position(new Vector3((float) (19.0 * Constants.oneAstronomicalUnit), 0, 0))
                    .speed(new Vector3(0, 6_800, 0))
                    .build())
            // TODO add Uranus moons
            .add(Body.builder()
                    .name("Neptune")
                    .radius(24_622_000)
                    .mass(1.02413e26)
                    .position(new Vector3((float) (30.0 * Constants.oneAstronomicalUnit), 0, 0))
                    .speed(new Vector3(0, 5_430, 0))
                    .build())
            // TODO add Neptune moons
            .add(Body.builder()
                    .name("Pluto")
                    .radius(1_188_300)
                    .mass(1.303e22)
                    .position(Vector3.Zero)
                    .speed(new Vector3(0, 4_743, 0))
                    .build())
            // TODO add Pluto moons
            .build();
}

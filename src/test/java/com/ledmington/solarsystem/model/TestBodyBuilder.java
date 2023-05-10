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

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class TestBodyBuilder {

    private BodyBuilder bb;

    @BeforeEach
    public void setup() {
        bb = Body.builder();
    }

    @Test
    public void cannotSetNameTwice() {
        bb.name("example");
        assertThrows(IllegalStateException.class, () -> bb.name("hello"));
    }

    @Test
    public void cannotSetRadiusTwice() {
        bb.radius(12);
        assertThrows(IllegalStateException.class, () -> bb.radius(34));
    }

    @Test
    public void cannotSetMassTwice() {
        bb.mass(12);
        assertThrows(IllegalStateException.class, () -> bb.mass(34));
    }

    @Test
    public void cannotSetPositionTwice() {
        bb.position(new Vector3(1, 2, 3));
        assertThrows(IllegalStateException.class, () -> bb.position(new Vector3(9, 8, 7)));
    }

    @Test
    public void cannotSetSpeedTwice() {
        bb.speed(new Vector3(1, 2, 3));
        assertThrows(IllegalStateException.class, () -> bb.speed(new Vector3(9, 8, 7)));
    }

    @Test
    public void cannotSetColorTwice() {
        bb.color(Color.BLACK);
        assertThrows(IllegalStateException.class, () -> bb.color(Color.CYAN));
    }

    @Test
    public void cannotSetTextureTwice() {
        bb.texture("example");
        assertThrows(IllegalStateException.class, () -> bb.texture("hello"));
    }

    @Test
    public void cannotSetColorAndThenTexture() {
        bb.color(Color.BLACK);
        assertThrows(IllegalStateException.class, () -> bb.texture("hello"));
    }

    @Test
    public void cannotSetTextureAndThenColor() {
        bb.texture("example");
        assertThrows(IllegalStateException.class, () -> bb.color(Color.CYAN));
    }

    @Test
    public void cannotSetNegativeRadius() {
        assertThrows(IllegalArgumentException.class, () -> bb.radius(-1));
    }

    @Test
    public void cannotSetNegativeMass() {
        assertThrows(IllegalArgumentException.class, () -> bb.radius(-1));
    }

    @Test
    public void cannotBuildWithoutMass() {
        assertThrows(IllegalStateException.class, () -> bb.radius(1.0).build());
    }

    @Test
    public void cannotBuildWithoutRadius() {
        assertThrows(IllegalStateException.class, () -> bb.mass(1.0).build());
    }
}

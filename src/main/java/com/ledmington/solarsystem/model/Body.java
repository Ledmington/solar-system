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

import java.util.Optional;

import com.badlogic.gdx.math.Vector3;

public final class Body {

    public static BodyBuilder builder() {
        return new BodyBuilder();
    }

    private final Optional<String> name;
    private final double radiusInMeters;
    private final double massInKilograms;
    private Vector3 position;
    private Vector3 speed;

    public Body(
            final String name,
            final double radiusInMeters,
            final double massInKilograms,
            final Vector3 position,
            final Vector3 speed) {
        if (name == null || name.isEmpty() || name.isBlank()) {
            this.name = Optional.empty();
        } else {
            this.name = Optional.of(name);
        }
        this.radiusInMeters = radiusInMeters;
        this.massInKilograms = massInKilograms;
        this.position = position;
        this.speed = speed;
    }

    public String name() {
        return this.name.orElseThrow();
    }

    public double radius() {
        return this.radiusInMeters;
    }

    public Vector3 position() {
        return this.position;
    }
}

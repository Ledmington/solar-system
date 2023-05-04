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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public final class BodyBuilder {
    private String name;
    private double radiusInMeters = -1.0;
    private double massInKilograms = -1.0;
    private Vector3 position = Vector3.Zero;
    private Vector3 speed = Vector3.Zero;
    private Color color = Color.WHITE;

    public BodyBuilder() {}

    public BodyBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public BodyBuilder radius(final double radiusInMeters) {
        this.radiusInMeters = radiusInMeters;
        return this;
    }

    public BodyBuilder mass(final double massInKilograms) {
        this.massInKilograms = massInKilograms;
        return this;
    }

    public BodyBuilder position(final Vector3 position) {
        this.position = position;
        return this;
    }

    public BodyBuilder speed(final Vector3 speed) {
        this.speed = speed;
        return this;
    }

    public BodyBuilder color(final Color color) {
        this.color = color;
        return this;
    }

    public Body build() {
        if (radiusInMeters < 0) {
            throw new IllegalArgumentException("Cannot create a body without radius");
        }
        if (massInKilograms < 0) {
            throw new IllegalArgumentException("Cannot create a body without mass");
        }
        return new Body(this.name, this.radiusInMeters, this.massInKilograms, this.position, this.speed, this.color);
    }
}

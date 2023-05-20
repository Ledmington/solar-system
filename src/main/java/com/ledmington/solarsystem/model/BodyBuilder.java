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

import java.io.File;
import java.util.Optional;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.ledmington.solarsystem.Constants;

public final class BodyBuilder {
    private String name = null;
    private double radiusInMeters = -1.0;
    private double massInKilograms = -1.0;
    private Vector3 position = Vector3.Zero;
    private Vector3 speed = Vector3.Zero;
    private Optional<Color> color = Optional.of(Color.WHITE);
    private Optional<String> texture = Optional.empty();
    private boolean positionHasBeenSet = false;
    private boolean speedHasBeenSet = false;
    private boolean colorHasBeenSet = false;
    private boolean textureHasBeenSet = false;

    public BodyBuilder() {}

    public BodyBuilder name(final String name) {
        if (this.name != null) {
            throw new IllegalStateException("Cannot set Body's name twice");
        }
        this.name = name;
        return this;
    }

    public BodyBuilder radius(final double radiusInMeters) {
        if (radiusInMeters < 0) {
            throw new IllegalArgumentException("Cannot set a negative radius for a Body");
        }
        if (this.radiusInMeters >= 0) {
            throw new IllegalStateException("Cannot set Body's radius twice");
        }
        this.radiusInMeters = radiusInMeters;
        return this;
    }

    public BodyBuilder mass(final double massInKilograms) {
        if (massInKilograms < 0) {
            throw new IllegalArgumentException("Cannot set a negative mass for a Body");
        }
        if (this.massInKilograms >= 0) {
            throw new IllegalStateException("Cannot set Body's mass twice");
        }
        this.massInKilograms = massInKilograms;
        return this;
    }

    public BodyBuilder position(final Vector3 position) {
        if (this.positionHasBeenSet) {
            throw new IllegalStateException("Cannot set Body's position twice");
        }
        this.position = position;
        this.positionHasBeenSet = true;
        return this;
    }

    public BodyBuilder speed(final Vector3 speed) {
        if (this.speedHasBeenSet) {
            throw new IllegalStateException("Cannot set Body's speed twice");
        }
        this.speed = speed;
        this.speedHasBeenSet = true;
        return this;
    }

    public BodyBuilder color(final Color color) {
        if (this.colorHasBeenSet) {
            throw new IllegalStateException("Cannot set Body's color twice");
        }
        this.color = Optional.of(color);
        this.colorHasBeenSet = true;
        return this;
    }

    public BodyBuilder texture(final String texture) {
        if (this.textureHasBeenSet) {
            throw new IllegalStateException("Cannot set Body's texture twice");
        }
        this.texture = Optional.of(Constants.MODELS_FOLDER + File.separator + texture);
        this.textureHasBeenSet = true;
        return this;
    }

    public Body build() {
        if (this.massInKilograms < 0) {
            throw new IllegalStateException("Cannot build a Body without mass");
        }
        if (this.radiusInMeters < 0) {
            throw new IllegalStateException("Cannot build a Body without radius");
        }
        return new Body(
                Optional.of(this.name),
                this.radiusInMeters,
                this.massInKilograms,
                this.position,
                this.speed,
                this.color,
                this.texture);
    }
}

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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.ImmutableMap;
import com.ledmington.solarsystem.utils.language.Dictionary;

public final class Body {

    public static BodyBuilder builder() {
        return new BodyBuilder();
    }

    private final Optional<String> name;
    private final double radiusInMeters;
    private final double massInKilograms;
    private Vector3 position;
    private Vector3 speed;
    private final Color color;

    public Body(
            final String name,
            final double radiusInMeters,
            final double massInKilograms,
            final Vector3 position,
            final Vector3 speed,
            final Color color) {
        if (name == null || name.isEmpty() || name.isBlank()) {
            this.name = Optional.empty();
        } else {
            this.name = Optional.of(name);
        }
        this.radiusInMeters = radiusInMeters;
        this.massInKilograms = massInKilograms;
        this.position = position;
        this.speed = speed;
        this.color = color;
    }

    public String name() {
        return Dictionary.getInstance().get(this.name.orElseThrow());
    }

    public double radius() {
        return this.radiusInMeters;
    }

    public Vector3 position() {
        return this.position;
    }

    public Color color() {
        return this.color;
    }

    public String toString() {
        return "Body("
                + ImmutableMap.<String, String>builder()
                        .put("name", name.isPresent() ? name.get() : "N/A")
                        .put("radius", String.valueOf(radiusInMeters))
                        .put("mass", String.valueOf(massInKilograms))
                        .put("position", position.toString())
                        .put("speed", speed.toString())
                        .put("color", color.toString())
                        .build()
                        .entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(","))
                + ")";
    }

    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final Body b = (Body) other;
        return name.equals(b.name)
                && radiusInMeters == b.radiusInMeters
                && massInKilograms == b.massInKilograms
                && position.equals(b.position)
                && speed.equals(b.speed)
                && color.equals(b.color);
    }

    public int hashCode() {
        return Objects.hash(name, radiusInMeters, massInKilograms, position, speed, color);
    }
}

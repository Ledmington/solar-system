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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.ledmington.solarsystem.Constants;
import com.ledmington.solarsystem.utils.language.Dictionary;

public final record Body(
        Optional<String> name,
        double radius,
        double mass,
        Vector3 position,
        Vector3 speed,
        Optional<Color> color,
        Optional<String> texture) {

    public static BodyBuilder builder() {
        return new BodyBuilder();
    }

    public Optional<String> name() {
        return this.name.isPresent() ? Optional.of(Dictionary.getInstance().get(this.name.get())) : Optional.empty();
    }

    public boolean hasColor() {
        return this.color.isPresent();
    }

    public boolean hasTexture() {
        return this.texture.isPresent();
    }

    public Vector3 scaledPosition() {
        return position.cpy().scl((float) Constants.SCALE);
    }

    public float scaledRadius() {
        return (float) (radius * Constants.SCALE);
    }
}

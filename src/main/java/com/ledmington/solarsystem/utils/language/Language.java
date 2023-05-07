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
package com.ledmington.solarsystem.utils.language;

/**
 * Each value of this enumeration corresponds to an available
 * language for the current project.
 */
public enum Language {
    ITALIAN("italian"),
    ENGLISH("english");

    private final String name;

    Language(final String name) {
        this.name = name;
    }

    /**
     * @return
     *      The lowercase name of the Language.
     */
    public String getName() {
        return name;
    }
}

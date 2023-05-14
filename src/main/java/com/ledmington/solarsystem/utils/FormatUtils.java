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
package com.ledmington.solarsystem.utils;

import java.math.BigInteger;

public final class FormatUtils {

    private FormatUtils() {}

    public static String thousands(final int val) {
        return thousands(BigInteger.valueOf(val));
    }

    public static String thousands(final long val) {
        return thousands(BigInteger.valueOf(val));
    }

    public static String thousands(final BigInteger val) {
        final StringBuilder sb = new StringBuilder();

        final String s = val.toString();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                sb.insert(0, ",");
            }
            sb.insert(0, s.charAt(s.length() - i - 1));
        }

        return sb.toString();
    }
}

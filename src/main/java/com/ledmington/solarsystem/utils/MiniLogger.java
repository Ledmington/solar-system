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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Personal implementation of a simple Logger
 * that mimics the behavior of the {@link java.util.logging.Logger} class.
 */
public final class MiniLogger {

    private static final Map<String, MiniLogger> ALL_LOGGERS = new HashMap<>();
    private static final long BEGINNING = System.currentTimeMillis();
    private static LoggingLevel minimumLevel = LoggingLevel.DEBUG;

    /**
     * Specifies the level for all MiniLoggers.
     */
    public enum LoggingLevel {
        /**
         * The lowest level, useful for testing and debugging.
         */
        DEBUG,

        /**
         * Generally used for logging basic stuff.
         */
        INFO,

        /**
         * Useful for logging unexpected changes at runtime.
         */
        WARNING,

        /**
         * Generally used for critical errors and exceptions.
         */
        ERROR
    }

    /**
     * Returns a new MiniLogger with the given name.
     *
     * @param name
     *      The name of the MiniLogger.
     * @return
     *      A new MiniLogger instance.
     */
    public static MiniLogger getLogger(final String name) {
        Objects.requireNonNull(name);
        if (!ALL_LOGGERS.containsKey(name)) {
            ALL_LOGGERS.put(name, new MiniLogger(name));
        }
        return ALL_LOGGERS.get(name);
    }

    /**
     * Sets the minimum logging level for all MiniLogger instances.
     *
     * @param level
     *      The new logging level.
     */
    public static void setMinimumLevel(final LoggingLevel level) {
        Objects.requireNonNull(level);
        minimumLevel = level;
    }

    private final String name;

    private MiniLogger(final String name) {
        this.name = name;
    }

    private void log(final String formatString, final String tag, final Object... args) {
        Objects.requireNonNull(formatString);
        long t = System.currentTimeMillis() - BEGINNING;
        final long milliseconds = t % 1000;
        t /= 1000;
        final long seconds = t % 60;
        t /= 60;
        final long minutes = t % 60;
        t /= 60;
        final long hours = t % 24;

        final String header = String.format(
                "[%02d:%02d:%02d.%03d][%s][%s][%s]",
                hours, minutes, seconds, milliseconds, Thread.currentThread().getName(), name, tag);

        String line = String.format(
                "%s " + formatString,
                Stream.concat(Stream.of(header), Arrays.stream(args)).toArray());

        line = line.replace("\n", "\n" + header + " ");

        System.out.println(line);
    }

    /**
     *
     * @param formatString
     * @param args
     */
    public void debug(final String formatString, final Object... args) {
        if (minimumLevel != LoggingLevel.DEBUG) {
            return;
        }
        log(formatString, "DEBUG", args);
    }

    /**
     *
     * @param formatString
     * @param args
     */
    public void info(final String formatString, final Object... args) {
        if (minimumLevel == LoggingLevel.WARNING || minimumLevel == LoggingLevel.ERROR) {
            return;
        }
        log(formatString, "INFO", args);
    }

    /**
     *
     * @param formatString
     * @param args
     */
    public void warning(final String formatString, final Object... args) {
        if (minimumLevel == LoggingLevel.ERROR) {
            return;
        }
        log(formatString, "WARNING", args);
    }

    /**
     *
     * @param formatString
     * @param args
     */
    public void error(final String formatString, final Object... args) {
        log(formatString, "ERROR", args);
    }

    /**
     *
     * @param t
     */
    public void error(final Throwable t) {
        if (t.getMessage() != null) {
            error(t.getMessage());
        } else {
            error("null");
        }
        for (StackTraceElement ste : t.getStackTrace()) {
            error("  " + ste.toString());
        }
        if (t.getCause() != null) {
            error("Caused by:");
            error(t.getCause());
        }
    }
}

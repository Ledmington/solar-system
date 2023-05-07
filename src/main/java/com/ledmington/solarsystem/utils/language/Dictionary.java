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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.ledmington.solarsystem.Constants;
import com.ledmington.solarsystem.utils.MiniLogger;

/**
 * This class contains all the translations in all the available
 * (for the current project) languages for all the strings used
 * in the project.
 * <p>
 * This class is a singleton, in order to use it, call {@link getInstance} method.
 */
public final class Dictionary {

    private static Dictionary instance = null;

    /**
     * Singleton method to get the only available {@link Dictionary} instance.
     * @return
     *      The only instance of {@link Dictionary}.
     */
    public static Dictionary getInstance() {
        if (instance == null) {
            instance = new Dictionary();
        }
        return instance;
    }

    private final Map<String, Language> nameToLanguage = new HashMap<>();
    private final Map<Language, Map<String, String>> dict = new HashMap<>();
    private Language lang = Language.ENGLISH;
    private final MiniLogger logger = MiniLogger.getLogger(getClass().getSimpleName());

    private Dictionary() {
        for (Language l : Language.values()) {
            nameToLanguage.put(l.getName(), l);
        }

        long t = System.nanoTime();
        String line;
        try (BufferedReader br = new BufferedReader(
                new FileReader(Constants.RESOURCES_DIR + "/" + Constants.DATA_FOLDER + "/dictionary.csv"))) {
            final String[] languages = br.readLine().split(",");
            final List<BiConsumer<String, String>> consumers = new ArrayList<>();
            for (int i = 1; i < languages.length; i++) {
                final int finalI = i;
                dict.put(nameToLanguage.get(languages[i]), new HashMap<>());
                consumers.add((id, word) ->
                        dict.get(nameToLanguage.get(languages[finalI])).put(id, word));
            }
            while ((line = br.readLine()) != null) {
                final String[] record = line.split(",");
                final String id = record[0];
                final Iterator<BiConsumer<String, String>> it = consumers.iterator();
                for (int i = 1; i < languages.length; i++) {
                    String word = record[i];
                    if (word.startsWith("\"")) {
                        word = word.split("\"")[1];
                    }
                    it.next().accept(id, word);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        t = System.nanoTime() - t;
        logger.debug(String.format("Loaded dictionary in %d ms", t / 1_000_000));
    }

    /**
     * Getter for the current {@link Language}.
     * @return
     *      The currently set {@link Language}.
     */
    public Language getCurrentLanguage() {
        return lang;
    }

    /**
     * Setter for the current {@link Language}.
     * @param language
     *      The new {@link Language} to be set.
     */
    public void setLanguage(final Language language) {
        this.lang = language;
    }

    /**
     * Main method of the {@link Dictionary} class.
     * Looks for the {@link String} with the given id for the current language.
     *
     * @param id
     *      The id of the wanted String.
     * @return
     *      If the id is correct, the translated String.
     */
    public String get(final String id) {
        return dict.get(lang).get(id);
    }
}

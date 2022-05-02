/*
 * #%L
 * SkinsRestorer
 * %%
 * Copyright (C) 2021 SkinsRestorer
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package dev.brighten.anticheat.commands;

import cc.funkemunky.api.utils.co.aikar.commands.BukkitCommandManager;
import cc.funkemunky.api.utils.co.aikar.commands.Locales;
import cc.funkemunky.api.utils.co.aikar.locales.MessageKey;

import java.io.*;
import java.util.Properties;

public class CommandPropertiesManager {
    private static final String FILE = "command-messages.properties";
    private final File configPath;
    private final InputStream inputStream;

    public CommandPropertiesManager(BukkitCommandManager manager, File configPath, InputStream inputStream) {
        this.configPath = configPath;
        this.inputStream = inputStream;
        copyFile();

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(new File(configPath, FILE))) {
            props.load(in);
            props.forEach((k, v) -> manager.getLocales().addMessage(Locales.ENGLISH, MessageKey.of(k.toString()), v.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile() {
        File outFile = new File(configPath, FILE);

        try {
            if (!outFile.exists()) {
                try (OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }

                inputStream.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
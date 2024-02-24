/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.decompiler;

import cn.maxpixel.mcdecompiler.common.app.util.JarUtil;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.jar.Manifest;

public class Decompilers {
    private static final Manifest MANIFEST = JarUtil.getManifest(Decompilers.class);
    private static final Logger LOGGER = LogManager.getLogger("Decompiler Manager");
    private static final Object2ObjectOpenHashMap<String, IDecompiler> decompilers = new Object2ObjectOpenHashMap<>();
    static {
        for (IDecompiler d : ServiceLoader.load(IDecompiler.class)) {
            decompilers.put(d.name(), d);
        }
        decompilers.put(UserDefinedDecompiler.NAME, findUserDefined());
    }

    private static UserDefinedDecompiler findUserDefined() {
        Path path = Path.of("decompiler", "decompiler.properties");
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Properties decompilerProperties = new Properties();
                decompilerProperties.load(reader);
                String decompilerPath = Objects.requireNonNull(decompilerProperties.getProperty("decompiler-file"),
                        "decompiler-file is a required property");
                String sourceType = Objects.requireNonNull(decompilerProperties.getProperty("source-type"),
                        "source-type is a required property").toUpperCase(Locale.ROOT);
                String[] args = Objects.requireNonNull(decompilerProperties.getProperty("args"), "args is a required property")
                        .split(" ");
                return new UserDefinedDecompiler(IDecompiler.SourceType.valueOf(sourceType), Path.of("decompiler", decompilerPath).
                        toAbsolutePath().normalize(), ObjectImmutableList.of(args));
            } catch (IOException e) {
                LOGGER.warn("Error occurred when constructing the user-defined decompiler", e);
            }
        }
        return UserDefinedDecompiler.NONE;
    }

    public static IDecompiler get(String name) {
        return decompilers.get(name);
    }

    public static String getProperty(String name, String prop) {
        return JarUtil.getProperty(MANIFEST, name, prop);
    }
}
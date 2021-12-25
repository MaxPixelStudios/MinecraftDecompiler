/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.decompiler;

import cn.maxpixel.mcdecompiler.Info;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

public class Decompilers {
    private static final EnumMap<Info.DecompilerType, IDecompiler> decompilers = new EnumMap<>(Info.DecompilerType.class);
    private static final Object2ObjectOpenHashMap<String, ICustomDecompiler> customDecompilers = new Object2ObjectOpenHashMap<>();
    static {
        init();
        initCustom();
    }

    private static void init() {
        decompilers.put(Info.DecompilerType.FERNFLOWER, new FernFlowerDecompiler());
        decompilers.put(Info.DecompilerType.CFR, new CFRDecompiler());
        decompilers.put(Info.DecompilerType.FORGEFLOWER, new ForgeFlowerDecompiler());
        decompilers.put(Info.DecompilerType.USER_DEFINED, findUserDefined());
    }

    private static UserDefinedDecompiler findUserDefined() {
        Path path = Path.of("decompiler", "decompiler.properties");
        if(Files.exists(path)) {
            try(BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Properties decompilerProperties = new Properties();
                decompilerProperties.load(reader);
                String decompilerPath = Objects.requireNonNull(decompilerProperties.getProperty("decompiler-file"),
                        "decompiler-file is a required property");
                String sourceType = Objects.requireNonNull(decompilerProperties.getProperty("source-type"),
                        "source-type is a required property").toUpperCase(Locale.ROOT);
                String libRecommended = decompilerProperties.getProperty("lib-recommended", "false");
                String[] args = Objects.requireNonNull(decompilerProperties.getProperty("args"), "args is a required property")
                        .split(" ");
                return new UserDefinedDecompiler(IDecompiler.SourceType.valueOf(sourceType), Path.of("decompiler", decompilerPath).
                        toAbsolutePath().normalize(), ObjectArrayList.wrap(args), Boolean.parseBoolean(libRecommended));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return UserDefinedDecompiler.NONE;
    }

    private static void initCustom() {
        StreamSupport.stream(ServiceLoader.load(ICustomDecompiler.class).spliterator(), true)
                .forEach(icd -> {
                    synchronized(customDecompilers) {
                        customDecompilers.put(icd.name(), icd);
                    }
                });
    }

    public static IDecompiler get(Info.DecompilerType type) {
        return decompilers.getOrDefault(type, decompilers.get(Info.DecompilerType.FORGEFLOWER));
    }

    public static ICustomDecompiler getCustom(String name) {
        return customDecompilers.get(name);
    }
}
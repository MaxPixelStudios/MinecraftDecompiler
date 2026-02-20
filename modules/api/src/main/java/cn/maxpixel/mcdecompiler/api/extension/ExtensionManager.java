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

package cn.maxpixel.mcdecompiler.api.extension;

import cn.maxpixel.mcdecompiler.api.util.DataMap;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ServiceLoader;

public final class ExtensionManager {
    public static final OptionRegistry OPTION_REGISTRY = new OptionRegistry();

    private static final ServiceLoader<Extension> EXTENSIONS = ServiceLoader.load(Extension.class);

    static {
        OPTION_REGISTRY.registerOptions(EXTENSIONS);
    }

    public static void init() {}

    /**
     * Call the extensions to let them receive their options.
     * This would be automatically called on the first use of {@link cn.maxpixel.mcdecompiler.api.MinecraftDecompiler},
     * but it can be called multiple times manually as needed.
     */
    public static void receiveOptions() {
        OPTION_REGISTRY.receiveOptions(EXTENSIONS);
    }

    public static void setup() {
        SetupHelper.setup();
    }

    public static void onPreprocess(FileSystem fs, Path tempDir, DataMap dataMap) throws IOException {
        for (Extension extension : EXTENSIONS) {
            extension.onPreprocess(fs, tempDir, dataMap);
        }
    }
}

class SetupHelper {
    static {
        ExtensionManager.receiveOptions();
    }

    public static void setup() {}
}
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

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.Properties;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static cn.maxpixel.mcdecompiler.decompiler.ForgeFlowerDecompiler.FERNFLOWER_ABSTRACT_PARAMETER_NAMES;

public class UserDefinedDecompiler implements ILibRecommendedDecompiler {
    public static final String NAME = "user-defined";
    public static final UserDefinedDecompiler NONE = new UserDefinedDecompiler() {
        @Override
        public void decompile(@NotNull Path source, @NotNull Path target) {
            throw new RuntimeException("User decompiler not found. Please make sure you have put correct config file in \"decompiler\" directory");
        }
    };
    private SourceType sourceType;
    private Path decompilerPath;
    private List<String> options;
    private ObjectList<String> libs = ObjectLists.emptyList();

    private UserDefinedDecompiler() {}

    UserDefinedDecompiler(@NotNull SourceType sourceType, @NotNull Path decompilerPath, @NotNull List<String> options) {
        this.sourceType = sourceType;
        this.decompilerPath = FileUtil.requireExist(decompilerPath);
        this.options = options;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public @NotNull SourceType getSourceType() {
        return sourceType;
    }

    @Override
    public void decompile(@NotNull Path source, @NotNull Path target) throws IOException {
        checkArgs(source, target);
        ObjectArrayList<String> arrayList = new ObjectArrayList<>();
        arrayList.add("java");
        arrayList.add("-jar");
        arrayList.add(decompilerPath.toString());
        arrayList.addAll(resolveArgs(source, target, options));
        Utils.waitForProcess(Runtime.getRuntime().exec(arrayList.toArray(new String[0])));
    }

    private ObjectArrayList<String> resolveArgs(@NotNull Path source, @NotNull Path target, @NotNull List<String> options) {
        ObjectArrayList<String> resolvedOptions = new ObjectArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            String s = options.get(i);
            if (s.contains("%source%")) s = s.replace("%source%", source.toString());
            if (s.contains("%target%")) s = s.replace("%target%", target.toString());
            if (s.contains("%lib_all%")) s = s.replace("%lib_all%", String.join(Info.PATH_SEPARATOR, libs));
            if (s.contains("%abstract_params%")) {
                Path p = Properties.TEMP_DIR.resolve(FERNFLOWER_ABSTRACT_PARAMETER_NAMES).toAbsolutePath().normalize();
                s = s.replace("%abstract_params%", Files.exists(p) ? p.toString() : "");
            }
            if (s.contains("%lib_repeat%")) {
                for (int j = 0; j < libs.size(); j++) {
                    if (s.equals("%lib_repeat%")) {
                        resolvedOptions.add(options.get(i - 1));
                        resolvedOptions.add(libs.get(j));
                    } else resolvedOptions.add(s.replace("%lib_repeat%", libs.get(j)));
                }
            } else resolvedOptions.add(s);
        }
        return resolvedOptions;
    }

    @Override
    public void receiveLibs(@NotNull ObjectSet<Path> libs) {
        this.libs = libs.stream().map(p -> p.toAbsolutePath().normalize().toString())
                .collect(ObjectArrayList.toListWithExpectedSize(libs.size()));
    }
}
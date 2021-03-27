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
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class UserDefinedDecompiler extends AbstractLibRecommendedDecompiler {
    private UserDefinedDecompiler() {}
    public static final UserDefinedDecompiler NONE = new UserDefinedDecompiler() {
        @Override
        public void decompile(Path source, Path target) {
            throw new RuntimeException("User decompiler not found. Make sure you put correct config file in \"decompiler\" directory");
        }
        @Override
        public void downloadLib(Path libDir, String version) {}
    };
    private SourceType sourceType;
    private Path decompilerPath;
    private ObjectArrayList<String> options;
    private boolean libRecommended;
    UserDefinedDecompiler(SourceType sourceType, Path decompilerPath, ObjectArrayList<String> options, boolean libRecommended) {
        this.sourceType = Objects.requireNonNull(sourceType);
        this.decompilerPath = Objects.requireNonNull(decompilerPath);
        FileUtil.requireExist(decompilerPath);
        this.options = Objects.requireNonNull(options);
        this.libRecommended = libRecommended;
    }
    @Override
    public SourceType getSourceType() {
        return sourceType;
    }
    @Override
    public void decompile(Path source, Path target) throws IOException {
        checkArgs(source, target);
        ObjectArrayList<String> arrayList = new ObjectArrayList<>();
        arrayList.add("java");
        arrayList.add("-jar");
        arrayList.add(decompilerPath.toString());
        arrayList.addAll(resolveArgs(source, target, options));
        Utils.waitForProcess(Runtime.getRuntime().exec(arrayList.toArray(new String[0])));
    }
    private ObjectArrayList<String> resolveArgs(Path source, Path target, ObjectArrayList<String> options) {
        ObjectArrayList<String> resolvedOptions = new ObjectArrayList<>();
        List<String> libs = listLibs();
        for(int i = 0; i < options.size(); i++) {
            String s = options.get(i);
            if(!libRecommended && (s.contains("%lib_all%") || s.contains("%lib_repeat%")))
                throw new RuntimeException("lib-recommended option is set to false, if you want to use %lib_all% or %lib_repeat% variable, " +
                        "please change lib-recommended option to true(in decompiler.properties)");
            if(s.contains("%source%")) s = s.replace("%source%", source.toString());
            if(s.contains("%target%")) s = s.replace("%target%", target.toString());
            if(libRecommended && s.contains("%lib_all%")) s = s.replace("%lib_all%", String.join(Info.PATH_SEPARATOR, libs));
            if(libRecommended && s.contains("%lib_repeat%")) {
                for(int j = 0; j < libs.size(); j++) {
                    if(s.equals("%lib_repeat%")) {
                        resolvedOptions.add(options.get(i - 1));
                        resolvedOptions.add(libs.get(j));
                    } else resolvedOptions.add(s.replace("%lib_repeat%", libs.get(j)));
                }
            } else resolvedOptions.add(s);
        }
        return resolvedOptions;
    }
    @Override
    public void downloadLib(Path libDir, String version) throws IOException {
        if(libRecommended) super.downloadLib(libDir, version);
    }
}
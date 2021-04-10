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
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.getopt.OptionsImpl;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CFRDecompiler extends AbstractLibRecommendedDecompiler {
    CFRDecompiler() {}

    @Override
    public SourceType getSourceType() {
        return SourceType.FILE;
    }

    @Override
    public void decompile(Path source, Path target) {
        checkArgs(source, target);
        Map<String, String> options = new HashMap<>();
        options.put(OptionsImpl.FORCE_AGGRESSIVE_EXCEPTION_AGG.getName(), "true");
        options.put(OptionsImpl.FORCE_CLASSFILEVER.getName(), "52.0");
        options.put(OptionsImpl.CLOBBER_FILES.getName(), "true");
        options.put(OptionsImpl.ECLIPSE.getName(), "false");
        options.put(OptionsImpl.EXTRA_CLASS_PATH.getName(), String.join(Info.PATH_SEPARATOR, listLibs()));
        options.put(OptionsImpl.OUTPUT_PATH.getName(), target.toString());
        options.put(OptionsImpl.REMOVE_BAD_GENERICS.getName(), "false");
        options.put(OptionsImpl.REMOVE_DEAD_CONDITIONALS.getName(), "false");
        options.put(OptionsImpl.JAR_FILTER.getName(), "^(net\\.minecraft|com\\.mojang\\.(blaze3d|math|realmsclient))\\.*");
        CfrDriver cfr = new CfrDriver.Builder().withOptions(options).build();
        cfr.analyse(ObjectLists.singleton(source.toString()));
    }
}
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
        options.put("aexagg", "true");
        options.put("forceclassfilever", "52.0");
        options.put("caseinsensitivefs", "false");
        options.put("clobber", "true");
        options.put("eclipse", "false");
        options.put("extraclasspath", String.join(Info.PATH_SEPARATOR, listLibs()));
        options.put("outputpath", target.toString());
        options.put("removebadgenerics", "false");
        options.put("removedeadconditionals", "false");
        options.put("jarfilter", "^(net\\.minecraft|com\\.mojang\\.(blaze3d|math|realmsclient))\\.*");
        CfrDriver cfr = new CfrDriver.Builder().withOptions(options).build();
        cfr.analyse(ObjectLists.singleton(source.toString()));
    }
}
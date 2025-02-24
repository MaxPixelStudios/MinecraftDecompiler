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

package cn.maxpixel.mcdecompiler.mapping.parchment;

import cn.maxpixel.mcdecompiler.api.extension.Extension;
import cn.maxpixel.mcdecompiler.api.extension.OptionRegistry;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import org.jetbrains.annotations.NotNull;

public class ParchmentExtension implements Extension {
    @NotNull
    @Override
    public String getName() {
        return ParchmentMappingFormat.NAME;
    }

    @Override
    public void onRegistering(OptionRegistry.Registrar optionRegistrar) {
        MappingFormats.registerMappingFormat(ParchmentMappingFormat.INSTANCE);
    }
}
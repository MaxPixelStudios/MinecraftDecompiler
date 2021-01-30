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

package cn.maxpixel.mcdecompiler.deobfuscator;

import cn.maxpixel.mcdecompiler.reader.SrgMappingReader;

import java.nio.file.Path;

public class SrgDeobfuscator extends AbstractDeobfuscator {
    public SrgDeobfuscator(String mappingPath) {
        super(mappingPath);
    }
    @Override
    public SrgDeobfuscator deobfuscate(Path source, Path target) {
        try(SrgMappingReader mappingReader = new SrgMappingReader(mappingPath)) {
            sharedDeobfuscate(source, target, mappingReader);
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        return this;
    }
}
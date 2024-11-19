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

package cn.maxpixel.mcdecompiler.remapper.variable;

import org.objectweb.asm.Type;

public class LambdaRenamer extends Renamer {
    private final Renamer owner;
    private final int skips;
    private boolean prepared;

    public LambdaRenamer(Renamer owner, int skips) {
        this.owner = owner;
        this.skips = skips;
    }

    @Override
    public void prepare() {
        if (prepared) throw new IllegalStateException("Already prepared");
        vars.putAll(owner.vars);
        prepared = true;
    }

    @Override
    public String addExistingName(String name, int index) {
        if (!prepared) throw new IllegalStateException("Not prepared");
        if (index < skips) return name;
        return super.addExistingName(name, index);
    }

    @Override
    public String getVarName(Type type, int index) {
        if (!prepared) throw new IllegalStateException("Not prepared");
        if (index < skips) return null;
        return super.getVarName(type, index);
    }
}
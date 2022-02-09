/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

package cn.maxpixel.mcdecompiler.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.function.Function;

public class ClassProcessor {//TODO
    private final ClassVisitor visitor;

    public ClassProcessor(Function<ClassVisitor, ClassVisitor> visitor, ClassWriter writer) {
        this.visitor = visitor.apply(writer);
    }

    public ClassVisitor getVisitor() {
        return visitor;
    }

    public interface Process {
        enum State {
            BEFORE,
            AFTER
        }

        State getState();

        Function<ClassVisitor, ClassVisitor> getVisitor();
    }
}
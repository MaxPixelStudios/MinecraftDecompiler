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

package cn.maxpixel.mcdecompiler.asm;

import cn.maxpixel.mcdecompiler.mapping.TinyClassMapping;
import cn.maxpixel.mcdecompiler.mapping.tiny.TinyMethodMapping;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Optional;

public class TinyV2LVTRenamer extends ClassVisitor {
    private final Object2ObjectOpenHashMap<String, TinyClassMapping> mappings;
    private TinyClassMapping mapping;
    private boolean disabled;
    public TinyV2LVTRenamer(ClassVisitor classVisitor, Object2ObjectOpenHashMap<String, TinyClassMapping> mappings) {
        super(Opcodes.ASM9, classVisitor);
        this.mappings = mappings;
    }
    public TinyV2LVTRenamer(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
        this.mappings = null;
        this.disabled = true;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if(!disabled) mapping = mappings.get(NamingUtil.asJavaName(name));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if(!disabled) {
            Optional<TinyMethodMapping> methodMapping = mapping.getMethods().stream().filter(m -> m.getUnmappedName().equals(name) && m.getUnmappedDescriptor().equals(descriptor)).findAny();
            return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                @Override
                public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                    super.visitLocalVariable(methodMapping.map(mm -> mm.getLocalVariableName(index)).orElse(name), descriptor, signature, start, end, index);
                }
            };
        } else return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
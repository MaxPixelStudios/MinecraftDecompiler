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

import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMethodMapping;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Optional;

public class LVTRenamer extends ClassVisitor {
    private final Object2ObjectOpenHashMap<String, ? extends NamespacedClassMapping> mappings;
    private NamespacedClassMapping mapping;
    private final String fromNamespace;
    private final String toNamespace;

    public LVTRenamer(ClassVisitor classVisitor, Object2ObjectOpenHashMap<String, ? extends NamespacedClassMapping> mappings, String fromNamespace, String toNamespace) {
        super(Opcodes.ASM9, classVisitor);
        this.mappings = mappings;
        this.fromNamespace = fromNamespace;
        this.toNamespace = toNamespace;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mapping = mappings.get(NamingUtil.asJavaName(name));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        Optional<NamespacedMethodMapping> methodMapping = mapping.getMethods().stream().filter(m -> m.getName(fromNamespace).equals(name) && m.getUnmappedDescriptor().equals(descriptor)).findAny();
        return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            @Override
            public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                super.visitLocalVariable(methodMapping.map(mm -> mm.getLocalVariableName(index, toNamespace)).filter(lvn -> !lvn.isEmpty() &&
                                !lvn.equals("o"/* tsrg2 empty lvn placeholder */)).orElse(name), descriptor, signature, start, end, index);
            }
        };
    }
}
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

package cn.maxpixel.mcdecompiler.asm;

import cn.maxpixel.mcdecompiler.Info;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class IndyRemapper extends ClassVisitor {
    private final ClassifiedMappingRemapper mappingRemapper;

    public IndyRemapper(ClassVisitor classVisitor, ClassifiedMappingRemapper mappingRemapper) {
        super(Info.ASM_VERSION, classVisitor);
        this.mappingRemapper = mappingRemapper;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new Remapper(super.visitMethod(access, name, descriptor, signature, exceptions));
    }

    public class Remapper extends MethodVisitor {
        public Remapper(MethodVisitor methodVisitor) {
            super(Info.ASM_VERSION, methodVisitor);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            // We only remap name here. Other things are remapped by ASM's ClassRemapper
            String newName = name;
            if (bootstrapMethodHandle.getOwner().equals("java/lang/invoke/LambdaMetafactory")) {
                Type interfaceMethodType = (Type) bootstrapMethodArguments[0];
                newName = mappingRemapper.mapMethodName(Type.getReturnType(descriptor).getInternalName(), name, interfaceMethodType.getDescriptor());
            }
            super.visitInvokeDynamicInsn(newName, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }
    }
}
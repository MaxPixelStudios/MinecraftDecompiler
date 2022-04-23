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

import cn.maxpixel.mcdecompiler.Info;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.objectweb.asm.*;

import java.util.Optional;

public class RecordNameRemapper extends ClassVisitor {
    private final ObjectArrayList<String> recordNames = new ObjectArrayList<>();
    private final StringBuilder recordDesc = new StringBuilder("(");
    private boolean done;

    public RecordNameRemapper(ClassVisitor classVisitor) { // Use this when (access & Opcodes.ACC_RECORD) != 0
        super(Info.ASM_VERSION, classVisitor);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        recordNames.add(name);
        recordDesc.append(descriptor);
        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if(!done) {
            recordDesc.append(")V");
            done = true;
        }
        if(name.equals("<init>") && descriptor.contentEquals(recordDesc))
            return new RecordNameVisitor(mv, (access & Opcodes.ACC_STATIC) != 0);
        return mv;
    }

    public class RecordNameVisitor extends MethodVisitor implements VariableNameGenerator.Skippable {
        private final Optional<VariableNameGenerator.Skippable> skippable;
        private final boolean isStatic;

        private boolean remap;
        private int i;

        public RecordNameVisitor(MethodVisitor methodVisitor, boolean isStatic) {
            super(RecordNameRemapper.this.api, methodVisitor);
            this.skippable = methodVisitor instanceof VariableNameGenerator.Skippable s ? Optional.of(s) : Optional.empty();
            this.isStatic = isStatic;
        }
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if(!remap && opcode == Opcodes.INVOKESPECIAL && descriptor.equals("()V") && name.equals("<init>")
                    && owner.equals("java/lang/Record"))
                remap = true;
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            if(remap && i < recordNames.size() && (index != 0 || isStatic)) {
                skip();
                super.visitLocalVariable(recordNames.get(i++), descriptor, signature, start, end, index);
            } else super.visitLocalVariable(name, descriptor, signature, start, end, index);
        }

        @Override
        public void skip() {
            skippable.ifPresent(VariableNameGenerator.Skippable::skip);
        }
    }
}
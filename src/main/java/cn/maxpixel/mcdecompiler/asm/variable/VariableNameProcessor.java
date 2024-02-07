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

package cn.maxpixel.mcdecompiler.asm.variable;

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.util.Logging;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VariableNameProcessor extends ClassVisitor {
    private static final Logger LOGGER = Logging.getLogger();
    private final Object2ObjectOpenHashMap<String, Renamer> sharedRenamers = new Object2ObjectOpenHashMap<>();
    private final @NotNull ForgeFlowerAbstractParametersRecorder recorder;
    private final @NotNull VariableNameHandler handler;
    private final @NotNull String className;
    private final boolean regenerate;

    public VariableNameProcessor(@Nullable ClassVisitor classVisitor, @NotNull ForgeFlowerAbstractParametersRecorder recorder, @NotNull VariableNameHandler handler, @NotNull String className, boolean regenerate) {
        super(Info.ASM_VERSION, classVisitor);
        this.recorder = recorder;
        this.handler = handler;
        this.className = className;
        this.regenerate = regenerate;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if ((access & Opcodes.ACC_ABSTRACT) != 0 && recorder.isRecording() && descriptor.charAt(1) != ')') { // equivalent to !descriptor.startsWith("()")
            recorder.record(className, name, descriptor, handler.handleAbstractMethod(access, name, descriptor, signature, exceptions), handler.omitThis());
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
        VariableNameProvider.RenameFunction func = handler.handleMethod(access, name, descriptor, signature, exceptions);
        String method = name.concat(descriptor);
        // Filter some methods because only lambda methods need to share renamer with the caller
        Renamer renamer = regenerate ? ((access & Opcodes.ACC_PRIVATE) != 0 && (access & Opcodes.ACC_SYNTHETIC) != 0 ?
                sharedRenamers.getOrDefault(method, new Renamer()) : new Renamer()) : null;
        return new MethodProcessor(super.visitMethod(access, name, descriptor, signature, exceptions), func, renamer,
                method, (access & Opcodes.ACC_STATIC) == 0);
    }

    public class MethodProcessor extends MethodVisitor {
        private final VariableNameProvider.RenameFunction renameFunction;
        private final Renamer renamer;
        private final String method;
        private final boolean notStatic;
        private final boolean omitThis;
        private boolean skip;

        public MethodProcessor(MethodVisitor methodVisitor, VariableNameProvider.RenameFunction renameFunction, Renamer renamer, String method, boolean notStatic) {
            super(Info.ASM_VERSION, methodVisitor);
            this.renameFunction = renameFunction;
            this.renamer = renamer;
            this.method = method;
            this.notStatic = notStatic;
            this.omitThis = handler.omitThis();
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            if (skip) return;
            switch (bootstrapMethodHandle.getOwner()) {
                case "java/lang/runtime/ObjectMethods" -> this.skip = true;
                case "java/lang/invoke/LambdaMetafactory" -> {
                    if (!regenerate) return;
                    Handle lambdaImpl = (Handle) bootstrapMethodArguments[1];
                    if (lambdaImpl.getOwner().equals(className) && sharedRenamers.putIfAbsent(
                            lambdaImpl.getName().concat(lambdaImpl.getDesc()), renamer) == null) {
                        LOGGER.log(Level.FINEST, "Method {0} is going to share renamer with {1}{2}",
                                new Object[] {method, lambdaImpl.getName(), lambdaImpl.getDesc()});
                    }
                }
            }
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            if (skip || (index == 0 && notStatic)) super.visitLocalVariable(name, descriptor, signature, start, end, index);
            else {
                String newName = renameFunction != null ? renameFunction.getName(name, descriptor, signature, start, end,
                        (notStatic && omitThis) ? index - 1 : index) : null;
                super.visitLocalVariable(regenerate ? (newName != null ? renamer.addExistingName(newName) :
                        renamer.getVarName(Type.getType(descriptor))) : (newName != null ? newName : name), descriptor, signature, start, end, index);
            }
        }
    }
}
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
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.Logging;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VariableNameGenerator extends ClassVisitor {
    private static final Logger LOGGER = Logging.getLogger("Variable Name Generator");
    private static final ObjectList<String> generatedAbstractParameterNames = ObjectLists.synchronize(new ObjectArrayList<>());
    private static volatile boolean recordStarted;

    public static void startRecord() {
        if(recordStarted) throw new IllegalStateException("Record already started");
        generatedAbstractParameterNames.clear();
        LOGGER.finest("Cleared previously generated abstract parameter names(if any)");
        recordStarted = true;
        LOGGER.finest("Started to record the generated abstract method parameter names");
    }

    public static void endRecord(@NotNull Path writeTo) throws IOException {
        if(!recordStarted) throw new IllegalStateException("Record not started yet");
        FileUtil.deleteIfExists(writeTo);
        Files.writeString(FileUtil.ensureFileExist(writeTo), String.join("\n", generatedAbstractParameterNames),
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        LOGGER.log(Level.FINE, "Saved record to {0}", writeTo);
        recordStarted = false;
        LOGGER.finer("Ended record");
    }

    private final Object2ObjectOpenHashMap<String, Renamer> sharedRenamers = new Object2ObjectOpenHashMap<>();
    private String className;

    public VariableNameGenerator(ClassVisitor classVisitor) {
        super(Info.ASM_VERSION, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // Filter some methods because only lambda methods need to share renamer with the caller
        Renamer renamer = (access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE)) != 0 ?
                sharedRenamers.getOrDefault(name.concat(descriptor), new Renamer()) : new Renamer();
        if((access & Opcodes.ACC_ABSTRACT) != 0) {
            if(recordStarted && descriptor.charAt(1) != ')') { // equivalent to !descriptor.startsWith("()")
                StringJoiner joiner = new StringJoiner(" ").add(className).add(name).add(descriptor);
                LOGGER.log(Level.FINEST, "Generation of abstract parameter names started for method {0}{1} in class {2}",
                        new Object[] {name, descriptor, className});
                for(Type type : Type.getArgumentTypes(descriptor)) {
                    joiner.add(renamer.getVarName(type));
                    LOGGER.log(Level.FINEST, "Generated an abstract parameter name from descriptor \"{0}\" for method {1}{2} in class {3}",
                            new Supplier[] {type::getDescriptor, () -> name, () -> descriptor, () -> className});
                }
                generatedAbstractParameterNames.add(joiner.toString());
                LOGGER.log(Level.FINEST, "Generation of abstract parameter names completed for method {0}{1} in class {2}",
                        new Object[] {name, descriptor, className});
            }
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
        return new VariableMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions),
                (access & Opcodes.ACC_STATIC) != 0, name, descriptor, renamer);
    }

    public interface Skippable {
        void skip();
    }

    public class VariableMethodVisitor extends MethodVisitor implements Skippable {
        private final boolean isStatic;
        private final String methodName;
        private final String methodDesc;
        private final Renamer renamer;

        private boolean skipped;
        private boolean dontGenerate;

        public VariableMethodVisitor(MethodVisitor methodVisitor, boolean isStatic, String methodName, String methodDesc, Renamer renamer) {
            super(VariableNameGenerator.this.api, methodVisitor);
            this.isStatic = isStatic;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
            this.renamer = renamer;
        }

        @Override
        public void visitInvokeDynamicInsn(String n, String desc, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            if("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethodHandle.getOwner())) {
                Handle lambdaHandle = (Handle) bootstrapMethodArguments[1];
                if(sharedRenamers.putIfAbsent(lambdaHandle.getName().concat(lambdaHandle.getDesc()), renamer) == null) {
                    LOGGER.log(Level.FINEST, "Method {0}{1} is going to share renamer with {2}{3}",
                            new Object[] {methodName, methodDesc, lambdaHandle.getName(), lambdaHandle.getDesc()});
                }
            }
            if("java/lang/runtime/ObjectMethods".equals(bootstrapMethodHandle.getOwner())) this.dontGenerate = true;
            super.visitInvokeDynamicInsn(n, desc, bootstrapMethodHandle, bootstrapMethodArguments);
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            if(dontGenerate || skipped) {
                super.visitLocalVariable(name, descriptor, signature, start, end, index);
                skipped = false;
            } else if(index != 0 || isStatic) {
                super.visitLocalVariable(renamer.getVarName(Type.getType(descriptor)), descriptor, signature, start, end, index);
            } else super.visitLocalVariable(name, descriptor, signature, start, end, index);
        }

        @Override
        public void skip() {
            this.skipped = true;
        }
    }

    public static class Renamer {
        private record Holder(boolean skipZero, @NotNull String... names) {
            private Holder(@NotNull String... names) {
                this(true, names);
            }
        }

        private static final Object2ObjectOpenHashMap<String, Holder> PREDEF = new Object2ObjectOpenHashMap<>();
        static {
            Holder h = new Holder("i", "j", "k", "l");
            PREDEF.put("int",  h);
            PREDEF.put("long", h);
            PREDEF.put("byte",    new Holder(false, "b"    ));
            PREDEF.put("char",    new Holder(false, "c"    ));
            PREDEF.put("short",   new Holder(false, "short"));
            PREDEF.put("double",  new Holder(false, "d"    ));
            PREDEF.put("boolean", new Holder("flag"    ));
            PREDEF.put("float",   new Holder("f"       ));
            PREDEF.put("String",  new Holder("s"       ));
            PREDEF.put("Class",   new Holder("oclass"  ));
            PREDEF.put("Long",    new Holder("olong"   ));
            PREDEF.put("Byte",    new Holder("obyte"   ));
            PREDEF.put("Short",   new Holder("oshort"  ));
            PREDEF.put("Boolean", new Holder("obool"   ));
            PREDEF.put("Float",   new Holder("ofloat"  ));
            PREDEF.put("Double",  new Holder("odouble" ));
            PREDEF.put("Package", new Holder("opackage"));
            PREDEF.put("Enum",    new Holder("oenum"   ));
            PREDEF.put("Void",    new Holder("ovoid"   ));
        }

        private final Object2IntOpenHashMap<String> vars = new Object2IntOpenHashMap<>();

        {
            vars.defaultReturnValue(0);
        }

        public String getVarName(Type type) {
            boolean isArray = false;
            if(type.getSort() == Type.ARRAY) {
                type = type.getElementType();
                isArray = true;
            }
            String varBaseName = type.getClassName();
            if(type.getSort() == Type.OBJECT) varBaseName = varBaseName.substring(varBaseName.lastIndexOf('.') + 1);
            Holder holder = isArray ? null : PREDEF.get(varBaseName);
            if(holder != null) {
                if(holder.names.length == 1) {
                    String s = holder.names[0];
                    int count = vars.addTo(s, 1);
                    return s + (count == 0 && holder.skipZero ? "" : count);
                } else {
                    for(int i = 0; ; i++) {
                        for(String s : holder.names) {
                            if(i >= vars.getInt(s)) {
                                int j = vars.addTo(s, 1);
                                return s + (j == 0 && holder.skipZero ? "" : j);
                            }
                        }
                    }
                }
            } else {
                varBaseName = varBaseName.replace('$','_').toLowerCase(Locale.ENGLISH);
                if(isArray) varBaseName = "a" + varBaseName;
                int count = vars.addTo(varBaseName, 1);
                return varBaseName + (count > 0 ? count : "");
            }
        }
    }
}
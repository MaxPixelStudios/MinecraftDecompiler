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

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.mapping1.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping1.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping1.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping1.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import it.unimi.dsi.fastutil.objects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalVariableTableRenamer extends ClassVisitor {
    private static final Logger LOGGER = LogManager.getLogger("LVT Renamer");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("o");

    private final Object2ObjectOpenHashMap<String, Renamer> sharedRenamers = new Object2ObjectOpenHashMap<>();
    private final Matcher placeholderMatcher = PLACEHOLDER_PATTERN.matcher("");
    private String className;
    private final boolean rvn;

    private final String fromNamespace;
    private final String toNamespace;
    private final ClassMapping<NamespacedMapping> mapping;

    public LocalVariableTableRenamer(ClassVisitor classVisitor, boolean rvn) {
        super(Info.ASM_VERSION, classVisitor);
        this.rvn = rvn;
        this.fromNamespace = null;
        this.toNamespace = null;
        this.mapping = null;
    }

    public LocalVariableTableRenamer(ClassVisitor classVisitor, boolean rvn, String fromNamespace, String toNamespace, ClassMapping<NamespacedMapping> mapping) {
        super(Info.ASM_VERSION, classVisitor);
        this.rvn = rvn;
        this.fromNamespace = Objects.requireNonNull(fromNamespace);
        this.toNamespace = Objects.requireNonNull(toNamespace);
        this.mapping = Objects.requireNonNull(mapping);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if(mapping != null && !mapping.mapping.getName(fromNamespace).equals(name))
            throw new IllegalArgumentException("Mapping mismatch");
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // Filter some methods because only lambda methods need to share renamer with the caller
        Renamer renamer = (access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE)) == (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE) ?
                sharedRenamers.getOrDefault(String.join(".", className, name, descriptor), new Renamer()) : new Renamer();
        if((access & Opcodes.ACC_ABSTRACT) != 0 && recordStarted && !descriptor.startsWith("()")) {
            StringJoiner joiner = new StringJoiner(" ").add(className).add(name).add(descriptor);
            LOGGER.trace("Generation of abstract parameter names started for method {}{} in class {}", name, descriptor, className);
            for(Type type : Type.getArgumentTypes(descriptor)) {
                joiner.add(renamer.getVarName(type));
                LOGGER.trace("Generated an abstract parameter name from descriptor \"{}\" for method {}{} in class {}",
                        type::getDescriptor, () -> name, () -> descriptor, () -> className);
            }
            generatedAbstractParameterNames.add(joiner.toString());
            LOGGER.trace("Generation of abstract parameter names completed for method {}{} in class {}", name, descriptor, className);
        }
        Optional<LocalVariableTable.Namespaced> lvt = Optional.ofNullable(mapping)
                .map(ClassMapping::getMethods)
                .map(ObjectList::parallelStream)
                .flatMap(stream -> {
                    NamespacedMapping[] m = stream.filter(mapping -> {
                        Descriptor.Namespaced desc = mapping.getComponent(Descriptor.Namespaced.class);
                        if(!desc.getDescriptorNamespace().equals(fromNamespace))
                            throw new IllegalArgumentException("Descriptor namespace mismatch");
                        return mapping.getName(fromNamespace).equals(name) && desc.getUnmappedDescriptor().equals(descriptor);
                    }).toArray(NamespacedMapping[]::new);
                    if(m.length > 1) throw new IllegalArgumentException("Method duplicated");
                    return m.length == 1 && m[0].hasComponent(LocalVariableTable.Namespaced.class) ? Optional.of(m[0]) : Optional.empty();
                })
                .map(LocalVariableTable.Namespaced.class::cast);
        return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            @Override
            public void visitInvokeDynamicInsn(String n, String desc, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                if("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethodHandle.getOwner())) {
                    Handle lambdaHandle = (Handle) bootstrapMethodArguments[1];
                    if(sharedRenamers.putIfAbsent(String.join(".", className, lambdaHandle.getName(), lambdaHandle.getDesc()),
                            renamer) == null) {
                        LOGGER.trace("Method {}{} is going to share renamer with {}{} in class {}",
                                name, descriptor, lambdaHandle.getName(), lambdaHandle.getDesc(), className);
                    }
                }
                super.visitInvokeDynamicInsn(n, desc, bootstrapMethodHandle, bootstrapMethodArguments);
            }

            @Override
            public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                if(index == 0 && (access & Opcodes.ACC_STATIC) == 0) {
                    super.visitLocalVariable("this", descriptor, signature, start, end, index);
                } else {
                    if(lvt.isPresent()) {
                        String s = lvt.get().getLocalVariableName(index, toNamespace);
                        if(s != null && !s.isBlank() && !placeholderMatcher.reset(s).matches()) {
                            super.visitLocalVariable(s, descriptor, signature, start, end, index);
                            return;
                        }
                    }
                    if(rvn) {
                        super.visitLocalVariable(renamer.getVarName(Type.getType(descriptor)), descriptor, signature, start, end, index);
                        return;
                    }
                    super.visitLocalVariable(name, descriptor, signature, start, end, index);
                }
            }
        };
    }

    private static final ObjectList<String> generatedAbstractParameterNames = ObjectLists.synchronize(new ObjectArrayList<>());
    private static volatile boolean recordStarted;

    public static void startRecord() {
        if(recordStarted) throw new IllegalStateException("Record already started");
        generatedAbstractParameterNames.clear();
        LOGGER.trace("Cleared previously generated abstract parameter names(if any)");
        recordStarted = true;
        LOGGER.debug("Started to record the generated abstract method parameter names");
    }

    public static void endRecord(Path writeTo) throws IOException {
        if(!recordStarted) throw new IllegalStateException("Record not started yet");
        FileUtil.deleteIfExists(writeTo);
        Files.writeString(FileUtil.ensureFileExist(writeTo), String.join("\n", generatedAbstractParameterNames),
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        LOGGER.debug("Saved record to {}", writeTo);
        recordStarted = false;
        LOGGER.debug("Ended record");
    }

    public static class Renamer {
        private static class Holder {
            public final int id;
            public final boolean skip_zero;
            public final ObjectArrayList<String> names = new ObjectArrayList<>();

            public Holder(int id, boolean skip_zero, String... names) {
                this.id = id;
                this.skip_zero = skip_zero;
                this.names.addElements(0, names);
            }
        }

        private static final Object2ObjectOpenHashMap<String, Holder> PREDEF = new Object2ObjectOpenHashMap<>();
        static {
            PREDEF.put("int",       new Holder(0, true,  "i", "j", "k", "l"));
            PREDEF.put("byte",      new Holder(0, false, "b"       ));
            PREDEF.put("char",      new Holder(0, false, "c"       ));
            PREDEF.put("short",     new Holder(1, false, "short"   ));
            PREDEF.put("boolean",   new Holder(0, true,  "flag"    ));
            PREDEF.put("double",    new Holder(0, false, "d"       ));
            PREDEF.put("float",     new Holder(0, true,  "f"       ));
            PREDEF.put("File",      new Holder(1, true,  "file"    ));
            PREDEF.put("String",    new Holder(0, true,  "s"       ));
            PREDEF.put("Class",     new Holder(0, true,  "oclass"  ));
            PREDEF.put("Long",      new Holder(0, true,  "olong"   ));
            PREDEF.put("Byte",      new Holder(0, true,  "obyte"   ));
            PREDEF.put("Short",     new Holder(0, true,  "oshort"  ));
            PREDEF.put("Float",     new Holder(0, true,  "ofloat"  ));
            PREDEF.put("Double",    new Holder(0, true,  "odouble"  ));
            PREDEF.put("Boolean",   new Holder(0, true,  "obool"   ));
            PREDEF.put("Package",   new Holder(0, true,  "opackage"));
            PREDEF.put("Enum",      new Holder(0, true,  "oenum"   ));
            PREDEF.put("Void",      new Holder(0, true,  "ovoid"   ));
        }

        private final Object2IntOpenHashMap<String> vars = new Object2IntOpenHashMap<>();
        private final Object2IntOpenHashMap<Holder> ids = new Object2IntOpenHashMap<>();

        public String getVarName(Type type) {
            boolean isArray = false;
            if(type.getSort() == Type.ARRAY) {
                type = type.getElementType();
                isArray = true;
            }
            String varBaseName = type.getClassName();
            if(type.getSort() == Type.OBJECT) varBaseName = varBaseName.substring(varBaseName.lastIndexOf('.') + 1);
            Holder holder = isArray ? null : PREDEF.get(varBaseName.equals("long") ? "int" : varBaseName);
            varBaseName = (isArray ? "a" : "") + varBaseName.replace('$','_').toLowerCase(Locale.ENGLISH);
            if(holder != null) {
                if(holder.names.size() == 1) {
                    varBaseName = holder.names.get(0);
                    int count = vars.getOrDefault(varBaseName, holder.id);
                    vars.put(varBaseName, count + 1);
                    return varBaseName + (count == 0 && holder.skip_zero ? "" : count);
                } else {
                    ids.computeIfAbsent(holder, (ToIntFunction<? super Holder>) h -> h.id);
                    for(;;) {
                        for(int i = 0; i < holder.names.size(); ++i) {
                            varBaseName = holder.names.get(i);
                            if(!vars.containsKey(varBaseName)) {
                                int j = ids.getInt(holder);
                                vars.put(varBaseName, j);
                                return varBaseName + (j == 0 && holder.skip_zero ? "" : j);
                            }
                        }
                        ids.addTo(holder, 1);
                        for(int i = 0; i < holder.names.size(); ++i) vars.removeInt(holder.names.get(i));
                    }
                }
            } else {
                int count = vars.getInt(varBaseName);
                vars.put(varBaseName, count + 1);
                return varBaseName + (count > 0 ? count : "");
            }
        }
    }
}
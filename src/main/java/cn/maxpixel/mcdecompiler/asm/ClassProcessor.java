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
import cn.maxpixel.mcdecompiler.reader.AbstractMappingReader;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.ToIntFunction;

public class ClassProcessor extends ClassNode {
    private static final Logger LOGGER = LogManager.getLogger("Class Processor");
    private final boolean rvn;
    private final boolean isNamespaced;

    // Optional fields
    private NamespacedClassMapping mapping;
    private String fromNamespace;
    private final String targetNamespace;

    public ClassProcessor(boolean rvn, AbstractMappingReader reader, Object2ObjectOpenHashMap<String, ? extends NamespacedClassMapping> mappings, String targetNamespace) {
        super(Opcodes.ASM9);
        this.rvn = rvn;
        this.targetNamespace = targetNamespace;
        if(reader.getProcessor().isNamespaced()) {
            this.isNamespaced = true;
            this.fromNamespace = reader.getProcessor().asNamespaced().getNamespaces()[0];
            this.mapping = mappings.get(name);
        } else this.isNamespaced = false;
    }

    @Override
    public void visitEnd() {
        fixAnnotations();
        renameLVT();
    }

    // Similar to https://github.com/ModCoderPack/MCInjector/blob/master/src/main/java/de/oceanlabs/mcp/mcinjector/adaptors/ParameterAnnotationFixer.java
    private void fixAnnotations() {
        String toProcess = innerClasses.stream().filter(icn -> icn.name.equals(name)).findFirst()
                .filter(icn -> (icn.access & (Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE)) == 0 && icn.innerName != null)
                .map(icn -> '(' + Type.getObjectType(icn.outerName).getDescriptor())
                .orElse((access & Opcodes.ACC_ENUM) != 0 ? "(Ljava/lang/String;I" : null);
        if(toProcess != null) {
            methods.stream().filter(mn -> mn.name.equals("<init>") && mn.desc.startsWith(toProcess)).forEach(mn -> {
                int params = Type.getArgumentTypes(mn.desc).length;
                int synthetics = (access & Opcodes.ACC_ENUM) != 0 ? 2 : 1;
                mn.visibleParameterAnnotations = processAnnotations(params, synthetics, mn.visibleParameterAnnotations);
                mn.invisibleParameterAnnotations = processAnnotations(params, synthetics, mn.invisibleParameterAnnotations);
                if(mn.visibleParameterAnnotations != null) mn.visibleAnnotableParameterCount = mn.visibleParameterAnnotations.length;
                if(mn.invisibleParameterAnnotations != null) mn.invisibleAnnotableParameterCount = mn.invisibleParameterAnnotations.length;
            });
        }
    }

    private List<AnnotationNode>[] processAnnotations(int params, int synthetics, List<AnnotationNode>[] annotations) {
        if(annotations == null) return null;
        int annotationsCount = annotations.length;
        if(params == annotationsCount) return Arrays.copyOfRange(annotations, synthetics, annotationsCount);
        return annotations;
    }

    // --------------------------------------------------------------------------------------------------------

    private void renameLVT() {
        methods.forEach(methodNode -> {
            if(methodNode.localVariables != null && isNamespaced) {
                Optional<NamespacedMethodMapping> methodMapping = mapping.getMethods().parallelStream()
                        .filter(m -> m.getName(fromNamespace).equals(methodNode.name) &&
                                m.getUnmappedDescriptor().equals(methodNode.desc)).findAny();
                IntArrayList regen = new IntArrayList();
                methodNode.localVariables.forEach(lvn -> methodMapping.map(mm -> mm.getLocalVariableName(lvn.index, targetNamespace))
                        .filter(name -> !name.isEmpty() && !name.equals("o"/* tsrg2 empty lvn placeholder */))
                        .ifPresentOrElse(name -> lvn.name = name, () -> regen.add(lvn.index)));
                if(rvn) regenerateVariableNames(methodNode, regen);
            } else if(rvn) regenerateVariableNames(methodNode, null);
        });
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
        Files.writeString(writeTo, String.join("\n", generatedAbstractParameterNames),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LOGGER.debug("Saved record to {}", writeTo);
        recordStarted = false;
        LOGGER.debug("Ended record");
    }

    private final Object2ObjectOpenHashMap<String, Renamer> sharedRenamers = new Object2ObjectOpenHashMap<>();

    private void regenerateVariableNames(MethodNode node, IntArrayList needToRegenerate) {
        // Filter some methods because only lambda methods need to share renamer with the caller(save the time)
        Renamer renamer = (access & Opcodes.ACC_SYNTHETIC) != 0 && node.name.startsWith("lambda$") ?
                sharedRenamers.getOrDefault(String.join(".", name, node.name, node.desc), new Renamer())
                : new Renamer();
        if((node.access & Opcodes.ACC_ABSTRACT) != 0 && recordStarted && !node.desc.startsWith("()")) {
            StringJoiner joiner = new StringJoiner(" ");
            joiner.add(name).add(node.name).add(node.desc);
            LOGGER.trace("Generation of abstract parameter names started for method {}{} in class {}", node.name, node.desc, name);
            for(Type type : Type.getArgumentTypes(node.desc)) {
                joiner.add(renamer.getVarName(type));
                LOGGER.trace("Generated an abstract parameter name from descriptor \"{}\" for method {}{} in class {}",
                        type::getDescriptor, () -> node.name, () -> node.desc, () -> name);
            }
            generatedAbstractParameterNames.add(joiner.toString());
            LOGGER.trace("Generation of abstract parameter names completed for method {}{} in class {}", node.name, node.desc, name);
        }
        for(AbstractInsnNode in : node.instructions) {
            if(in.getType() == AbstractInsnNode.INVOKE_DYNAMIC_INSN) {
                InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) in;
                if(!indy.bsm.getOwner().equals("java/lang/invoke/StringConcatFactory")) {
                    Handle lambdaHandle = (Handle) indy.bsmArgs[1];
                    if(lambdaHandle.getOwner().equals(name) && lambdaHandle.getName().startsWith("lambda$") &&
                            sharedRenamers.putIfAbsent(String.join(".", name, lambdaHandle.getName(),
                                    lambdaHandle.getDesc()), renamer) == null)
                        LOGGER.trace("Method {}{} is going to share renamer with {}{} in class {}",
                                () -> node.name, () -> node.desc, lambdaHandle::getName, lambdaHandle::getDesc, () -> name);
                }
            }
        }
        if(node.localVariables != null) node.localVariables.forEach(lvn -> {
            if(needToRegenerate == null || needToRegenerate.contains(lvn.index)) {
                lvn.name = renamer.getVarName(Type.getType(lvn.desc));
            }
        });
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
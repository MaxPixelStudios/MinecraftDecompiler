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

package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping1.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping1.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping1.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping1.component.Documented;
import cn.maxpixel.mcdecompiler.mapping1.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping1.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.util.MappingUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.*;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MappingProcessors {
    public static final MappingProcessor.Classified<PairedMapping> SRG = new MappingProcessor.Classified<>() {
        @Override
        public boolean supportPackage() {
            return true;
        }

        @Override
        public Pair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), new ObjectArrayList<>());
            Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: unmapped name
            content.parallelStream().forEach(s -> {
                String[] strings = s.split(" ", 6);
                if(s.startsWith("CL:")) {
                    ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(strings[1], strings[2]));
                    synchronized(classes) {
                        classes.putIfAbsent(classMapping.mapping.unmappedName, classMapping);
                    }
                } else if(s.startsWith("FD:")) {
                    PairedMapping fieldMapping = MappingUtil.Paired.o(getName(strings[1]), getName(strings[2]));
                    String unmClassName = getClassName(strings[1]);
                    synchronized(classes) {
                        classes.computeIfAbsent(unmClassName, k -> new ClassMapping<>(new PairedMapping(unmClassName, getClassName(strings[2]))))
                                .addField(fieldMapping);
                    }
                } else if(s.startsWith("MD:")) {
                    PairedMapping methodMapping = MappingUtil.Paired.d2o(getName(strings[1]), getName(strings[3]), strings[2], strings[4]);
                    String unmClassName = getClassName(strings[1]);
                    synchronized(classes) {
                        classes.computeIfAbsent(unmClassName, k -> new ClassMapping<>(new PairedMapping(unmClassName, getClassName(strings[3]))))
                                .addMethod(methodMapping);
                    }
                } else if(s.startsWith("PK:")) synchronized(mappings.right()) {
                    mappings.right().add(new PairedMapping(strings[1], strings[2]));
                } else throw new IllegalArgumentException("Is this a SRG mapping file?");
            });
            mappings.left().addAll(classes.values());
            return mappings;
        }

        private String getClassName(String s) {
            return s.substring(0, s.lastIndexOf('/'));
        }

        private String getName(String s) {
            return s.substring(s.lastIndexOf('/') + 1);
        }
    };

    public static final MappingProcessor.Classified<PairedMapping> CSRG = new MappingProcessor.Classified<>() {
        private static final Function<String, ClassMapping<PairedMapping>> COMPUTE_FUNC = name ->
                new ClassMapping<>(new PairedMapping(name, name));

        @Override
        public boolean supportPackage() {
            return true;
        }

        @Override
        public Pair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), new ObjectArrayList<>());
            Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: unmapped name
            content.parallelStream().forEach(s -> {
                String[] sa = s.split(" ", 5);
                switch(sa.length) {
                    case 2: // Class / Package
                        if(sa[0].charAt(sa[0].length() - 1) == '/') synchronized(mappings.right()) {
                            mappings.right().add(new PairedMapping(sa[0].substring(0, sa[0].length() - 1),
                                    sa[1].substring(0, sa[1].length() - 1)));
                        } else {
                            ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(sa[0], sa[1]));
                            synchronized(classes) {
                                classes.merge(classMapping.mapping.unmappedName, classMapping, (o, n) -> {
                                    n.addFields(o.getFields());
                                    n.addMethods(o.getMethods());
                                    return n;
                                });
                            }
                        }
                        break;
                    case 3: // Field
                        PairedMapping fieldMapping = MappingUtil.Paired.o(sa[1], sa[2]);
                        synchronized(classes) {
                            classes.computeIfAbsent(sa[0], COMPUTE_FUNC).addField(fieldMapping);
                        }
                        break;
                    case 4: // Method
                        PairedMapping methodMapping = MappingUtil.Paired.duo(sa[1], sa[3], sa[2]);
                        synchronized(classes) {
                            classes.computeIfAbsent(sa[0], COMPUTE_FUNC).addMethod(methodMapping);
                        }
                        break;
                    default: throw new IllegalArgumentException("Is this a CSRG mapping file?");
                }
            });
            mappings.left().addAll(classes.values());
            return mappings;
        }
    };

    public static final MappingProcessor.Classified<PairedMapping> TSRG_V1 = new MappingProcessor.Classified<>() {
        @Override
        public boolean supportPackage() {
            return true;
        }

        @Override
        public Pair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), new ObjectArrayList<>());
            for(int i = 0, len = content.size(); i < len; i++) {
                String[] sa = content.get(i).split(" ");
                if(sa[0].charAt(0) != '\t') {
                    if(sa[0].charAt(sa[0].length() - 1) == '/')
                        mappings.right().add(new PairedMapping(sa[0], sa[1]));
                    else {
                        ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(sa[0], sa[1]));
                        i = processTree(i, len, content, classMapping);
                        mappings.left().add(classMapping);
                    }
                } else error();
            }
            return mappings;
        }

        private static int processTree(int index, int size, ObjectList<String> content, ClassMapping<PairedMapping> classMapping) {
            if(index + 1 >= size) return index;
            String s = content.get(index + 1);
            if(s.charAt(0) == '\t') {
                String[] sa = s.substring(1).split(" ");
                switch(sa.length) {
                    case 2 -> classMapping.addField(MappingUtil.Paired.o(sa[0], sa[1]));
                    case 3 -> classMapping.addMethod(MappingUtil.Paired.duo(sa[0], sa[2], sa[1]));
                    default -> error();
                }
                return processTree(index + 1, size, content, classMapping);
            }
            return index;
        }

        private static void error() {
            throw new IllegalArgumentException("Is this a TSRG v1 mapping file?");
        }
    };

    public static final MappingProcessor.Classified<NamespacedMapping> TSRG_V2 = new MappingProcessor.Classified<>() {
        @Override
        public boolean supportPackage() {
            return true;
        }

        @Override
        public Pair<ObjectList<ClassMapping<NamespacedMapping>>, ObjectList<NamespacedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<NamespacedMapping>>, ObjectList<NamespacedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), new ObjectArrayList<>());
            if(!content.get(0).startsWith("tsrg2")) error();
            String[] namespaces = content.get(0).substring(6).split(" ");
            for(int i = 1, len = content.size(); i < len; ) {
                String[] sa = content.get(i).split(" ");
                if(sa[0].charAt(0) != '\t') {
                    if(sa[0].charAt(sa[0].length() - 1) == '/') {
                        for(int j = 0; j < sa.length; j++) sa[j] = sa[j].substring(0, sa[j].length() - 1);
                        mappings.right().add(new NamespacedMapping(namespaces, sa));
                        i++;
                    } else {
                        ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(new NamespacedMapping(namespaces, sa));
                        i = processTree(i, len, namespaces, content, classMapping);
                        mappings.left().add(classMapping);
                    }
                } else error();
            }
            return mappings;
        }

        private static int processTree(int index, int size, String[] namespaces, ObjectList<String> content,
                                       ClassMapping<NamespacedMapping> classMapping) {
            for(index = index + 1; index < size; index++) {
                String s = content.get(index);
                if(s.charAt(0) == '\t') {
                    String[] sa = s.substring(1).split(" ");
                    switch(sa.length - namespaces.length) {
                        case 0 -> classMapping.addField(MappingUtil.Namespaced.o(namespaces, sa));
                        case 1 -> {
                            String desc = sa[1];
                            sa[1] = sa[0];
                            if(desc.charAt(0) == '(') {
                                NamespacedMapping methodMapping = MappingUtil.Namespaced.slduo(
                                        namespaces, sa, 1, namespaces[0], desc);
                                index = processTree1(index, size, namespaces, content, methodMapping);
                                classMapping.addMethod(methodMapping);
                            } else {
                                classMapping.addField(MappingUtil.Namespaced.duo(namespaces,
                                        sa, 1, namespaces[0], desc));
                            }
                        }
                        default -> error();
                    }
                } else return index;
            }
            return index;
        }

        private static int processTree1(int index, int size, String[] namespaces, ObjectList<String> content, NamespacedMapping methodMapping) {
            for(index = index + 1; index < size; index++) {
                String s = content.get(index);
                if(s.charAt(1) == '\t') {
                    if(s.equals("\t\tstatic")) methodMapping.getComponent(StaticIdentifiable.class).setStatic(true);
                    else {
                        String[] sa = s.substring(2).split(" ");
                        methodMapping.getComponent(LocalVariableTable.Namespaced.class)
                                .setLocalVariableName(Integer.parseInt(sa[0]), namespaces, sa, 1);
                    }
                } else return index - 1;
            }
            return index;
        }

        private static void error() {
            throw new IllegalArgumentException("Is this a TSRG v2 mapping file?");
        }
    };

    public static final MappingProcessor.Classified<PairedMapping> PROGUARD = new MappingProcessor.Classified<>() {
        private static final Pattern CLASS_PATTERN = Pattern.compile(" -> |:");
        private static final Pattern METHOD_PATTERN = Pattern.compile("[: (]|\\) -> ");
        private static final Pattern FIELD_PATTERN = Pattern.compile(" (-> )?");

        @Override
        public Pair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), ObjectLists.emptyList());
            Matcher classMatcher = CLASS_PATTERN.matcher("");
            Matcher fieldMatcher = FIELD_PATTERN.matcher("");
            Matcher methodMatcher = METHOD_PATTERN.matcher("");
            for(int i = 0, len = content.size(); i < len; i++) {
                String s = content.get(i);
                if (!s.startsWith("    ")) {
                    String[] sa = split(s, classMatcher, 2, true);
                    ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(
                            NamingUtil.asNativeName(sa[1]), NamingUtil.asNativeName(sa[0])));
                    i = processTree(i, len, content, classMapping, fieldMatcher, methodMatcher);
                    mappings.left().add(classMapping);
                } else error();
            }
            return mappings;
        }

        private static int processTree(int index, int size, ObjectList<String> content, ClassMapping<PairedMapping> classMapping,
                                       Matcher fieldMatcher, Matcher methodMatcher) {
            if(index + 1 >= size) return index;
            String s = content.get(index + 1);
            if(s.startsWith("    ")) {
                if(s.contains("(") && s.contains(")")) {
                    String[] sa = split(s.substring(4), methodMatcher, 6, false);
                    if(sa.length == 6) {
                        StringBuilder descriptor = new StringBuilder("(");
                        for(String arg : sa[4].split(",")) descriptor.append(NamingUtil.asDescriptor(arg));
                        descriptor.append(')').append(NamingUtil.asDescriptor(sa[2]));
                        classMapping.addMethod(MappingUtil.Paired.ldmo(sa[5], sa[3],
                                descriptor.toString(), Integer.parseInt(sa[0]), Integer.parseInt(sa[1])));
                    } else if(sa.length == 4) {
                        StringBuilder descriptor = new StringBuilder("(");
                        for(String arg : sa[2].split(",")) descriptor.append(NamingUtil.asDescriptor(arg));
                        descriptor.append(')').append(NamingUtil.asDescriptor(sa[0]));
                        classMapping.addMethod(MappingUtil.Paired.dmo(sa[3], sa[1], descriptor.toString()));
                    } else error();
                } else {
                    String[] sa = split(s.substring(4), fieldMatcher, 3, true);
                    classMapping.addField(MappingUtil.Paired.dmo(sa[2], sa[1], NamingUtil.asDescriptor(sa[0])));
                }
                return processTree(index + 1, size, content, classMapping, fieldMatcher, methodMatcher);
            }
            return index;
        }

        private static String[] split(String s, Matcher matcher, int count, boolean force) {
            matcher.reset(s);
            String[] sa = new String[count];
            int index = 0;
            for(int i = 0; i < count; i++) {
                if(!matcher.find()) {
                    if(index < s.length()) {
                        sa[i] = s.substring(index);
                        if(i < count - 1) {
                            if(force) error();
                            String[] ret = new String[i + 1];
                            System.arraycopy(sa, 0, ret, 0, ret.length);
                            return ret;
                        }
                        break;
                    } else error();
                }
                sa[i] = s.substring(index, matcher.start());
                index = matcher.end();
            }
            if(matcher.find()) error();
            return sa;
        }

        private static void error() {
            throw new IllegalArgumentException("Is this a Proguard mapping file?");
        }
    };

    public static final MappingProcessor.Classified<NamespacedMapping> TINY_V1 = new MappingProcessor.Classified<>() {
        @Override
        public Pair<ObjectList<ClassMapping<NamespacedMapping>>, ObjectList<NamespacedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<NamespacedMapping>>, ObjectList<NamespacedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), ObjectLists.emptyList());
            Object2ObjectOpenHashMap<String, ClassMapping<NamespacedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: the first namespace, usually unmapped name
            if (!content.get(0).startsWith("v1")) error();
            String[] namespaces = content.get(0).substring(3).split("\t");
            String k = namespaces[0];
            content.parallelStream().skip(1).forEach(s -> {
                String[] sa = s.split("\t");
                if (s.startsWith("CLASS")) {
                    ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(new NamespacedMapping(namespaces, sa, 1));
                    synchronized (classes) {
                        classes.merge(sa[1], classMapping, (o, n) -> n.addFields(o.getFields()).addMethods(o.getMethods()));
                    }
                } else if (s.startsWith("FIELD")) {
                    NamespacedMapping fieldMapping = MappingUtil.Namespaced.duo(namespaces, sa, 3, k, sa[2]);
                    synchronized (classes) {
                        classes.computeIfAbsent(sa[1], key -> new ClassMapping<>(new NamespacedMapping(k, sa[1])))
                                .addField(fieldMapping);
                    }
                } else if (s.startsWith("METHOD")) {
                    NamespacedMapping methodMapping = MappingUtil.Namespaced.duo(namespaces, sa, 3, k, sa[2]);
                    synchronized (classes) {
                        classes.computeIfAbsent(sa[1], key -> new ClassMapping<>(new NamespacedMapping(k, sa[1])))
                                .addMethod(methodMapping);
                    }
                } else error();
            });
            return mappings;
        }

        private static void error() {
            throw new IllegalArgumentException("Is this a Tiny v1 mapping file?");
        }
    };

    public static final MappingProcessor.Classified<NamespacedMapping> TINY_V2 = new MappingProcessor.Classified<>() {
        @Override
        public Pair<ObjectList<ClassMapping<NamespacedMapping>>, ObjectList<NamespacedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<NamespacedMapping>>, ObjectList<NamespacedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), ObjectLists.emptyList());
            if (!content.get(0).startsWith("tiny\t2\t0")) error();
            String[] namespaces = content.get(0).substring(9).split("\t");
            for(int i = 1, len = content.size(); i < len; i++) {
                String[] sa = content.get(i).split("\t");
                if(sa[0].length() == 1 && sa[0].charAt(0) == 'c') {
                    ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(MappingUtil.Namespaced.d(namespaces, sa, 1));
                    i = processTree(i, len, namespaces, content, classMapping);
                    mappings.left().add(classMapping);
                } else error();
            }
            return mappings;
        }

        private static int processTree(int index, int size, String[] namespaces, ObjectList<String> content,
                                       ClassMapping<NamespacedMapping> classMapping) {
            if(index + 1 >= size) return index;
            String s = content.get(index + 1);
            if(s.charAt(0) == '\t') {
                String[] sa = s.substring(1).split("\t");
                switch(sa[0].charAt(0)) {
                    case 'c' -> classMapping.mapping.getComponent(Documented.class).setDoc(sa[1]);
                    case 'f' -> {
                        NamespacedMapping fieldMapping = MappingUtil.Namespaced.dduo(namespaces, sa, 2, namespaces[0], sa[1]);
                        index = processTree1(index + 1, size, namespaces, content, fieldMapping) - 1;
                        classMapping.addField(fieldMapping);
                    }
                    case 'm' -> {
                        NamespacedMapping methodMapping = MappingUtil.Namespaced.dllduo(namespaces, sa, 2, namespaces[0], sa[1]);
                        index = processTree1(index + 1, size, namespaces, content, methodMapping) - 1;
                        classMapping.addMethod(methodMapping);
                    }
                    default -> error();
                }
                return processTree(index + 1, size, namespaces, content, classMapping);
            }
            return index;
        }

        private static int processTree1(int index, int size, String[] namespaces, ObjectList<String> content,
                                        NamespacedMapping fieldOrMethod) {
            if(index + 1 >= size) return index;
            String s = content.get(index + 1);
            if(s.charAt(1) == '\t' && s.charAt(0) == '\t') {
                switch(s.charAt(2)) {
                    case 'c' -> fieldOrMethod.getComponent(Documented.class).setDoc(s.substring(4));
                    case 'p' -> {
                        String[] sa = s.substring(4).split("\t");
                        int i = Integer.parseInt(sa[0]);
                        fieldOrMethod.getComponent(LocalVariableTable.Namespaced.class).setLocalVariableName(i, namespaces, sa, 1);
                        index = processTree2(index + 1, size, i, content, fieldOrMethod) - 1;
                    }
                    default -> error();
                }
                return processTree1(index + 1, size, namespaces, content, fieldOrMethod);
            }
            return index;
        }

        private static int processTree2(int index, int size, int i, ObjectList<String> content, NamespacedMapping methodMapping) {
            if(index + 1 >= size) return index;
            String s = content.get(index + 1);
            if(s.charAt(2) == '\t' && s.charAt(1) == '\t' && s.charAt(0) == '\t') {
                if(s.charAt(3) == 'c') methodMapping.getComponent(Documented.LocalVariable.class).setLocalVariableDoc(i, s.substring(5));
                else error();
                return index + 1;
            }
            return index;
        }

        private static void error() {
            throw new IllegalArgumentException("Is this a Tiny v2 mapping file?");
        }
    };
}
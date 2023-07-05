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

package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import cn.maxpixel.mcdecompiler.mapping.type.MappingTypes;
import cn.maxpixel.mcdecompiler.util.MappingUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.*;

import java.util.function.Function;

public interface MappingProcessors {
    MappingProcessor.Classified<PairedMapping> SRG = new MappingProcessor.Classified<>() {
        private static final Function<String, Function<String, ClassMapping<PairedMapping>>> MAPPING_FUNC = s ->
                k -> new ClassMapping<>(new PairedMapping(k, getClassName(s)));

        @Override
        public MappingType<PairedMapping, ObjectList<ClassMapping<PairedMapping>>> getType() {
            return MappingTypes.SRG;
        }

        @Override
        public Pair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), new ObjectArrayList<>());
            Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: unmapped name
            content.parallelStream().forEach(s -> {
                String[] strings = s.split(" ", 6);
                switch(strings[0]) {
                    case "CL:":
                        ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(strings[1], strings[2]));
                        synchronized(classes) {
                            classes.putIfAbsent(strings[1], classMapping);
                        }
                        break;
                    case "FD:":
                        PairedMapping fieldMapping = MappingUtil.Paired.o(getName(strings[1]), getName(strings[2]));
                        String unmClassName = getClassName(strings[1]);
                        synchronized(classes) {
                            classes.computeIfAbsent(unmClassName, MAPPING_FUNC.apply(strings[2]))
                                    .addField(fieldMapping);
                        }
                        break;
                    case "MD:":
                        PairedMapping methodMapping = MappingUtil.Paired.d2o(getName(strings[1]), getName(strings[3]), strings[2], strings[4]);
                        unmClassName = getClassName(strings[1]);
                        synchronized(classes) {
                            classes.computeIfAbsent(unmClassName, MAPPING_FUNC.apply(strings[3]))
                                    .addMethod(methodMapping);
                        }
                        break;
                    case "PK:":
                        synchronized(mappings.right()) {
                            mappings.right().add(new PairedMapping(strings[1], strings[2]));
                        }
                        break;
                    default: throw new IllegalArgumentException("Is this a SRG mapping file?");
                }
            });
            mappings.left().addAll(classes.values());
            return mappings;
        }

        private static String getClassName(String s) {
            return s.substring(0, s.lastIndexOf('/'));
        }

        private static String getName(String s) {
            return s.substring(s.lastIndexOf('/') + 1);
        }
    };

    MappingProcessor.Classified<PairedMapping> CSRG = new MappingProcessor.Classified<>() {
        private static final Function<String, ClassMapping<PairedMapping>> COMPUTE_FUNC = name ->
                new ClassMapping<>(new PairedMapping(name, name));

        @Override
        public MappingType<PairedMapping, ObjectList<ClassMapping<PairedMapping>>> getType() {
            return MappingTypes.CSRG;
        }

        @Override
        public Pair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), new ObjectArrayList<>());
            Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: unmapped name
            content.parallelStream().forEach(s -> {
                String[] sa = s.split(" ", 5);
                switch (sa.length) {
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

    MappingProcessor.Classified<PairedMapping> TSRG_V1 = new MappingProcessor.Classified<>() {
        @Override
        public MappingType<PairedMapping, ObjectList<ClassMapping<PairedMapping>>> getType() {
            return MappingTypes.TSRG_V1;
        }

        @Override
        public Pair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), new ObjectArrayList<>());
            for(int i = 0, len = content.size(); i < len;) {
                String[] sa = content.get(i).split(" ");
                if(sa[0].charAt(0) != '\t') {
                    if(sa[0].charAt(sa[0].length() - 1) == '/') {
                        mappings.right().add(new PairedMapping(sa[0].substring(0, sa[0].length() - 1),
                                sa[1].substring(0, sa[1].length() - 1)));
                        i++;
                    } else {
                        ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(sa[0], sa[1]));
                        i = processTree(i, len, content, classMapping);
                        mappings.left().add(classMapping);
                    }
                } else error();
            }
            return mappings;
        }

        private static int processTree(int index, int size, ObjectList<String> content, ClassMapping<PairedMapping> classMapping) {
            for(index = index + 1; index < size; index++) {
                String s = content.get(index);
                if(s.charAt(0) == '\t') {
                    String[] sa = s.substring(1).split(" ");
                    switch(sa.length) {
                        case 2 -> classMapping.addField(MappingUtil.Paired.o(sa[0], sa[1]));
                        case 3 -> classMapping.addMethod(MappingUtil.Paired.duo(sa[0], sa[2], sa[1]));
                        default -> error();
                    }
                } else break;
            }
            return index;
        }

        private static void error() {
            throw new IllegalArgumentException("Is this a TSRG v1 mapping file?");
        }
    };

    MappingProcessor.Classified<NamespacedMapping> TSRG_V2 = new MappingProcessor.Classified<>() {
        @Override
        public MappingType<NamespacedMapping, ObjectList<ClassMapping<NamespacedMapping>>> getType() {
            return MappingTypes.TSRG_V2;
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
                        mappings.right().add(new NamespacedMapping(namespaces, sa).setUnmappedNamespace(namespaces[0]));
                        i++;
                    } else {
                        ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(new NamespacedMapping(namespaces, sa)
                                .setUnmappedNamespace(namespaces[0]));
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
                } else break;
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
                                .setLocalVariable(Integer.parseInt(sa[0]), new NamespacedMapping(namespaces, sa, 1));
                    }
                } else break;
            }
            return index - 1;
        }

        private static void error() {
            throw new IllegalArgumentException("Is this a TSRG v2 mapping file?");
        }
    };

    MappingProcessor.Classified<PairedMapping> PROGUARD = new MappingProcessor.Classified<>() {
        @Override
        public MappingType<PairedMapping, ObjectList<ClassMapping<PairedMapping>>> getType() {
            return MappingTypes.PROGUARD;
        }

        @Override
        public Pair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<PairedMapping>>, ObjectList<PairedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), ObjectLists.emptyList());
            for(int i = 0, len = content.size(); i < len; ) {
                String s = content.get(i);
                if (!s.startsWith("    ")) {
                    int splitIndex = s.indexOf(" -> ");
                    if (splitIndex <= 0) error();
                    ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(
                            NamingUtil.asNativeName(s.substring(splitIndex + 4, s.length() - 1)),
                            NamingUtil.asNativeName(s.substring(0, splitIndex))
                    ));
                    i = processTree(i, len, content, classMapping);
                    mappings.left().add(classMapping);
                } else error();
            }
            return mappings;
        }

        private static int processTree(int index, int size, ObjectList<String> content, ClassMapping<PairedMapping> classMapping) {
            for (index = index + 1; index < size; index++) {
                String s = content.get(index);
                if (s.startsWith("    ")) {
                    if (s.contains("(") && s.contains(")")) {
                        int lineNum = s.indexOf(':');
                        int leftBracket = s.indexOf('(');
                        int rightBracket = s.lastIndexOf(')');
                        StringBuilder descriptor = new StringBuilder("(");
                        int prev = leftBracket;
                        for (int next = s.indexOf(',', prev + 1); next > 0;
                             prev = next, next = s.indexOf(',', prev + 1)) {
                            descriptor.append(NamingUtil.java2Descriptor(s.substring(prev + 1, next)));
                        }
                        if (rightBracket - 1 != leftBracket) descriptor.append(NamingUtil.java2Descriptor(s.substring(prev + 1, rightBracket)));
                        if (lineNum > 0) {
                            int split1 = s.indexOf(' ', 11);// skip leading 4 spaces, descriptor name(at least 3 chars), and line number(at least 4 chars)
                            if (split1 < 0) error();
                            int lineNum1 = s.indexOf(':', lineNum + 2);
                            if (lineNum1 < 0) error();
                            classMapping.addMethod(MappingUtil.Paired.ldmo(s.substring(rightBracket + 5), s.substring(split1 + 1, leftBracket),
                                    descriptor.append(')').append(NamingUtil.java2Descriptor(s.substring(lineNum1 + 1, split1))).toString(),
                                    Integer.parseInt(s.substring(4, lineNum)), Integer.parseInt(s.substring(lineNum + 1, lineNum1))));
                        } else { // no line number
                            int split1 = s.indexOf(' ', 7);// skip leading 4 spaces and descriptor name/line number(at least 3 chars)
                            if (split1 < 0) error();
                            classMapping.addMethod(MappingUtil.Paired.dmo(s.substring(rightBracket + 5), s.substring(split1 + 1, leftBracket),
                                    descriptor.append(')').append(NamingUtil.java2Descriptor(s.substring(4, split1))).toString()));
                        }
                    } else {
                        int split1 = s.indexOf(' ', 7);// skip leading 4 spaces and descriptor name(at least 3 chars)
                        if (split1 < 0) error();
                        int split2 = s.indexOf(" -> ", split1 + 2);// skip split1(1 char) and mapped name(at least 1 char)
                        if (split2 < 0) error();
                        classMapping.addField(MappingUtil.Paired.dmo(s.substring(split2 + 4),
                                s.substring(split1 + 1, split2), NamingUtil.java2Descriptor(s.substring(4, split1))));
                    }
                } else break;
            }
            return index;
        }

        private static void error() {
            throw new IllegalArgumentException("Is this a Proguard mapping file?");
        }
    };

    MappingProcessor.Classified<NamespacedMapping> TINY_V1 = new MappingProcessor.Classified<>() {
        private static final Function<String, Function<String, ClassMapping<NamespacedMapping>>> MAPPING_FUNC = k ->
                key -> new ClassMapping<>(new NamespacedMapping(k, key).setUnmappedNamespace(k));

        @Override
        public MappingType<NamespacedMapping, ObjectList<ClassMapping<NamespacedMapping>>> getType() {
            return MappingTypes.TINY_V1;
        }

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
                if(s.startsWith("CLASS")) {
                    ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(new NamespacedMapping(namespaces, sa, 1)
                            .setUnmappedNamespace(k));
                    synchronized(classes) {
                        classes.merge(sa[1], classMapping, (o, n) -> n.addFields(o.getFields()).addMethods(o.getMethods()));
                    }
                } else if(s.startsWith("FIELD")) {
                    NamespacedMapping fieldMapping = MappingUtil.Namespaced.duo(namespaces, sa, 3, k, sa[2]);
                    synchronized(classes) {
                        classes.computeIfAbsent(sa[1], MAPPING_FUNC.apply(k))
                                .addField(fieldMapping);
                    }
                } else if(s.startsWith("METHOD")) {
                    NamespacedMapping methodMapping = MappingUtil.Namespaced.duo(namespaces, sa, 3, k, sa[2]);
                    synchronized(classes) {
                        classes.computeIfAbsent(sa[1], MAPPING_FUNC.apply(k))
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

    MappingProcessor.Classified<NamespacedMapping> TINY_V2 = new MappingProcessor.Classified<>() {
        @Override
        public MappingType<NamespacedMapping, ObjectList<ClassMapping<NamespacedMapping>>> getType() {
            return MappingTypes.TINY_V2;
        }

        @Override
        public Pair<ObjectList<ClassMapping<NamespacedMapping>>, ObjectList<NamespacedMapping>> process(ObjectList<String> content) {
            ObjectObjectImmutablePair<ObjectList<ClassMapping<NamespacedMapping>>, ObjectList<NamespacedMapping>> mappings =
                    new ObjectObjectImmutablePair<>(new ObjectArrayList<>(), ObjectLists.emptyList());
            if (!content.get(0).startsWith("tiny\t2\t0")) error();
            String[] namespaces = content.get(0).substring(9).split("\t");
            for(int i = 1, len = content.size(); i < len; ) {
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
            for(index = index + 1; index < size; index++) {
                String s = content.get(index);
                if(s.charAt(0) == '\t') {
                    String[] sa = s.substring(3).split("\t");
                    switch(s.charAt(1)) {
                        case 'c' -> classMapping.mapping.getComponent(Documented.class).setDoc(sa[0]);
                        case 'f' -> {
                            NamespacedMapping fieldMapping = MappingUtil.Namespaced.dduo(namespaces, sa, 1, namespaces[0], sa[0]);
                            index = processTree1(index, size, namespaces, content, fieldMapping);
                            classMapping.addField(fieldMapping);
                        }
                        case 'm' -> {
                            NamespacedMapping methodMapping = MappingUtil.Namespaced.dlduo(namespaces, sa, 1, namespaces[0], sa[0]);
                            index = processTree1(index, size, namespaces, content, methodMapping);
                            classMapping.addMethod(methodMapping);
                        }
                        default -> error();
                    }
                } else break;
            }
            return index;
        }

        private static int processTree1(int index, int size, String[] namespaces, ObjectList<String> content,
                                        NamespacedMapping mapping) {
            for(index = index + 1; index < size; index++) {
                String s = content.get(index);
                if(s.charAt(1) == '\t' && s.charAt(0) == '\t') {
                    switch(s.charAt(2)) {
                        case 'c' -> mapping.getComponent(Documented.class).setDoc(s.substring(4));
                        case 'p' -> {
                            String[] sa = s.substring(4).split("\t");
                            NamespacedMapping localVariable = MappingUtil.Namespaced.d(namespaces, sa, 1);
                            mapping.getComponent(LocalVariableTable.Namespaced.class)
                                    .setLocalVariable(Integer.parseInt(sa[0]), localVariable);
                            index = processTree2(index, size, content, localVariable);
                        }
                        default -> error();
                    }
                } else break;
            }
            return index - 1;
        }

        private static int processTree2(int index, int size, ObjectList<String> content, NamespacedMapping localVariable) {
            if(++index < size) {
                String s = content.get(index);
                if(s.charAt(2) == '\t' && s.charAt(1) == '\t' && s.charAt(0) == '\t') {
                    if(s.charAt(3) == 'c') localVariable.getComponent(Documented.class).setDoc(s.substring(5));
                    else error();
                    return index;
                }
            }
            return index - 1;
        }

        private static void error() {
            throw new IllegalArgumentException("Is this a Tiny v2 mapping file?");
        }
    };
}
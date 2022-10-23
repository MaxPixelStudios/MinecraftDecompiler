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
import cn.maxpixel.mcdecompiler.util.IOUtil;
import cn.maxpixel.mcdecompiler.util.Logging;
import it.unimi.dsi.fastutil.objects.*;
import org.objectweb.asm.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ExtraClassesInformation implements Consumer<Path> {
    private static final Logger LOGGER = Logging.getLogger("Class Info Collector");
    private final Object2ObjectOpenHashMap<String, ObjectArrayList<String>> superClassMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, Object2IntOpenHashMap<String>> accessMap = new Object2ObjectOpenHashMap<>();
    private final Map<String, Map<String, String>> refMap;
    public final Object2ObjectOpenHashMap<String, ObjectSet<String>> dontRemap = new Object2ObjectOpenHashMap<>();

    public ExtraClassesInformation() {
        this(Object2ObjectMaps.emptyMap());
    }

    public ExtraClassesInformation(Stream<Path> classes) {
        this(classes, false);
    }

    public ExtraClassesInformation(Stream<Path> classes, boolean close) {
        this(Object2ObjectMaps.emptyMap(), classes, close);
    }

    public ExtraClassesInformation(Map<String, Map<String, String>> refMap) {
        this.refMap = refMap;
    }

    public ExtraClassesInformation(Map<String, Map<String, String>> refMap, Stream<Path> classes) {
        this(refMap, classes, false);
    }

    public ExtraClassesInformation(Map<String, Map<String, String>> refMap, Stream<Path> classes, boolean close) {
        this.refMap = refMap;
        if(close) try(classes) {
            classes.forEach(this);
        } else classes.forEach(this);
    }

    @Override
    public void accept(Path classFilePath) {
        try {
            ClassReader reader = new ClassReader(IOUtil.readAllBytes(classFilePath));
            String className = reader.getClassName();
            boolean needToRecord = (reader.getAccess() & (Opcodes.ACC_INTERFACE | Opcodes.ACC_RECORD)) == 0;
            boolean notEnum = (reader.getAccess() & Opcodes.ACC_ENUM) == 0;
            String superName = reader.getSuperName();
            String[] interfaces = reader.getInterfaces();
            int itfLen = interfaces.length;
            if(needToRecord && !superName.startsWith("java/")) {
                ObjectArrayList<String> list = new ObjectArrayList<>(itfLen + 1);
                list.add(superName);
                if(itfLen > 0) for(String itf : interfaces) {
                    if(itf.startsWith("java/")) continue;
                    list.add(itf);
                }
                synchronized(superClassMap) {
                    superClassMap.put(className, list);
                }
            } else if(itfLen > 0) {
                ObjectArrayList<String> list = new ObjectArrayList<>(itfLen);
                for(String itf : interfaces) {
                    if(itf.startsWith("java/")) continue;
                    list.add(itf);
                }
                synchronized(superClassMap) {
                    superClassMap.put(className, list);
                }
            }
            reader.accept(new ClassVisitor(Info.ASM_VERSION) {
                private final boolean recordAccess = needToRecord && notEnum;
                private final Object2IntOpenHashMap<String> map = recordAccess ? new Object2IntOpenHashMap<>() : null;
                private boolean isMixin;

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(descriptor)) {
                        this.isMixin = true;
                        ObjectArrayList<String> list = superClassMap.computeIfAbsent(className, s -> new ObjectArrayList<>());
                        return new AnnotationVisitor(api) {
                            @Override
                            public AnnotationVisitor visitArray(String name) {
                                return switch (name) {
                                    case "value" -> new AnnotationVisitor(api) {
                                        @Override
                                        public void visit(String name, Object value) {
                                            if (value instanceof Type t && t.getSort() == Type.OBJECT) {
                                                list.add(t.getInternalName());
                                            } else throw new IllegalArgumentException();
                                        }
                                    };
                                    case "targets" -> new AnnotationVisitor(api) {
                                        @Override
                                        public void visit(String name, Object value) {
                                            if (value instanceof String s) {
                                                list.add(refMap.getOrDefault(className, Object2ObjectMaps.emptyMap())
                                                        .getOrDefault(s, s));
                                            } else throw new IllegalArgumentException();
                                        }
                                    };
                                    default -> null;
                                };
                            }

                            @Override
                            public void visit(String name, Object value) {
                                if ("remap".equals(name) && value instanceof Boolean b && !b) {
                                    dontRemap.put(className, ObjectSets.emptySet());
                                }
                            }
                        };
                    }
                    return null;
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    if(recordAccess && (access & Opcodes.ACC_PUBLIC) == 0) map.put(name, access);
                    return !isMixin || dontRemap.get(className) == ObjectSets.<String>emptySet() ? null : new FieldVisitor(api) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            return new AnnotationVisitor(api) {
                                @Override
                                public void visit(String name, Object value) {
                                    if ("remap".equals(name) && value instanceof Boolean b && !b) {
                                        dontRemap.computeIfAbsent(className, k -> new ObjectOpenHashSet<>())
                                                .add(name);
                                    }
                                }
                            };
                        }
                    };
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if(recordAccess && (access & Opcodes.ACC_PUBLIC) == 0) map.put(name.concat(descriptor), access);
                    return !isMixin || dontRemap.get(className) == ObjectSets.<String>emptySet() ? null : new MethodVisitor(api) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            return new AnnotationVisitor(api) {
                                @Override
                                public void visit(String name, Object value) {
                                    if ("remap".equals(name) && value instanceof Boolean b && !b) {
                                        dontRemap.computeIfAbsent(className, k -> new ObjectOpenHashSet<>())
                                                .add(name.concat(descriptor));
                                    }
                                }
                            };
                        }
                    };
                }

                @Override
                public void visitEnd() {
                    if(recordAccess && !map.isEmpty()) {
                        map.defaultReturnValue(Opcodes.ACC_PUBLIC);
                        synchronized(accessMap) {
                            accessMap.put(className, map);
                        }
                    }
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch(IOException e) {
            LOGGER.log(Level.WARNING, "Error when creating super class mapping", e);
        }
    }

    public ObjectList<String> getSuperNames(String name) {
        return superClassMap.get(name);
    }

    public int getAccessFlags(String className, String combinedMemberName) {
        Object2IntMap<String> map = accessMap.get(className);
        if(map == null) return Opcodes.ACC_PUBLIC;
        return map.getInt(combinedMemberName);
    }
}
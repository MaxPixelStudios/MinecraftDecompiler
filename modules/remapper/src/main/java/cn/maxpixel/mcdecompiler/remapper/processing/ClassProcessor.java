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

package cn.maxpixel.mcdecompiler.remapper.processing;

import cn.maxpixel.mcdecompiler.api.Constants;
import cn.maxpixel.mcdecompiler.api.Directories;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.remapper.DeobfuscationOptions;
import cn.maxpixel.mcdecompiler.remapper.Deobfuscator;
import cn.maxpixel.mcdecompiler.remapper.variable.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.jar.Manifest;

public final class ClassProcessor {
    private static final ObjectArrayList<Supplier<Process>> BEFORE = new ObjectArrayList<>();
    private static final ObjectArrayList<Supplier<Process>> AFTER = new ObjectArrayList<>();

    private final CoreProcess core = new CoreProcess();
    private final ObjectArrayList<Process> before;
    private final ObjectArrayList<Process> after;
    private final DeobfuscationOptions options;

    public ClassProcessor(DeobfuscationOptions options) {
        this.options = Objects.requireNonNull(options);
        this.before = BEFORE.stream().map(Supplier::get).collect(ObjectArrayList.toListWithExpectedSize(BEFORE.size()));
        this.after = AFTER.stream().map(Supplier::get).collect(ObjectArrayList.toListWithExpectedSize(AFTER.size()));
    }

    public static void addProcess(Process.Run run, Supplier<Process> process) {
        switch (Objects.requireNonNull(run)) {
            case BEFORE -> BEFORE.add(Objects.requireNonNull(process));
            case AFTER -> AFTER.add(Objects.requireNonNull(process));
        }
    }

    public void beforeCollectingExtraClassesInformation(Path input) throws IOException {
        core.beforeCollectingExtraClassesInformation(input);
        for (Process process : after) {
            process.beforeCollectingExtraClassesInformation(input);
        }
        for (Process process : before) {
            process.beforeCollectingExtraClassesInformation(input);
        }
    }

    public ClassVisitor getExtraClassesInformationVisitor(ExtraClassesInformation eci, ClassReader reader) {
        ClassVisitor cv = core.getExtraClassesInformationVisitor(eci, reader, null);
        for (Process process : after) {
            var cv1 = process.getExtraClassesInformationVisitor(eci, reader, cv);
            if (cv1 != null) cv = cv1;
        }
        for (Process process : before) {
            var cv1 = process.getExtraClassesInformationVisitor(eci, reader, cv);
            if (cv1 != null) cv = cv1;
        }
        return cv;
    }

    public void beforeRunning(ClassFileRemapper mappingRemapper, Path input) throws IOException {
        core.beforeRunning(options, mappingRemapper, input);
        for (Process process : after) {
            process.beforeRunning(options, mappingRemapper, input);
        }
        for (Process process : before) {
            process.beforeRunning(options, mappingRemapper, input);
        }
    }

    public void afterRunning(ClassFileRemapper mappingRemapper) throws IOException {
        core.afterRunning(options, mappingRemapper);
        for (Process process : after) {
            process.afterRunning(options, mappingRemapper);
        }
        for (Process process : before) {
            process.afterRunning(options, mappingRemapper);
        }
    }

    public ClassVisitor getVisitor(ClassWriter writer, ClassReader reader, ClassFileRemapper mappingRemapper) {
        ClassVisitor cv = writer;
        for (Process process : after) {
            cv = process.getVisitor(options, reader, mappingRemapper, cv);
        }
        cv = core.getVisitor(options, reader, mappingRemapper, cv);
        for (Process process : before) {
            cv = process.getVisitor(options, reader, mappingRemapper, cv);
        }
        return cv;
    }

    private static class CoreProcess implements Process {
        private final ForgeFlowerAbstractParametersRecorder recorder = new ForgeFlowerAbstractParametersRecorder();
        private final Object2ObjectOpenHashMap<String, Map<String, String>> refMap = new Object2ObjectOpenHashMap<>();// TODO: move this to datamap
        public final Object2ObjectOpenHashMap<String, ObjectSet<String>> dontRemap = new Object2ObjectOpenHashMap<>();
        // TODO: Do we really need DataMaps?

        @Override
        public @NotNull String getName() {
            return "core";
        }

        @Override
        public void beforeCollectingExtraClassesInformation(Path input) throws IOException {
            try (InputStream is = Files.newInputStream(input.resolve("META-INF/MANIFEST.MF"))) {
                // TODO: Move this to extensions
                Optional.ofNullable(new Manifest(is).getMainAttributes().getValue("MixinConfigs"))
                        .map(input::resolve)
                        .flatMap(p -> {
                            try (var isr = new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8)) {
                                return Optional.of(JsonParser.parseReader(isr).getAsJsonObject()
                                        .get("refmap").getAsString());
                            } catch (Exception e) {// TODO: Log exceptions here
                                return Optional.empty();
                            }
                        })
                        .map(input::resolve)
                        .flatMap(p -> {
                            try (var isr = new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8)) {
                                return Optional.of(JsonParser.parseReader(isr).getAsJsonObject()
                                        .getAsJsonObject("mappings"));
                            } catch (Exception e) {// TODO: Log exceptions here
                                return Optional.empty();
                            }
                        })
                        .ifPresent(mappings -> {
                            for (Map.Entry<String, JsonElement> e1 : mappings.entrySet()) {
                                Object2ObjectOpenHashMap<String, String> mapping = new Object2ObjectOpenHashMap<>();
                                for (Map.Entry<String, JsonElement> e2 : e1.getValue().getAsJsonObject().entrySet()) {
                                    mapping.put(e2.getKey(), e2.getValue().getAsString());
                                }
                                refMap.put(e1.getKey(), mapping);
                            }
                        });
            } catch (IOException ignored) {
            }
        }

        @Override
        public @Nullable ClassVisitor getExtraClassesInformationVisitor(ExtraClassesInformation eci, ClassReader reader, ClassVisitor parent) {
            return new ClassVisitor(Deobfuscator.ASM_VERSION, parent) {
                private static final ObjectSet<String> EMPTY = ObjectSets.emptySet();
                private String className;
                private boolean isMixin;

                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    this.className = name;
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(descriptor)) {
                        this.isMixin = true;
                        return new AnnotationVisitor(api, super.visitAnnotation(descriptor, visible)) {
                            private final ObjectArrayList<String> list = eci.superClassMap.computeIfAbsent(className,
                                    s -> new ObjectArrayList<>());

                            @Override
                            public AnnotationVisitor visitArray(String name) {
                                var p = super.visitArray(name);
                                return switch (name) {
                                    case "value" -> new AnnotationVisitor(api, p) {
                                        @Override
                                        public void visit(String name, Object value) {
                                            if (value instanceof Type t && t.getSort() == Type.OBJECT) {
                                                list.add(t.getInternalName());
                                            } else throw new IllegalArgumentException();
                                            super.visit(name, value);
                                        }
                                    };
                                    case "targets" -> new AnnotationVisitor(api, p) {
                                        @Override
                                        public void visit(String name, Object value) {
                                            if (value instanceof String s) {
                                                list.add(refMap.getOrDefault(className, Object2ObjectMaps.emptyMap())
                                                        .getOrDefault(s, s));
                                            } else throw new IllegalArgumentException();
                                            super.visit(name, value);
                                        }
                                    };
                                    default -> p;
                                };
                            }

                            @Override
                            public void visit(String name, Object value) {
                                if ("remap".equals(name) && value instanceof Boolean b && !b) {
                                    dontRemap.put(className, EMPTY);
                                }
                                super.visit(name, value);
                            }
                        };
                    }
                    return super.visitAnnotation(descriptor, visible);
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    var p = super.visitField(access, name, descriptor, signature, value);
                    return !isMixin || dontRemap.get(className) == EMPTY ? p : new FieldVisitor(api, p) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            return new AnnotationVisitor(api, super.visitAnnotation(descriptor, visible)) {
                                @Override
                                public void visit(String name, Object value) {
                                    if ("remap".equals(name) && value instanceof Boolean b && !b) {
                                        dontRemap.computeIfAbsent(className, k -> new ObjectOpenHashSet<>())
                                                .add(name);
                                    }
                                    super.visit(name, value);
                                }
                            };
                        }
                    };
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    var p = super.visitMethod(access, name, descriptor, signature, exceptions);
                    return !isMixin || dontRemap.get(className) == EMPTY ? p : new MethodVisitor(api, p) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            return new AnnotationVisitor(api, super.visitAnnotation(descriptor, visible)) {
                                @Override
                                public void visit(String name, Object value) {
                                    if ("remap".equals(name) && value instanceof Boolean b && !b) {
                                        dontRemap.computeIfAbsent(className, k -> new ObjectOpenHashSet<>())
                                                .add(name.concat(descriptor));
                                    }
                                    super.visit(name, value);
                                }
                            };
                        }
                    };
                }
            };
        }

        @Override
        public void beforeRunning(DeobfuscationOptions options, ClassFileRemapper mappingRemapper, Path input) {
            if (options.rvn()) recorder.startRecord();
        }

        @Override
        public void afterRunning(DeobfuscationOptions options, ClassFileRemapper mappingRemapper) throws IOException {
            if (options.rvn()) recorder.endRecord(Directories.TEMP_DIR.resolve(Constants.FERNFLOWER_ABSTRACT_PARAMETER_NAMES));
        }

        @Override
        public @NotNull ClassVisitor getVisitor(DeobfuscationOptions options, ClassReader reader, ClassFileRemapper cfr, ClassVisitor parent) {
            String className = reader.getClassName();
            int access = reader.getAccess();
            ClassVisitor cv = parent;
            VariableNameHandler handler = new VariableNameHandler();
            if (cfr.remapper instanceof ClassifiedMappingRemapper cmr) {
                ClassMapping<? extends Mapping> cm = cmr.getClassMappingUnmapped(className);
                if (cm != null) {
                    MappingVariableNameProvider provider = new MappingVariableNameProvider(cm, cmr);
                    if (provider.omitThis()) handler.setOmitThis();
                    handler.addProvider(provider);
                }
            }
            if ((access & Opcodes.ACC_RECORD) != 0) {
                RecordNameRemapper r = new RecordNameRemapper(cv);
                cv = r;
                handler.addProvider(r);
            }
            cv = new VariableNameProcessor(cv, recorder, handler, cfr.map(className), options.rvn());
            cv = new ClassRemapper(cv, cfr);
            cv = new IndyRemapper(cv, cfr);
            ExtraClassesInformation eci = cfr.eci;
            if (dontRemap.containsKey(className)) {
                ObjectSet<String> skipped = dontRemap.get(className);
                if (!skipped.isEmpty()) {
                    cv = new MixinClassRemapper(cv, cfr.remapper, eci, refMap, skipped, className);
                }
            } else {
                cv = new MixinClassRemapper(cv, cfr.remapper, eci, refMap, ObjectSets.emptySet(), className);
            }
            return new RuntimeParameterAnnotationFixer(cv, className, access);
        }
    }
}
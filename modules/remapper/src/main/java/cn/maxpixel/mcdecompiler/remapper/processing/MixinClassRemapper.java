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

import cn.maxpixel.mcdecompiler.mapping.remapper.MappingRemapper;
import cn.maxpixel.mcdecompiler.remapper.Deobfuscator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MixinClassRemapper extends ClassVisitor {
    private final MappingRemapper remapper;
    private final ExtraClassesInformation info;

    private final Map<String, Map<String, String>> refMap;
    private final ObjectSet<String> skipped;
    private final String className;
    private boolean mixin;

    public MixinClassRemapper(ClassVisitor classVisitor, MappingRemapper remapper, ExtraClassesInformation info,
                              Map<String, Map<String, String>> refMap, ObjectSet<String> skipped, String className) {
        super(Deobfuscator.ASM_VERSION, classVisitor);
        this.remapper = remapper;
        this.info = info;
        this.refMap = refMap;
        this.skipped = skipped;
        this.className = className;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(descriptor)) {
            this.mixin = true;
            return new AnnotationVisitor(api, super.visitAnnotation(descriptor, visible)) {
                @Override
                public AnnotationVisitor visitArray(String name) {
                    if ("targets".equals(name)) {
                        return new AnnotationVisitor(api, super.visitArray(name)) {
                            @Override
                            public void visit(String name, Object value) {
                                if (value instanceof String s) {
                                    super.visit(name, remapper.mapClassOrDefault(
                                            refMap.getOrDefault(className, Object2ObjectMaps.emptyMap())
                                                    .getOrDefault(s, s)
                                    ));
                                } else throw new IllegalArgumentException();
                            }
                        };
                    }
                    return super.visitArray(name);
                }
            };
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (mixin && !skipped.contains(name.concat(descriptor))) {
            return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    AnnotationVisitor av = super.visitAnnotation(descriptor, visible);
                    return switch (descriptor) {
                        case "Lorg/spongepowered/asm/mixin/injection/Redirect;",
                             "Lorg/spongepowered/asm/mixin/injection/Inject;",
                             "Lorg/spongepowered/asm/mixin/injection/ModifyArg;",
                             "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;",
                            "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;",
                             "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;" -> new MixinMethodRemapper(api, av);
                        default -> av;
                    };
                }
            };
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    private String remapMethodSelector(String s) {// TODO: full-support of target selector
        if (s.charAt(s.length() - 1) != '/' && s.charAt(0) != '@' && !s.contains("<")) {
            String s1 = refMap.getOrDefault(className, Object2ObjectMaps.emptyMap())
                    .getOrDefault(s, s);
            MixinTargetSelector selector = MixinTargetSelector.parse(s1);
            return Optional.ofNullable(selector.owner())
                    .map(Type::getInternalName)
                    .map(List::of)
                    .or(() -> Optional.ofNullable(info.getSuperNames(className)))
                    .filter(owners -> selector.name() != null)
                    .flatMap(owners -> selector.field() ? owners.parallelStream()
                            .map(owner -> remapper.mapField(owner, selector.name()))
                            .filter(Objects::nonNull).reduce((l, r) -> {
                                throw new IllegalArgumentException("Multiple matches found");
                            }) : owners.parallelStream()
                            .map(owner -> remapper.mapMethod(owner, selector.name(), selector.descriptor()))// FIXME: potentially incorrect remapping
                            .filter(Objects::nonNull).findAny()
                    ).map(mapped -> selector.remap(remapper, mapped).toSelectorString())
                    .orElse(s1);
        } else return s;
    }

    private class MixinMethodRemapper extends AnnotationVisitor {
        private MixinMethodRemapper(int api, AnnotationVisitor annotationVisitor) {
            super(api, annotationVisitor);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            if ("at".equals(name)) {
                return new AnnotationVisitor(api, super.visitAnnotation(name, descriptor)) {
                    @Override
                    public void visit(String name, Object value) {
                        if ("target".equals(name)) {
                            super.visit(name, remapMethodSelector(((String) value)));
                        } else super.visit(name, value);
                    }
                };
            }
            return super.visitAnnotation(name, descriptor);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            if ("method".equals(name)) {
                return new MethodTargetSelectorArrayRemapper(api, super.visitArray(name));
            }
            return super.visitArray(name);
        }
    }

    private class MethodTargetSelectorArrayRemapper extends AnnotationVisitor {
        private MethodTargetSelectorArrayRemapper(int api, AnnotationVisitor annotationVisitor) {
            super(api, annotationVisitor);
        }

        @Override
        public void visit(String name, Object value) {
            if (value instanceof String s) {
                super.visit(name, remapMethodSelector(s));
            } else throw new IllegalArgumentException();
        }
    }
}
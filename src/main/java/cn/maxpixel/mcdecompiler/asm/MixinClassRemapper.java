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
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.util.MixinTargetSelector;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MixinClassRemapper extends ClassVisitor {
    private final ClassifiedMappingRemapper remapper;
    private final ExtraClassesInformation info;

    private final Map<String, Map<String, String>> refMap;
    private final ObjectSet<String> skipped;
    private final String className;
    private boolean mixin;

    public MixinClassRemapper(ClassVisitor classVisitor, ClassifiedMappingRemapper remapper, ExtraClassesInformation info,
                              Map<String, Map<String, String>> refMap, ObjectSet<String> skipped, String className) {
        super(Info.ASM_VERSION, classVisitor);
        this.remapper = remapper;
        this.info = info;
        this.refMap = refMap;
        this.skipped = skipped;
        this.className = className;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
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
                                    super.visit(name, remapper.map(
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
                    .flatMap(owners -> selector.field() ? Optional.of(owners.parallelStream()
                            .map(remapper::getClassByUnmappedName)
                            .flatMap(cm -> cm.getFields().stream())
                            .collect(Collectors.toMap(
                                    cm -> cm.unmappedName,
                                    Function.identity(),
                                    Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new))
                            ).map(map -> map.get(selector.name())): owners.parallelStream()
                            .map(remapper::getClassByUnmappedName)
                            .filter(Objects::nonNull)
                            .map(ClassMapping::getMethods)
                            .flatMap(ObjectList::stream)
                            .filter(mm -> {// FIXME: potentially incorrect remapping
                                if (!Objects.equals(selector.name(), mm.unmappedName)) return false;
                                if (selector.descriptor() == null) return true;
                                String descriptor;
                                if (mm.hasComponent(Descriptor.class))
                                    descriptor = mm.getComponent(Descriptor.class).unmappedDescriptor;
                                else if (mm.hasComponent(Descriptor.Mapped.class))
                                    descriptor = remapper.getUnmappedDescByMappedDesc(
                                            mm.getComponent(Descriptor.Mapped.class).mappedDescriptor);
                                else return false;
                                return descriptor.equals(selector.descriptor());
                            }).findAny()
                    ).map(mm -> selector.remap(remapper, mm.mappedName).toSelectorString())
                    .orElse(s1);
        } else return s;
    }

    private class MixinMethodRemapper extends AnnotationVisitor {
        private MixinMethodRemapper(int api, AnnotationVisitor annotationVisitor) {
            super(api, annotationVisitor);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            if (name.equals("at")) {
                return new AnnotationVisitor(api, super.visitAnnotation(name, descriptor)) {
                    @Override
                    public void visit(String name, Object value) {
                        if (name.equals("target")) {
                            super.visit(name, remapMethodSelector(((String) value)));
                        } else super.visit(name, value);
                    }
                };
            }
            return super.visitAnnotation(name, descriptor);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            if (name.equals("method")) {
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
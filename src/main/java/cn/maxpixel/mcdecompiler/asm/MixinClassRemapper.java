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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.objectweb.asm.*;

public class MixinClassRemapper extends ClassVisitor {
    private final ClassifiedMappingRemapper mappingRemapper;
    private boolean isMixin;
    private ObjectList<String> targetClasses = ObjectLists.emptyList();

    public MixinClassRemapper(ClassVisitor classVisitor, ClassifiedMappingRemapper mappingRemapper) {
        super(Info.ASM_VERSION, classVisitor);
        this.mappingRemapper = mappingRemapper;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitAnnotation(descriptor, visible);
        if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(descriptor)) {
            this.isMixin = true;
            this.targetClasses = new ObjectArrayList<>();
            return new AnnotationVisitor(api, av) {
                @Override
                public AnnotationVisitor visitArray(String name) {
                    AnnotationVisitor v = super.visitArray(name);
                    if (name.equals("value")) {
                        return new AnnotationVisitor(api, v) {
                            @Override
                            public void visit(String name, Object value) {
                                if (value instanceof Type t && t.getSort() == Type.OBJECT) {
                                    targetClasses.add(t.getInternalName());
                                }
                                super.visit(name, value);
                            }
                        };
                    }
                    return v;
                }
            };
        }
        return av;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (isMixin) {// FIXME: Cannot determine whether this is a shadow field or not
            for (String targetClass : targetClasses) {
                String result = mappingRemapper.mapFieldName(targetClass, name, descriptor);
                if (result != name) {// Use != because name will be returned if mapped name is not found
                    return super.visitField(access, result, descriptor, signature, value);
                }
            }
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (isMixin) {
            for (String targetClass : targetClasses) {
                String result = mappingRemapper.mapMethodName(targetClass, name, descriptor);
                if (result != name) {
                    return super.visitMethod(access, result, descriptor, signature, exceptions);
                }
            }
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
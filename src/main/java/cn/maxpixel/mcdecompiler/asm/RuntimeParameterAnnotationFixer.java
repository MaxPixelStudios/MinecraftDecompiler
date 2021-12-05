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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;

// Visitor version of https://github.com/ModCoderPack/MCInjector/blob/master/src/main/java/de/oceanlabs/mcp/mcinjector/adaptors/ParameterAnnotationFixer.java
public class RuntimeParameterAnnotationFixer extends ClassVisitor {
    private static final Logger LOGGER = LogManager.getLogger("Runtime(In)visibleParameterAnnotations Attribute Fixer");
    private int removeCount;// = isEnum ? 2 : 1
    private String className;
    private String toProcess;

    public RuntimeParameterAnnotationFixer(ClassVisitor classVisitor) {
        super(Info.ASM_VERSION, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        if((access & Opcodes.ACC_ENUM) != 0) {
            this.removeCount = 2;
            this.toProcess = "(Ljava/lang/String;I";
            LOGGER.debug("Fixing class {} because it is an enum", name);
        }
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if(toProcess == null && className.equals(name) && (access & (Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE)) == 0 && innerName != null) {
            this.removeCount = 1;
            this.toProcess = '(' + Type.getObjectType(outerName).getDescriptor();
            LOGGER.debug("Fixing class {} because it is an inner class of {}", name, outerName);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if(toProcess != null && name.equals("<init>") && descriptor.startsWith(toProcess)) {
            int params = Type.getArgumentTypes(descriptor).length;
            return new MethodVisitor(Info.ASM_VERSION, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                private boolean processVisible;
                private boolean processInvisible;
                @Override
                public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
                    if(params == parameterCount) {
                        LOGGER.trace("Found {} extra {}, try removing...", removeCount, visible ?
                                "RuntimeVisibleParameterAnnotations" : "RuntimeInvisibleParameterAnnotations");
                        if(visible) processVisible = true;
                        else processInvisible = true;
                        super.visitAnnotableParameterCount(parameterCount - removeCount, visible);
                    } else super.visitAnnotableParameterCount(parameterCount, visible);
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                    if(processVisible && visible) {
                        if(parameter >= removeCount) {
                            return super.visitParameterAnnotation(parameter - removeCount, descriptor, true);
                        } else {
                            LOGGER.trace("Dropped an annotation(descriptor={}) on synthetic param {}", descriptor, parameter);
                            return null;
                        }
                    }
                    if(processInvisible && !visible) {
                        if(parameter >= removeCount) {
                            return super.visitParameterAnnotation(parameter - removeCount, descriptor, false);
                        } else {
                            LOGGER.trace("Dropped an annotation(descriptor={}) on synthetic param {}", descriptor, parameter);
                            return null;
                        }
                    }
                    return super.visitParameterAnnotation(parameter, descriptor, visible);
                }
            };
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
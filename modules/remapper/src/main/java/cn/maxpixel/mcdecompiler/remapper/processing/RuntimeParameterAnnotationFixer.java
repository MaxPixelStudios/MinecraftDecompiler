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

import cn.maxpixel.mcdecompiler.remapper.Deobfuscator;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import org.intellij.lang.annotations.Subst;
import org.objectweb.asm.*;

// Visitor version of https://github.com/MinecraftForge/ForgeAutoRenamingTool/blob/master/src/main/java/net/minecraftforge/fart/internal/ParameterAnnotationFixer.java
public class RuntimeParameterAnnotationFixer extends ClassVisitor {
    private static final Logger LOGGER = LogManager.getLogger();
    private int removeCount;// = isEnum ? 2 : 1
    private final String className;
    private String toProcess;

    public RuntimeParameterAnnotationFixer(ClassVisitor classVisitor, String className, int access) {
        super(Deobfuscator.ASM_VERSION, classVisitor);
        this.className = className;
        if ((access & Opcodes.ACC_ENUM) != 0) {
            this.removeCount = 2;
            this.toProcess = "(Ljava/lang/String;I";
            LOGGER.debug("Fixing class {} because it is an enum", className);
        }
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (toProcess == null && name.equals(className) && (access & (Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE)) == 0 && innerName != null) {
            if (outerName == null) {
                int i = className.lastIndexOf('$');
                if (i != -1) {
                    this.removeCount = 1;
                    String s = className.substring(0, i);
                    this.toProcess = "(L" + s + ';';
                    LOGGER.debug("Fixing class {} as its name appears to be an inner class of {}", name, s);
                }
            } else {
                this.removeCount = 1;
                this.toProcess = "(L" + outerName + ';';
                LOGGER.debug("Fixing class {} as its an inner class of {}", name, outerName);
            }
        }
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, @Subst("(Ljava/lang/String;I)V") String descriptor, String signature, String[] exceptions) {
        if (toProcess != null && "<init>".equals(name) && descriptor.startsWith(toProcess)) {
            return new MethodVisitor(Deobfuscator.ASM_VERSION, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                private final int params = Type.getArgumentCount(descriptor);
                private boolean processVisible;
                private boolean processInvisible;
                @Override
                public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
                    if (params == parameterCount) {
                        LOGGER.trace("Found {} extra Runtime{}isibleParameterAnnotations, try removing...",
                                removeCount, visible ? "V" : "Inv");
                        if (visible) processVisible = true;
                        else processInvisible = true;
                        super.visitAnnotableParameterCount(parameterCount - removeCount, visible);
                    } else super.visitAnnotableParameterCount(parameterCount, visible);
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                    if (processVisible && visible) {
                        if (parameter >= removeCount) {
                            return super.visitParameterAnnotation(parameter - removeCount, descriptor, true);
                        } else {
                            LOGGER.trace("Dropped an annotation(descriptor={}) on synthetic param {}", descriptor, parameter);
                            return null;
                        }
                    }
                    if (processInvisible && !visible) {
                        if (parameter >= removeCount) {
                            return super.visitParameterAnnotation(parameter - removeCount, descriptor, false);
                        } else {
                            LOGGER.trace("Dropped an annotation(descriptor={}) on synthetic param {}",
                                    descriptor, parameter);
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
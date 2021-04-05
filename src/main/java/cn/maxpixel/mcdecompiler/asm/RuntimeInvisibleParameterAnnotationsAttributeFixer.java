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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;
import java.util.List;

// Similar to MCInjector's ParameterAnnotationFixer(https://github.com/ModCoderPack/MCInjector/blob/master/src/main/java/de/oceanlabs/mcp/mcinjector/adaptors/ParameterAnnotationFixer.java)
public class RuntimeInvisibleParameterAnnotationsAttributeFixer extends ClassNode {
    public RuntimeInvisibleParameterAnnotationsAttributeFixer() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visitEnd() {
        String toProcess = innerClasses.stream().filter(icn -> icn.name.equals(name)).findFirst()
                .filter(icn -> (icn.access & (Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE)) == 0 && icn.innerName != null)
                .map(icn -> '(' + Type.getObjectType(outerClass).getDescriptor())
                .orElse((access & Opcodes.ACC_ENUM) != 0 ? "(Ljava/lang/String;I" : null);
        if(toProcess != null) {
            methods.stream().filter(mn -> mn.name.equals("<init>") && mn.desc.startsWith(toProcess)).forEach(mn -> {
                int params = Type.getArgumentTypes(mn.desc).length;
                int synthetics = toProcess.equals("(Ljava/lang/String;I") ? 2 : 1;
                mn.visibleParameterAnnotations = process(params, synthetics, mn.visibleParameterAnnotations);
                mn.invisibleParameterAnnotations = process(params, synthetics, mn.invisibleParameterAnnotations);
                if(mn.visibleParameterAnnotations != null) mn.visibleAnnotableParameterCount = mn.visibleParameterAnnotations.length;
                if(mn.invisibleParameterAnnotations != null) mn.invisibleAnnotableParameterCount = mn.invisibleParameterAnnotations.length;
            });
        }
    }

    private List<AnnotationNode>[] process(int params, int synthetics, List<AnnotationNode>[] annotations) {
        if(annotations == null) return null;
        int annotationsCount = annotations.length;
        if(params == annotationsCount) return Arrays.copyOfRange(annotations, synthetics, annotationsCount);
        return annotations;
    }
}
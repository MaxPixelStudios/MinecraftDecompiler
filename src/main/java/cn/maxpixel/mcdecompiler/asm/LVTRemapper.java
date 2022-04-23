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
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.util.Logging;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LVTRemapper extends ClassVisitor {
    private static final Logger LOGGER = Logging.getLogger("LVT Remapper");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("o");

    private final Matcher placeholderMatcher = PLACEHOLDER_PATTERN.matcher("");
    private final Object2ObjectOpenHashMap<String, NamespacedMapping> methodByMapped = new Object2ObjectOpenHashMap<>();

    public LVTRemapper(@NotNull ClassVisitor classVisitor, @NotNull ClassMapping<NamespacedMapping> mapping,
                       @NotNull ClassifiedMappingRemapper remapper) {
        super(Info.ASM_VERSION, classVisitor);
        mapping.getMethods().parallelStream().forEach(m -> {
            Descriptor.Namespaced desc = m.getComponent(Descriptor.Namespaced.class);
            if(!desc.getDescriptorNamespace().equals(mapping.mapping.getUnmappedNamespace()))
                throw new IllegalArgumentException("Descriptor namespace mismatch");
            m.setMappedNamespace(mapping.mapping.getMappedNamespace());
            synchronized(methodByMapped) {
                if(methodByMapped.put(m.getMappedName().concat(remapper.getMappedDescByUnmappedDesc(
                        desc.getDescriptorNamespace())), m) != null) {
                    throw new IllegalArgumentException("Method duplicated");
                }
            }
        });
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        NamespacedMapping methodMapping = methodByMapped.get(name.concat(descriptor));
        if(methodMapping != null) {
            // tsrgv2 mapping always omits `this`
            boolean omitThis = methodMapping.hasComponent(StaticIdentifiable.class);
            boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
            LocalVariableTable.Namespaced lvt = methodMapping.getComponent(LocalVariableTable.Namespaced.class);
            if(lvt != null) return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                private final Optional<VariableNameGenerator.Skippable> skippable = mv instanceof VariableNameGenerator.Skippable s ?
                        Optional.of(s) : Optional.empty();

                @Override
                public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                    if(index != 0 || isStatic) {
                        String s = lvt.getMappedLocalVariableName(!isStatic && omitThis ? index - 1 : index);
                        if(s != null && !s.isBlank() && !placeholderMatcher.reset(s).matches()) {
                            skippable.ifPresent(VariableNameGenerator.Skippable::skip);
                            super.visitLocalVariable(s, descriptor, signature, start, end, index);
                            return;
                        }
                    }
                    super.visitLocalVariable(name, descriptor, signature, start, end, index);
                }
            };
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
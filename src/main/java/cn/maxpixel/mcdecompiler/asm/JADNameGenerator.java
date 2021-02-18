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

import cn.maxpixel.mcdecompiler.util.IntLocal;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.objectweb.asm.*;

import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

public class JADNameGenerator extends ClassVisitor {
    private static final Object2ObjectOpenHashMap<String, Holder> PREDEF = new Object2ObjectOpenHashMap<>();
    static {
        PREDEF.put("int",     new Holder(0, true,  "i", "j", "k", "l"));
        PREDEF.put("byte",    new Holder(0, false, "b"       ));
        PREDEF.put("char",    new Holder(0, false, "c"       ));
        PREDEF.put("short",   new Holder(1, false, "short"   ));
        PREDEF.put("boolean", new Holder(0, true,  "flag"    ));
        PREDEF.put("double",  new Holder(0, false, "d"       ));
        PREDEF.put("float",   new Holder(0, true,  "f"       ));
        PREDEF.put("File",    new Holder(1, true,  "file"    ));
        PREDEF.put("String",  new Holder(0, true,  "s"       ));
        PREDEF.put("Class",   new Holder(0, true,  "oclass"  ));
        PREDEF.put("Long",    new Holder(0, true,  "olong"   ));
        PREDEF.put("Byte",    new Holder(0, true,  "obyte"   ));
        PREDEF.put("Short",   new Holder(0, true,  "oshort"  ));
        PREDEF.put("Boolean", new Holder(0, true,  "obool"   ));
        PREDEF.put("Package", new Holder(0, true,  "opackage"));
        PREDEF.put("Enum",    new Holder(0, true,  "oenum"   ));
        PREDEF.put("Void",    new Holder(0, true,  "ovoid"   ));
    }
    public JADNameGenerator(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new LVTRenamer(access, super.visitMethod(access, name, descriptor, signature, exceptions));
    }
    public static class LVTRenamer extends MethodVisitor {
        private final Object2IntOpenHashMap<String> vars = new Object2IntOpenHashMap<>();
        private final Object2ObjectOpenHashMap<Holder, UUID> uuids = new Object2ObjectOpenHashMap<>();
        private final int access;
        public LVTRenamer(int access, MethodVisitor methodVisitor) {
            super(Opcodes.ASM9, methodVisitor);
            this.access = access;
        }
        private String getVarName(Type type) {
            boolean isArray = false;
            if(type.getSort() == Type.ARRAY) {
                type = type.getElementType();
                isArray = true;
            }
            String varBaseName = type.getClassName();
            if(type.getSort() == Type.OBJECT) varBaseName = varBaseName.substring(varBaseName.lastIndexOf('.') + 1);
            Holder holder = isArray ? null : PREDEF.get(varBaseName.equals("long") ? "int" : varBaseName);
            varBaseName = (isArray ? "a" : "") + varBaseName.replace('$','_').toLowerCase(Locale.ENGLISH);
            if(holder != null) {
                if(holder.names.size() == 1) {
                    varBaseName = holder.names.get(0);
                    int count = vars.getOrDefault(varBaseName, holder.id);
                    vars.put(varBaseName, count + 1);
                    return varBaseName + (count == 0 && holder.skip_zero ? "" : count);
                } else {
                    UUID identifier = uuids.computeIfAbsent(holder, h -> h.tempId.requestNew());
                    for(;;) {
                        for(int i = 0; i < holder.names.size(); ++i) {
                            varBaseName = holder.names.get(i);
                            if(!vars.containsKey(varBaseName)) {
                                int j = holder.tempId.get(identifier);
                                vars.put(varBaseName, j);
                                return varBaseName + (j == 0 && holder.skip_zero ? "" : j);
                            }
                        }
                        holder.tempId.increment(identifier);
                        for(int i = 0; i < holder.names.size(); ++i) vars.removeInt(holder.names.get(i));
                    }
                }
            } else {
                int count = vars.getInt(varBaseName);
                vars.put(varBaseName, count + 1);
                return varBaseName + (count > 0 ? count : "");
            }
        }
        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            if(name.equals("this") && index == 0 && (access & Opcodes.ACC_STATIC) == 0)
                super.visitLocalVariable(name, descriptor, signature, start, end, index);
            else super.visitLocalVariable(getVarName(Type.getType(descriptor)), descriptor, signature, start, end, index);
        }
        @Override
        public void visitEnd() {
            uuids.forEach((holder, uuid) -> holder.tempId.delete(uuid));
        }
    }
    private static class Holder {
        public final int id;
        public final IntLocal tempId;
        public final boolean skip_zero;
        public final ObjectArrayList<String> names = new ObjectArrayList<>();

        public Holder(int t1, boolean skip_zero, String... names) {
            this.id = t1;
            this.tempId = new IntLocal(this.id);
            this.skip_zero = skip_zero;
            Collections.addAll(this.names, names);
        }
    }
}
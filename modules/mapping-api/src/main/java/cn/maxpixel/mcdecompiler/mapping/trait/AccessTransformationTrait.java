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

package cn.maxpixel.mcdecompiler.mapping.trait;

import cn.maxpixel.mcdecompiler.mapping.NameGetter;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Objects;

/**
 * A trait that stores access flag transformation data
 */
public class AccessTransformationTrait implements MappingTrait {
    public final Object2IntOpenHashMap<String> classMap = new Object2IntOpenHashMap<>();
    public final Object2IntOpenHashMap<Member> fieldMap = new Object2IntOpenHashMap<>();
    public final Object2IntOpenHashMap<Member> methodMap = new Object2IntOpenHashMap<>();

    @Override
    public String getName() {
        return "access-transformation";
    }

    public Object2IntOpenHashMap<String> getClassMap() {
        return classMap;
    }

    public Object2IntOpenHashMap<Member> getFieldMap() {
        return fieldMap;
    }

    public Object2IntOpenHashMap<Member> getMethodMap() {
        return methodMap;
    }

    public void addClass(String name, int flag) {
        classMap.mergeInt(name, flag, (a, b) -> a | b);
    }

    public void addField(String owner, String name, int flag) {
        fieldMap.mergeInt(new Member(owner, name), flag, (a, b) -> a | b);
    }

    public void addField(String owner, String name, String descriptor, int flag) {
        fieldMap.mergeInt(new Member(owner, name, descriptor), flag, (a, b) -> a | b);
    }

    public void addMethod(String owner, String name, String descriptor, int flag) {
        methodMap.mergeInt(new Member(owner, name, descriptor), flag, (a, b) -> a | b);
    }

    public record Member(String owner, String name, String descriptor) implements NameGetter {
        public Member(String owner, String name) {
            this(name, owner, null);
        }

        @Override
        public String getUnmappedName() {
            return name;
        }

        @Override
        public String getMappedName() {
            return name;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccessTransformationTrait that)) return false;
        return classMap.equals(that.classMap) && fieldMap.equals(that.fieldMap) && methodMap.equals(that.methodMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classMap, fieldMap, methodMap);
    }
}
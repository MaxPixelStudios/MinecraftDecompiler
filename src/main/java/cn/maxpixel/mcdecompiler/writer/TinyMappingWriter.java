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

package cn.maxpixel.mcdecompiler.writer;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.components.Documented;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMethodMapping;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;

import java.util.Objects;
import java.util.StringJoiner;

public class TinyMappingWriter extends AbstractMappingWriter {
    public final int version;
    private final ObjectImmutableList<String> namespaces;

    public TinyMappingWriter(int version, String... namespaces) {
        super();
        this.version = version;
        this.namespaces = new ObjectImmutableList<>(namespaces);
    }

    public TinyMappingWriter(ClassifiedMappingRemapper remapper, int version, String... namespaces) {
        super(remapper);
        this.version = version;
        this.namespaces = new ObjectImmutableList<>(namespaces);
    }

    private final TinyV1MappingGenerator V1_GENERATOR = new TinyV1MappingGenerator();
    private final TinyV2MappingGenerator V2_GENERATOR = new TinyV2MappingGenerator();

    @Override
    protected MappingGenerator getGenerator() {
        return switch(version) {
            case 1 -> V1_GENERATOR;
            case 2 -> V2_GENERATOR;
            default -> throw new UnsupportedOperationException("Unknown tiny mapping version");
        };
    }

    @Override
    protected boolean needLock() {
        return switch(version) {
            case 1 -> false;
            case 2 -> true;
            default -> throw new UnsupportedOperationException("Unknown tiny mapping version");
        };
    }

    @Override
    protected String getHeader() {
        return switch(version) {
            case 1 -> {
                StringJoiner joiner = new StringJoiner("\t");
                joiner.add("v1");
                namespaces.forEach(joiner::add);
                yield joiner.toString();
            }
            case 2 -> {
                StringJoiner joiner = new StringJoiner("\t");
                joiner.add("tiny");
                joiner.add("2");
                joiner.add("0");
                namespaces.forEach(joiner::add);
                yield joiner.toString();
            }
            default -> throw new UnsupportedOperationException("Unknown tiny mapping version");
        };
    }

    private class TinyV1MappingGenerator implements NamespacedMappingGenerator {
        @Override
        public String generateClass(NamespacedClassMapping mapping) {
            StringJoiner joiner = new StringJoiner("\t");
            joiner.add("CLASS");
            namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(mapping.getName(namespace),
                    () -> "The provided mapping doesn't have expected namespace " + namespace)));
            return joiner.toString();
        }

        @Override
        public String generateMethod(NamespacedMethodMapping mapping) {
            StringJoiner joiner = new StringJoiner("\t");
            joiner.add("METHOD");
            joiner.add(mapping.getOwner().getName(namespaces.get(0)));
            joiner.add(mapping.getUnmappedDescriptor());
            namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(mapping.getName(namespace),
                    () -> "The provided mapping doesn't have expected namespace " + namespace)));
            return joiner.toString();
        }

        @Override
        public String generateField(NamespacedFieldMapping mapping) {
            StringJoiner joiner = new StringJoiner("\t");
            joiner.add("FIELD");
            joiner.add(mapping.getOwner().getName(namespaces.get(0)));
            if(!(mapping instanceof Descriptor desc)) throw new UnsupportedOperationException();
            joiner.add(desc.getUnmappedDescriptor());
            namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(mapping.getName(namespace),
                    () -> "The provided mapping doesn't have expected namespace " + namespace)));
            return joiner.toString();
        }
    }

    private class TinyV2MappingGenerator implements NamespacedMappingGenerator {
        @Override
        public String generateClass(NamespacedClassMapping mapping) {
            StringJoiner joiner = new StringJoiner("\t");
            joiner.add("c");
            namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(mapping.getName(namespace),
                    () -> "The provided mapping doesn't have expected namespace " + namespace)));
            if(mapping instanceof Documented doc) return joiner + "\n\tc\t" + doc.getDoc();
            return joiner.toString();
        }

        @Override
        public String generateMethod(NamespacedMethodMapping mapping) {
            StringJoiner joiner = new StringJoiner("\t", "\t", "");
            joiner.add("m");
            joiner.add(mapping.getUnmappedDescriptor());
            namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(mapping.getName(namespace),
                    () -> "The provided mapping doesn't have expected namespace " + namespace)));
            StringBuilder builder = new StringBuilder(joiner.toString());
            if(mapping instanceof Documented doc) builder.append("\n\t\tc\t").append(doc.getDoc());
            IntSet indexes = mapping.getLocalVariableIndexes();
            if(!indexes.isEmpty()) {
                indexes.forEach(index -> {
                    StringJoiner sj = new StringJoiner("\t", "\t\t", "");
                    sj.add("p");
                    sj.add(Integer.toString(index));
                    Object2ObjectMap<String, String> names = mapping.getLocalVariableNames(index);
                    namespaces.forEach(namespace -> sj.add(names.getOrDefault(namespace, "")));
                    builder.append('\n').append(sj);
                    if(mapping instanceof Documented.LocalVariable dlv) {
                        String doc = dlv.getLocalVariableDoc(index);
                        if(doc != null && !doc.isEmpty()) builder.append("\n\t\t\tc\t").append(doc);
                    }
                });
            }
            return builder.toString();
        }

        @Override
        public String generateField(NamespacedFieldMapping mapping) {
            StringJoiner joiner = new StringJoiner("\t", "\t", "");
            joiner.add("f");
            if(!(mapping instanceof Descriptor desc)) throw new UnsupportedOperationException();
            joiner.add(desc.getUnmappedDescriptor());
            namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(mapping.getName(namespace),
                    () -> "The provided mapping doesn't have expected namespace " + namespace)));
            if(mapping instanceof Documented doc) return joiner + "\n\tc\t" + doc.getDoc();
            return joiner.toString();
        }
    }
}
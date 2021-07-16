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

import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.AbstractMapping;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.tsrg.TsrgMethodMapping;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;

import java.util.Objects;
import java.util.StringJoiner;

public class TsrgMappingWriter extends AbstractMappingWriter {
    public final int version;
    private ObjectImmutableList<String> namespaces;

    public TsrgMappingWriter() {
        super();
        this.version = 1;
    }

    public TsrgMappingWriter(String... namespaces) {
        super();
        this.version = 2;
        this.namespaces = new ObjectImmutableList<>(namespaces);
    }

    public TsrgMappingWriter(MappingRemapper remapper) {
        super(remapper);
        this.version = 1;
    }

    public TsrgMappingWriter(MappingRemapper remapper, String... namespaces) {
        super(remapper);
        this.version = 2;
        this.namespaces = new ObjectImmutableList<>(namespaces);
    }

    private final TsrgV1MappingGenerator V1_GENERATOR = new TsrgV1MappingGenerator();
    private final TsrgV2MappingGenerator V2_GENERATOR = new TsrgV2MappingGenerator();

    @Override
    protected MappingGenerator getGenerator() {
        return switch(version) {
            case 1 -> V1_GENERATOR;
            case 2 -> V2_GENERATOR;
            default -> throw new UnsupportedOperationException("Unknown tsrg mapping version");
        };
    }

    @Override
    protected boolean needLock() {
        return true;
    }

    @Override
    protected String getHeader() {
        return switch(version) {
            case 1 -> super.getHeader();
            case 2 -> {
                StringJoiner joiner = new StringJoiner(" ");
                joiner.add("tsrg2");
                namespaces.forEach(joiner::add);
                yield joiner.toString();
            }
            default -> throw new UnsupportedOperationException("Unknown tsrg mapping version");
        };
    }

    private class TsrgV1MappingGenerator implements PairedMappingGenerator, PackageMappingGenerator {
        @Override
        public String generateClass(PairedClassMapping mapping) {
            return mapping.getUnmappedName() + ' ' + mapping.getMappedName();
        }

        @Override
        public String generateMethod(PairedMethodMapping mapping) {
            if(notDescImpl(mapping)) throw new UnsupportedOperationException();
            String unmappedDesc;
            if(mapping instanceof Descriptor desc) unmappedDesc = desc.getUnmappedDescriptor();
            else if(remapper != null) unmappedDesc = remapper.getUnmappedDescByMappedDesc(mapping.asMappedDescriptor().getMappedDescriptor());
            else throw new UnsupportedOperationException();
            return '\t' + mapping.getUnmappedName() + ' ' + unmappedDesc + ' ' + mapping.getMappedName();
        }

        @Override
        public String generateField(PairedFieldMapping mapping) {
            return '\t' + mapping.getUnmappedName() + ' ' + mapping.getMappedName();
        }

        @Override
        public String generatePackage(AbstractMapping mapping) {
            if(mapping instanceof PairedMapping paired && paired.getClass() == PairedMapping.class) {
                return paired.getUnmappedName() + '/' + ' ' + paired.getMappedName() + '/';
            } else throw new UnsupportedOperationException();
        }
    }

    private class TsrgV2MappingGenerator implements NamespacedMappingGenerator, PackageMappingGenerator {
        @Override
        public String generateClass(NamespacedClassMapping mapping) {
            StringJoiner joiner = new StringJoiner(" ");
            namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(mapping.getName(namespace),
                    () -> "The provided mapping doesn't have expected namespace " + namespace)));
            return joiner.toString();
        }

        @Override
        public String generateMethod(NamespacedMethodMapping mapping) {
            StringJoiner joiner = new StringJoiner(" ", "\t", "");
            namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(mapping.getName(namespace),
                    () -> "The provided mapping doesn't have expected namespace " + namespace)));
            StringBuilder builder = new StringBuilder(joiner.toString());
            builder.insert(builder.indexOf(" "), ' ' + mapping.getUnmappedDescriptor());
            if(mapping instanceof TsrgMethodMapping tsrg && tsrg.isStatic) builder.append("\n\t\tstatic");
            IntSet indexes = mapping.getLocalVariableIndexes();
            if(!indexes.isEmpty()) {
                indexes.forEach(index -> {
                    StringJoiner sj = new StringJoiner(" ");
                    sj.add(Integer.toString(index));
                    mapping.getLocalVariableNames(index).values().forEach(sj::add);
                    builder.append('\n').append('\t').append('\t').append(sj);
                });
            }
            return builder.toString();
        }

        @Override
        public String generateField(NamespacedFieldMapping mapping) {
            StringJoiner joiner = new StringJoiner(" ", "\t", "");
            namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(mapping.getName(namespace),
                    () -> "The provided mapping doesn't have expected namespace " + namespace)));
            String s = joiner.toString();
            if(mapping instanceof Descriptor desc) {
                int insert = s.indexOf(' ');
                s = s.substring(0, insert) + ' ' + desc.getUnmappedDescriptor() + s.substring(insert);
            }
            return s;
        }

        @Override
        public String generatePackage(AbstractMapping mapping) {
            if(mapping instanceof NamespacedMapping namespaced && namespaced.getClass() == NamespacedMapping.class) {
                StringJoiner joiner = new StringJoiner("/ ", "", "/");
                namespaces.forEach(namespace -> joiner.add(Objects.requireNonNull(namespaced.getName(namespace),
                        () -> "The provided mapping doesn't have expected namespace " + namespace)));
                return joiner.toString();
            } else throw new UnsupportedOperationException();
        }
    }
}
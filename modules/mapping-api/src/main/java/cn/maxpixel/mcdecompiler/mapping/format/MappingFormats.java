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

package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.MappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.generator.MappingGenerators;
import cn.maxpixel.mcdecompiler.mapping.processor.MappingProcessor;
import cn.maxpixel.mcdecompiler.mapping.processor.MappingProcessors;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public final class MappingFormats {
    public static final MappingFormat.Classified<PairedMapping> SRG = new MappingFormat.Classified<>() {
        @Override
        public @NotNull String getName() {
            return "srg";
        }

        @Override
        public @NotNull MappingProcessor.Classified<PairedMapping> getProcessor() {
            return MappingProcessors.SRG;
        }

        @Override
        public @NotNull MappingGenerator.Classified<PairedMapping> getGenerator() {
            return MappingGenerators.SRG;
        }
    };

    public static final MappingFormat.Classified<PairedMapping> CSRG = new MappingFormat.Classified<>() {
        @Override
        public @NotNull String getName() {
            return "csrg";
        }

        @Override
        public @NotNull MappingProcessor.Classified<PairedMapping> getProcessor() {
            return MappingProcessors.CSRG;
        }

        @Override
        public @NotNull MappingGenerator.Classified<PairedMapping> getGenerator() {
            return MappingGenerators.CSRG;
        }
    };

    public static final MappingFormat.Classified<PairedMapping> TSRG_V1 = new MappingFormat.Classified<>() {
        @Override
        public @NotNull String getName() {
            return "tsrg-v1";
        }

        @Override
        public @NotNull MappingProcessor.Classified<PairedMapping> getProcessor() {
            return MappingProcessors.TSRG_V1;
        }

        @Override
        public @NotNull MappingGenerator.Classified<PairedMapping> getGenerator() {
            return MappingGenerators.TSRG_V1;
        }
    };

    public static final MappingFormat.Classified<NamespacedMapping> TSRG_V2 = new MappingFormat.Classified<>() {
        @Override
        public @NotNull String getName() {
            return "tsrg-v2";
        }

        @Override
        public @NotNull MappingProcessor.Classified<NamespacedMapping> getProcessor() {
            return MappingProcessors.TSRG_V2;
        }

        @Override
        public @NotNull MappingGenerator.Classified<NamespacedMapping> getGenerator() {
            return MappingGenerators.TSRG_V2;
        }
    };

    public static final MappingFormat.Classified<PairedMapping> PROGUARD = new MappingFormat.Classified<>() {
        @Override
        public @NotNull String getName() {
            return "proguard";
        }

        @Override
        public @NotNull MappingProcessor.Classified<PairedMapping> getProcessor() {
            return MappingProcessors.PROGUARD;
        }

        @Override
        public @NotNull MappingGenerator.Classified<PairedMapping> getGenerator() {
            return MappingGenerators.PROGUARD;
        }
    };

    public static final MappingFormat.Classified<NamespacedMapping> TINY_V1 = new MappingFormat.Classified<>() {// TODO: support properties
        @Override
        public @NotNull String getName() {
            return "tiny-v1";
        }

        @Override
        public @NotNull MappingProcessor.Classified<NamespacedMapping> getProcessor() {
            return MappingProcessors.TINY_V1;
        }

        @Override
        public @NotNull MappingGenerator.Classified<NamespacedMapping> getGenerator() {
            return MappingGenerators.TINY_V1;
        }
    };

    public static final MappingFormat.Classified<NamespacedMapping> TINY_V2 = new MappingFormat.Classified<>() {
        @Override
        public @NotNull String getName() {
            return "tiny-v2";
        }

        @Override
        public char getCommentChar() {
            return '\0';
        }

        @Override
        public @NotNull MappingProcessor.Classified<NamespacedMapping> getProcessor() {
            return MappingProcessors.TINY_V2;
        }

        @Override
        public @NotNull MappingGenerator.Classified<NamespacedMapping> getGenerator() {
            return MappingGenerators.TINY_V2;
        }
    };
    
    public static final MappingFormat.Classified<PairedMapping> PDME = new MappingFormat.Classified<>() {
        @Override
        public @NotNull String getName() {
            return "pdme";
        }

        @Override
        public char getCommentChar() {
            return '#';
        }

        @Override
        public @NotNull MappingProcessor.Classified<PairedMapping> getProcessor() {
            return MappingProcessors.PDME;
        }

        @Override
        public @NotNull MappingGenerator.Classified<PairedMapping> getGenerator() {
            return MappingGenerators.PDME;
        }
    };
    

    private static final Object2ObjectOpenHashMap<String, MappingFormat<?, ?>> MAPPING_FORMATS = new Object2ObjectOpenHashMap<>();
    static {
        registerMappingFormat(SRG);
        registerMappingFormat(CSRG);
        registerMappingFormat(TSRG_V1);
        registerMappingFormat(TSRG_V2);
        registerMappingFormat(PROGUARD);
        registerMappingFormat(TINY_V1);
        registerMappingFormat(TINY_V2);
        registerMappingFormat(PDME);
    }

    public static boolean registerMappingFormat(MappingFormat<?, ?> format) {
        return MAPPING_FORMATS.putIfAbsent(Objects.requireNonNull(format.getName()), format) == null;
    }

    public static MappingFormat<?, ?> get(String name) {
        return MAPPING_FORMATS.get(name);
    }

    public static Set<String> getFormatNames() {
        return ObjectSets.unmodifiable(MAPPING_FORMATS.keySet());
    }
}

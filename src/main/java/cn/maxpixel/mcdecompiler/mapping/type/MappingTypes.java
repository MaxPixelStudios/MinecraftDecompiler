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

package cn.maxpixel.mcdecompiler.mapping.type;

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.reader.MappingProcessor;
import cn.maxpixel.mcdecompiler.reader.MappingProcessors;
import cn.maxpixel.mcdecompiler.writer.MappingGenerator;
import cn.maxpixel.mcdecompiler.writer.MappingGenerators;

public interface MappingTypes {
    MappingType.Classified<PairedMapping> SRG = new MappingType.Classified<>() {
        @Override
        public boolean supportPackage() {
            return true;
        }

        @Override
        public MappingProcessor.Classified<PairedMapping> getProcessor() {
            return MappingProcessors.SRG;
        }

        @Override
        public MappingGenerator.Classified<PairedMapping> getGenerator() {
            return MappingGenerators.SRG;
        }
    };

    MappingType.Classified<PairedMapping> CSRG = new MappingType.Classified<>() {
        @Override
        public boolean supportPackage() {
            return true;
        }

        @Override
        public MappingProcessor.Classified<PairedMapping> getProcessor() {
            return MappingProcessors.CSRG;
        }

        @Override
        public MappingGenerator.Classified<PairedMapping> getGenerator() {
            return MappingGenerators.CSRG;
        }
    };

    MappingType.Classified<PairedMapping> TSRG_V1 = new MappingType.Classified<>() {
        @Override
        public boolean supportPackage() {
            return true;
        }

        @Override
        public MappingProcessor.Classified<PairedMapping> getProcessor() {
            return MappingProcessors.TSRG_V1;
        }

        @Override
        public MappingGenerator.Classified<PairedMapping> getGenerator() {
            return MappingGenerators.TSRG_V1;
        }
    };

    MappingType.Classified<NamespacedMapping> TSRG_V2 = new MappingType.Classified<>() {
        @Override
        public boolean isNamespaced() {
            return true;
        }

        @Override
        public boolean supportPackage() {
            return true;
        }

        @Override
        public MappingProcessor.Classified<NamespacedMapping> getProcessor() {
            return MappingProcessors.TSRG_V2;
        }

        @Override
        public MappingGenerator.Classified<NamespacedMapping> getGenerator() {
            return MappingGenerators.TSRG_V2;
        }
    };

    MappingType.Classified<PairedMapping> PROGUARD = new MappingType.Classified<>() {
        @Override
        public MappingProcessor.Classified<PairedMapping> getProcessor() {
            return MappingProcessors.PROGUARD;
        }

        @Override
        public MappingGenerator.Classified<PairedMapping> getGenerator() {
            return MappingGenerators.PROGUARD;
        }
    };

    MappingType.Classified<NamespacedMapping> TINY_V1 = new MappingType.Classified<>() {
        @Override
        public boolean isNamespaced() {
            return true;
        }

        @Override
        public MappingProcessor.Classified<NamespacedMapping> getProcessor() {
            return MappingProcessors.TINY_V1;
        }

        @Override
        public MappingGenerator.Classified<NamespacedMapping> getGenerator() {
            return MappingGenerators.TINY_V1;
        }
    };

    MappingType.Classified<NamespacedMapping> TINY_V2 = new MappingType.Classified<>() {
        @Override
        public boolean isNamespaced() {
            return true;
        }

        @Override
        public MappingProcessor.Classified<NamespacedMapping> getProcessor() {
            return MappingProcessors.TINY_V2;
        }

        @Override
        public MappingGenerator.Classified<NamespacedMapping> getGenerator() {
            return MappingGenerators.TINY_V2;
        }
    };
}
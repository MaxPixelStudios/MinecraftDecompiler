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

package cn.maxpixel.mcdecompiler.mapping.processor;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.util.ContentList;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import cn.maxpixel.mcdecompiler.mapping.util.NamingUtil;

import java.util.function.Consumer;

public enum ProguardMappingProcessor implements MappingProcessor.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.PROGUARD;
    }

    @Override
    public ClassifiedMapping<PairedMapping> process(ContentList contents) {
        ClassifiedMapping<PairedMapping> mappings = new ClassifiedMapping<>();
        for (var content : contents) {
            try (var lines = preprocess(content.lines().map(this::stripComments))) {
                lines.forEach(new Consumer<>() {
                    private ClassMapping<PairedMapping> currentClass;

                    @Override
                    public void accept(String s) {
                        if (s.startsWith("    ")) {
                            processTree(s, currentClass);
                        } else {
                            int splitIndex = s.indexOf(" -> ");
                            if (splitIndex <= 0) error();
                            ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(
                                    NamingUtil.asNativeName(s.substring(splitIndex + 4, s.length() - 1)),
                                    NamingUtil.asNativeName(s.substring(0, splitIndex))
                            ));
                            mappings.classes.add(classMapping);
                            currentClass = classMapping;
                        }
                    }
                });
            }
        }
        return mappings;
    }

    private static void processTree(String s, ClassMapping<PairedMapping> classMapping) {
        if (s.contains("(") && s.contains(")")) {
            int lineNum = s.indexOf(':');
            int leftBracket = s.indexOf('(');
            int rightBracket = s.lastIndexOf(')');
            StringBuilder descriptor = new StringBuilder("(");
            int prev = leftBracket;
            for (int next = s.indexOf(',', prev + 1); next > 0;
                 prev = next, next = s.indexOf(',', prev + 1)) {
                descriptor.append(NamingUtil.java2Descriptor(s.substring(prev + 1, next)));
            }
            if (rightBracket - 1 != leftBracket) descriptor.append(NamingUtil.java2Descriptor(s.substring(prev + 1, rightBracket)));
            if (lineNum > 0) {
                int split1 = s.indexOf(' ', 11);// skip leading 4 spaces, descriptor name(at least 3 chars), and line number(at least 4 chars)
                if (split1 < 0) error();
                int lineNum1 = s.indexOf(':', lineNum + 2);
                if (lineNum1 < 0) error();
                classMapping.addMethod(MappingUtils.Paired.ldmo(s.substring(rightBracket + 5), s.substring(split1 + 1, leftBracket),
                        descriptor.append(')').append(NamingUtil.java2Descriptor(s.substring(lineNum1 + 1, split1))).toString(),
                        Integer.parseInt(s.substring(4, lineNum)), Integer.parseInt(s.substring(lineNum + 1, lineNum1))));
            } else { // no line number
                int split1 = s.indexOf(' ', 7);// skip leading 4 spaces and descriptor name/line number(at least 3 chars)
                if (split1 < 0) error();
                classMapping.addMethod(MappingUtils.Paired.dmo(s.substring(rightBracket + 5), s.substring(split1 + 1, leftBracket),
                        descriptor.append(')').append(NamingUtil.java2Descriptor(s.substring(4, split1))).toString()));
            }
        } else {
            int split1 = s.indexOf(' ', 7);// skip leading 4 spaces and descriptor name(at least 3 chars)
            if (split1 < 0) error();
            int split2 = s.indexOf(" -> ", split1 + 2);// skip split1(1 char) and mapped name(at least 1 char)
            if (split2 < 0) error();
            classMapping.addField(MappingUtils.Paired.dmo(s.substring(split2 + 4),
                    s.substring(split1 + 1, split2), NamingUtil.java2Descriptor(s.substring(4, split1))));
        }
    }

    private static void error() {
        throw new IllegalArgumentException("Is this Proguard mapping format?");
    }
}
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

package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.common.util.NamingUtil;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.*;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import cn.maxpixel.mcdecompiler.mapping.util.TinyUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public interface MappingGenerators {
    MappingGenerator.Classified<PairedMapping> SRG = new MappingGenerator.Classified<>() {
        @Override
        public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
            return MappingFormats.SRG;
        }

        @Override
        public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.classes.isEmpty() && mappings.packages.isEmpty()) return lines;
            mappings.classes.parallelStream().forEach(cls -> {
                PairedMapping classMapping = cls.mapping;
                synchronized (lines) {
                    lines.add("CL: " + classMapping.unmappedName + " " + classMapping.mappedName);
                }
                cls.getFields().parallelStream().forEach(field -> {
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    synchronized (lines) {
                        lines.add("FD: " + classMapping.unmappedName + '/' + field.unmappedName + ' ' +
                                classMapping.mappedName + '/' + field.mappedName);
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    MappingUtil.checkOwner(method.getOwned(), cls);
                    String unmappedDesc, mappedDesc;
                    if (method.hasComponent(Descriptor.class)) {
                        unmappedDesc = method.getComponent(Descriptor.class).unmappedDescriptor;
                        if (method.hasComponent(Descriptor.Mapped.class))
                            mappedDesc = method.getComponent(Descriptor.Mapped.class).mappedDescriptor;
                        else if (remapper != null) mappedDesc = remapper.mapMethodDesc(unmappedDesc);
                        else throw new UnsupportedOperationException();
                    } else if (method.hasComponent(Descriptor.Mapped.class)) {
                        mappedDesc = method.getComponent(Descriptor.Mapped.class).mappedDescriptor;
                        if (remapper != null) unmappedDesc = remapper.unmapMethodDesc(mappedDesc);
                        else throw new UnsupportedOperationException();
                    } else throw new UnsupportedOperationException();
                    synchronized (lines) {
                        lines.add("MD: " + classMapping.unmappedName + '/' + method.unmappedName + ' ' + unmappedDesc + ' ' +
                                classMapping.mappedName + '/' + method.mappedName + ' ' + mappedDesc);
                    }
                });
            });
            mappings.packages.parallelStream().forEach(pkg -> {
                synchronized(lines) {
                    lines.add("PK: " + pkg.unmappedName + ' ' + pkg.mappedName);
                }
            });
            return lines;
        }
    };

    MappingGenerator.Classified<PairedMapping> CSRG = new MappingGenerator.Classified<>() {
        @Override
        public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
            return MappingFormats.CSRG;
        }

        @Override
        public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.classes.isEmpty() && mappings.packages.isEmpty()) return lines;
            mappings.classes.parallelStream().forEach(cls -> {
                PairedMapping classMapping = cls.mapping;
                synchronized (lines) {
                    lines.add(classMapping.unmappedName + ' ' + classMapping.mappedName);
                }
                cls.getFields().parallelStream().forEach(field -> {
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    synchronized (lines) {
                        lines.add(classMapping.unmappedName + ' ' + field.unmappedName + ' ' + field.mappedName);
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    String unmappedDesc = MappingUtil.Paired.checkSlimSrgMethod(cls, method, remapper);
                    synchronized (lines) {
                        lines.add(classMapping.unmappedName + ' ' + method.unmappedName + ' ' +
                                unmappedDesc + ' ' + method.mappedName);
                    }
                });
            });
            mappings.packages.parallelStream().forEach(pkg -> {
                synchronized (lines) {
                    lines.add(pkg.unmappedName + "/ " + pkg.mappedName + '/');
                }
            });
            return lines;
        }
    };

    MappingGenerator.Classified<PairedMapping> TSRG_V1 = new MappingGenerator.Classified<>() {
        @Override
        public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
            return MappingFormats.TSRG_V1;
        }

        @Override
        public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.classes.isEmpty() && mappings.packages.isEmpty()) return lines;
            for (ClassMapping<PairedMapping> cls : mappings.classes) {
                lines.add(cls.mapping.unmappedName + ' ' + cls.mapping.mappedName);
                cls.getFields().parallelStream().forEach(field -> {
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    synchronized (lines) {
                        lines.add('\t' + field.unmappedName + ' ' + field.mappedName);
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    String unmappedDesc = MappingUtil.Paired.checkSlimSrgMethod(cls, method, remapper);
                    synchronized (lines) {
                        lines.add('\t' + method.unmappedName + ' ' + unmappedDesc + ' ' + method.mappedName);
                    }
                });
            }
            mappings.packages.parallelStream().forEach(pkg -> {
                synchronized (lines) {
                    lines.add(pkg.unmappedName + "/ " + pkg.mappedName + '/');
                }
            });
            return lines;
        }

    };

    MappingGenerator.Classified<NamespacedMapping> TSRG_V2 = new MappingGenerator.Classified<>() {
        @Override
        public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
            return MappingFormats.TSRG_V2;
        }

        @Override
        public ObjectList<String> generate(ClassifiedMapping<NamespacedMapping> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.classes.isEmpty() && mappings.packages.isEmpty()) return lines;
            var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
            String namespace0 = namespaces.first();
            lines.add("tsrg2 " + String.join(" ", namespaces));
            for (ClassMapping<NamespacedMapping> cls : mappings.classes) {
                lines.add(NamingUtil.concatNamespaces(namespaces, cls.mapping::getName, " "));
                cls.getFields().parallelStream().forEach(field -> {
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    String names = NamingUtil.concatNamespaces(namespaces, field::getName, " ");
                    if (field.hasComponent(Descriptor.Namespaced.class)) synchronized (lines) {
                        genDescriptorLine(lines, namespace0, field, names);
                    } else synchronized (lines) {
                        lines.add('\t' + names);
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    if (!method.hasComponent(Descriptor.Namespaced.class)) throw new UnsupportedOperationException();
                    MappingUtil.checkOwner(method.getOwned(), cls);
                    synchronized (lines) {
                        genDescriptorLine(lines, namespace0, method, NamingUtil.concatNamespaces(namespaces,
                                method::getName, " "));
                        var si = method.getComponent(StaticIdentifiable.class);
                        if (si != null && si.isStatic) lines.add("\t\tstatic");
                        if (method.hasComponent(LocalVariableTable.Namespaced.class)) {
                            LocalVariableTable.Namespaced lvt = method.getComponent(LocalVariableTable.Namespaced.class);
                            lvt.getLocalVariableIndexes().forEach(index -> {
                                String names = NamingUtil.concatNamespaces(namespaces, namespace -> {
                                    String name = lvt.getLocalVariable(index).getName(namespace);
                                    if(name != null && name.isBlank()) return "o";
                                    return name;
                                }, " ");
                                lines.add("\t\t" + index + ' ' + names);
                            });
                        }
                    }
                });
            }
            mappings.packages.parallelStream().forEach(pkg -> {
                synchronized (lines) {
                    lines.add(NamingUtil.concatNamespaces(namespaces, pkg::getName, " "));
                }
            });
            return lines;
        }

        private static void genDescriptorLine(ObjectArrayList<String> lines, String namespace0, NamespacedMapping method, String names) {
            Descriptor.Namespaced desc = method.getComponent(Descriptor.Namespaced.class);
            if (!namespace0.equals(desc.descriptorNamespace)) throw new IllegalArgumentException();
            int i = names.indexOf(' ');
            lines.add('\t' + names.substring(0, i + 1) + desc.unmappedDescriptor + names.substring(i));
        }
    };

    MappingGenerator.Classified<PairedMapping> PROGUARD = new MappingGenerator.Classified<>() {
        @Override
        public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
            return MappingFormats.PROGUARD;
        }

        @Override
        public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.classes.isEmpty()) return lines;
            for (ClassMapping<PairedMapping> cls : mappings.classes) {
                PairedMapping mapping = cls.mapping;
                lines.add(NamingUtil.asJavaName(mapping.mappedName) + " -> " +
                        NamingUtil.asJavaName(mapping.unmappedName) + ':');
                cls.getFields().parallelStream().forEach(field -> {
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    String mappedDesc;
                    if (field.hasComponent(Descriptor.Mapped.class)) {
                        mappedDesc = field.getComponent(Descriptor.Mapped.class).mappedDescriptor;
                    } else if (remapper != null && field.hasComponent(Descriptor.class)) {
                        mappedDesc = remapper.mapDesc(field.getComponent(Descriptor.class).unmappedDescriptor);
                    } else throw new UnsupportedOperationException();
                    synchronized (lines) {
                        lines.add("    " + NamingUtil.descriptor2Java(mappedDesc) + ' ' + field.mappedName +
                                " -> " + field.unmappedName);
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    MappingUtil.checkOwner(method.getOwned(), cls);
                    String mappedDesc;
                    if (method.hasComponent(Descriptor.Mapped.class)) {
                        mappedDesc = method.getComponent(Descriptor.Mapped.class).mappedDescriptor;
                    } else if (remapper != null && method.hasComponent(Descriptor.class)) {
                        mappedDesc = remapper.mapMethodDesc(method.getComponent(Descriptor.class).unmappedDescriptor);
                    } else throw new UnsupportedOperationException();
//                    String args = String.join(",", Utils.mapArray(Type.getArgumentTypes(mappedDesc),
//                            String[]::new, Type::getClassName));
                    StringBuilder args = new StringBuilder(mappedDesc.length());
                    int end = mappedDesc.lastIndexOf(')'), last = 1;
                    for (int i = 1; i < end; i++) {
                        char c = mappedDesc.charAt(i);
                        if (c != '[') {
                            if (c == 'L') i = mappedDesc.indexOf(';', i);
                            args.append(NamingUtil.descriptor2Java(mappedDesc.substring(last, last = i + 1))).append(',');
                        }
                    }
                    args.deleteCharAt(args.length() - 1);
                    String ret = NamingUtil.descriptor2Java(mappedDesc.substring(end + 1));
                    if (method.hasComponent(LineNumber.class)) {
                        LineNumber lineNumber = method.getComponent(LineNumber.class);
                        synchronized (lines) {
                            lines.add("    " + lineNumber.startLineNumber + ':' + lineNumber.endLineNumber + ':' +
                                    ret + ' ' + method.mappedName + '(' + args + ") -> " + method.unmappedName);
                        }
                    } else synchronized (lines) {
                        lines.add("    " + ret + ' ' + method.mappedName + '(' + args + ") -> " + method.unmappedName);
                    }
                });
            }
            return lines;
        }
    };

    MappingGenerator.Classified<NamespacedMapping> TINY_V1 = new MappingGenerator.Classified<>() {
        @Override
        public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
            return MappingFormats.TINY_V1;
        }

        @Override
        public ObjectList<String> generate(ClassifiedMapping<NamespacedMapping> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.classes.isEmpty()) return lines;
            var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
            String namespace0 = namespaces.first();
            lines.add("v1\t" + String.join("\t", namespaces));
            mappings.classes.parallelStream().forEach(cls -> {
                NamespacedMapping classMapping = cls.mapping;
                synchronized (lines) {
                    lines.add("CLASS\t" + NamingUtil.concatNamespaces(namespaces, classMapping::getName, "\t"));
                }
                cls.getFields().parallelStream().forEach(field -> {
                    String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, field);
                    synchronized (lines) {
                        lines.add("FIELD\t" + classMapping.getName(namespace0) + '\t' + desc + '\t' +
                                NamingUtil.concatNamespaces(namespaces, field::getName, "\t"));
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, method);
                    synchronized (lines) {
                        lines.add("METHOD\t" + classMapping.getName(namespace0) + '\t' + desc + '\t' +
                                NamingUtil.concatNamespaces(namespaces, method::getName, "\t"));
                    }
                });
            });
            return lines;
        }
    };

    MappingGenerator.Classified<NamespacedMapping> TINY_V2 = new MappingGenerator.Classified<>() {
        @Override
        public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
            return MappingFormats.TINY_V2;
        }

        @Override
        public ObjectList<String> generate(ClassifiedMapping<NamespacedMapping> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.classes.isEmpty()) return lines;
            var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
            String namespace0 = namespaces.first();
            lines.add("tiny\t2\t0\t" + String.join("\t", namespaces));
            for (ClassMapping<NamespacedMapping> cls : mappings.classes) {
                lines.add("c\t" + NamingUtil.concatNamespaces(namespaces, cls.mapping::getName, "\t"));
                cls.getFields().parallelStream().forEach(field -> {
                    String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, field);
                    synchronized (lines) {
                        lines.add("\tf\t" + desc + '\t' + NamingUtil.concatNamespaces(namespaces, field::getName, "\t"));
                        if (field.hasComponent(Documented.class)) {
                            String doc = field.getComponent(Documented.class).getContentString();
                            if (!doc.isBlank()) lines.add("\t\tc\t" + TinyUtil.escape(doc));
                        }
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, method);
                    synchronized (lines) {
                        lines.add("\tm\t" + desc + '\t' + NamingUtil.concatNamespaces(namespaces, method::getName, "\t"));
                        if (method.hasComponent(Documented.class)) {
                            String doc = method.getComponent(Documented.class).getContentString();
                            if (!doc.isBlank()) lines.add("\t\tc\t" + TinyUtil.escape(doc));
                        }
                        if (method.hasComponent(LocalVariableTable.Namespaced.class)) {
                            LocalVariableTable.Namespaced lvt = method.getComponent(LocalVariableTable.Namespaced.class);
                            boolean omittedThis = method.hasComponent(StaticIdentifiable.class) &&
                                    !method.getComponent(StaticIdentifiable.class).isStatic;
                            lvt.getLocalVariableIndexes().forEach(index -> {
                                NamespacedMapping localVariable = lvt.getLocalVariable(omittedThis ? index + 1 : index);
                                String names = NamingUtil.concatNamespaces(namespaces, namespace -> {
                                    String name = localVariable.getName(namespace);
                                    if(name == null || name.isBlank()) return "";
                                    return name;
                                }, "\t");
                                lines.add("\t\tp\t" + index + '\t' + names);
                                if (localVariable.hasComponent(Documented.class)) {
                                    String doc = localVariable.getComponent(Documented.class).getContentString();
                                    if (!doc.isBlank()) lines.add("\t\t\tc\t" + TinyUtil.escape(doc));
                                }
                            });
                        }
                    }
                });
            }
            return lines;
        }
    };

    MappingGenerator.Classified<PairedMapping> PDME = new MappingGenerator.Classified<>() {
        @Override
        public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
            return MappingFormats.PDME;
        }

        @Override
        public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.classes.isEmpty()) return lines;
            mappings.classes.parallelStream().forEach(cls -> {
                PairedMapping classMapping = cls.mapping;
                String clazz = classMapping.getUnmappedName().replace("/", ".");
                String mapped_class = classMapping.getMappedName().replace("/", ".");
                synchronized (lines) {
                    String classdoc = "";
                    String parsed_mapped_class; //Only have the name of the subclass by default
                    if (classMapping.hasComponent(Documented.class)) {
                        classdoc = classMapping.getComponent(Documented.class).getContentString();
                    }
                    if (mapped_class.contains("$")) {
                        String[] clz = mapped_class.split("\\$");
                        parsed_mapped_class = clz[clz.length - 1];
                    } else {
                        parsed_mapped_class = mapped_class;
                    }
                    lines.add(
                            "Class¶" + clazz + '¶' + parsed_mapped_class + "¶nil¶nil¶" + classdoc
                    );
                }
                cls.getFields().parallelStream().forEach(field -> {
                    String desc = field.getComponent(Descriptor.class).unmappedDescriptor;
                    String unmapped = clazz.replace("/", ".") + '.' + field.getUnmappedName() + ":" + desc;
                    String doc = "";
                    if (field.hasComponent(Documented.class)) {
                        doc = field.getComponent(Documented.class).getContentString();
                    }
                    synchronized (lines) {
                        lines.add(
                                "Var¶" + unmapped + '¶' + field.getMappedName() + "¶nil¶nil¶" + doc
                        );
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    String desc = method.getComponent(Descriptor.class).unmappedDescriptor;
                    String unmapped = clazz.replace("/", ".") + '.' + method.getUnmappedName() + desc;
                    String doc = "";
                    if (method.hasComponent(Documented.class)) {
                        doc = method.getComponent(Documented.class).getContentString();
                    }
                    synchronized (lines) {
                        lines.add(
                                "Def¶" + unmapped + '¶' + method.getMappedName() + "¶nil¶nil¶" + doc
                        );
                    }
                    if (method.hasComponent(LocalVariableTable.Paired.class)) {
                        LocalVariableTable.Paired lvt = method.getComponent(LocalVariableTable.Paired.class);
                        lvt.getLocalVariableIndexes().forEach(index -> {
                            String loc_unmapped = "nil";
                            PairedMapping loc = lvt.getLocalVariable(index);
                            String loc_doc = "";
                            if (!loc.getUnmappedName().equals(unmapped + "@" + index)) {
                                loc_unmapped = loc.getUnmappedName();
                            }
                            if (loc.hasComponent(Documented.class)) {
                                loc_doc = loc.getComponent(Documented.class).getContentString();
                            }
                            lines.add("Param¶" + loc_unmapped + "¶" + loc.getMappedName() + "¶" + unmapped + "¶" + index + "¶" + loc_doc);
                        });
                    }
                });
            });
            return lines;
        }
    };
}
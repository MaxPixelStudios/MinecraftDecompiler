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

package cn.maxpixel.mcdecompiler.writer;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.component.*;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;
import cn.maxpixel.mcdecompiler.mapping.type.MappingTypes;
import cn.maxpixel.mcdecompiler.util.MappingUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.objectweb.asm.Type;

public final class MappingGenerators {
    private MappingGenerators() {}

    public static final MappingGenerator.Classified<PairedMapping> SRG = new MappingGenerator.Classified<>() {
        @Override
        public MappingType<PairedMapping, ObjectList<ClassMapping<PairedMapping>>> getType() {
            return MappingTypes.SRG;
        }

        @Override
        public ObjectList<String> generate(ObjectList<ClassMapping<PairedMapping>> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.isEmpty()) return lines;
            mappings.parallelStream().forEach(cls -> {
                PairedMapping classMapping = cls.mapping;
                synchronized(lines) {
                    lines.add("CL: " + classMapping.getUnmappedName() + " " + classMapping.getMappedName());
                }
                cls.getFields().parallelStream().forEach(field -> {
                    if(!field.hasComponent(Owned.class)) throw new UnsupportedOperationException();
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    synchronized(lines) {
                        lines.add("FD: " + classMapping.getUnmappedName() + '/' + field.getUnmappedName() + ' ' +
                                classMapping.getMappedName() + '/' + field.getMappedName());
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    if(!method.hasComponent(Owned.class)) throw new UnsupportedOperationException();
                    MappingUtil.checkOwner(method.getOwned(), cls);
                    String unmappedDesc, mappedDesc;
                    if(method.hasComponent(Descriptor.class)) {
                        unmappedDesc = method.getComponent(Descriptor.class).getUnmappedDescriptor();
                        if(method.hasComponent(Descriptor.Mapped.class))
                            mappedDesc = method.getComponent(Descriptor.Mapped.class).getMappedDescriptor();
                        else if(remapper != null) mappedDesc = remapper.getMappedDescByUnmappedDesc(unmappedDesc);
                        else throw new UnsupportedOperationException();
                    } else if(method.hasComponent(Descriptor.Mapped.class)) {
                        mappedDesc = method.getComponent(Descriptor.Mapped.class).getMappedDescriptor();
                        if(remapper != null) unmappedDesc = remapper.getUnmappedDescByMappedDesc(mappedDesc);
                        else throw new UnsupportedOperationException();
                    } else throw new UnsupportedOperationException();
                    synchronized(lines) {
                        lines.add("MD: " + classMapping.getUnmappedName() + '/' + method.getUnmappedName() + ' ' + unmappedDesc + ' ' +
                                classMapping.getMappedName() + '/' + method.getMappedName() + ' ' + mappedDesc);
                    }
                });
            });
            return lines;
        }

        @Override
        public ObjectList<String> generatePackages(ObjectList<PairedMapping> packages) {
            if (packages.isEmpty()) return ObjectLists.emptyList();
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            packages.parallelStream().forEach(pkg -> {
                synchronized(lines) {
                    lines.add("PK: " + pkg.getUnmappedName() + ' ' + pkg.getMappedName());
                }
            });
            return lines;
        }
    };

    public static final MappingGenerator.Classified<PairedMapping> CSRG = new MappingGenerator.Classified<>() {
        @Override
        public MappingType<PairedMapping, ObjectList<ClassMapping<PairedMapping>>> getType() {
            return MappingTypes.CSRG;
        }

        @Override
        public ObjectList<String> generate(ObjectList<ClassMapping<PairedMapping>> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.isEmpty()) return lines;
            mappings.parallelStream().forEach(cls -> {
                PairedMapping classMapping = cls.mapping;
                synchronized(lines) {
                    lines.add(classMapping.getUnmappedName() + ' ' + classMapping.getMappedName());
                }
                cls.getFields().parallelStream().forEach(field -> {
                    if(!field.hasComponent(Owned.class)) throw new UnsupportedOperationException();
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    synchronized(lines) {
                        lines.add(classMapping.getUnmappedName() + ' ' + field.getUnmappedName() + ' ' + field.getMappedName());
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    String unmappedDesc = MappingUtil.Paired.checkSlimSrgMethod(cls, method, remapper);
                    synchronized(lines) {
                        lines.add(classMapping.getUnmappedName() + ' ' + method.getUnmappedName() + ' ' +
                                unmappedDesc + ' ' + method.getMappedName());
                    }
                });
            });
            return lines;
        }

        @Override
        public ObjectList<String> generatePackages(ObjectList<PairedMapping> packages) {
            if (packages.isEmpty()) return ObjectLists.emptyList();
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            packages.parallelStream().forEach(pkg -> {
                synchronized(lines) {
                    lines.add(pkg.getUnmappedName() + "/ " + pkg.getMappedName() + '/');
                }
            });
            return lines;
        }
    };

    public static final MappingGenerator.Classified<PairedMapping> TSRG_V1 = new MappingGenerator.Classified<>() {
        @Override
        public MappingType<PairedMapping, ObjectList<ClassMapping<PairedMapping>>> getType() {
            return MappingTypes.TSRG_V1;
        }

        @Override
        public ObjectList<String> generate(ObjectList<ClassMapping<PairedMapping>> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.isEmpty()) return lines;
            mappings.forEach(cls -> {
                lines.add(cls.mapping.getUnmappedName() + ' ' + cls.mapping.getMappedName());
                cls.getFields().parallelStream().forEach(field -> {
                    if(!field.hasComponent(Owned.class)) throw new UnsupportedOperationException();
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    synchronized(lines) {
                        lines.add('\t' + field.getUnmappedName() + ' ' + field.getMappedName());
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    String unmappedDesc = MappingUtil.Paired.checkSlimSrgMethod(cls, method, remapper);
                    synchronized(lines) {
                        lines.add('\t' + method.getUnmappedName() + ' ' + unmappedDesc + ' ' + method.getMappedName());
                    }
                });
            });
            return lines;
        }

        @Override
        public ObjectList<String> generatePackages(ObjectList<PairedMapping> packages) {
            if (packages.isEmpty()) return ObjectLists.emptyList();
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            packages.parallelStream().forEach(pkg -> {
                synchronized(lines) {
                    lines.add(pkg.getUnmappedName() + "/ " + pkg.getMappedName() + '/');
                }
            });
            return lines;
        }
    };

    public static final MappingGenerator.Classified<NamespacedMapping> TSRG_V2 = new MappingGenerator.Classified<>() {
        @Override
        public MappingType<NamespacedMapping, ObjectList<ClassMapping<NamespacedMapping>>> getType() {
            return MappingTypes.TSRG_V2;
        }

        @Override
        public ObjectList<String> generate(ObjectList<ClassMapping<NamespacedMapping>> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.isEmpty()) return lines;
            ObjectSet<String> namespaces = mappings.get(0).mapping.getNamespaces();
            String namespace0 = namespaces.iterator().next();
            lines.add("tsrg2 " + String.join(" ", namespaces));
            mappings.forEach(cls -> {
                lines.add(NamingUtil.concatNamespaces(namespaces, cls.mapping::getName, " "));
                cls.getFields().parallelStream().forEach(field -> {
                    if(!field.hasComponent(Owned.class)) throw new UnsupportedOperationException();
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    String names = NamingUtil.concatNamespaces(namespaces, field::getName, " ");
                    if(field.hasComponent(Descriptor.Namespaced.class)) synchronized(lines) {
                        genDescriptorLine(lines, namespace0, field, names);
                    } else synchronized(lines) {
                        lines.add('\t' + names);
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    if(!method.hasComponent(Owned.class) || !method.hasComponent(Descriptor.Namespaced.class) ||
                            !method.hasComponent(StaticIdentifiable.class))
                        throw new UnsupportedOperationException();
                    MappingUtil.checkOwner(method.getOwned(), cls);
                    synchronized(lines) {
                        genDescriptorLine(lines, namespace0, method, NamingUtil.concatNamespaces(namespaces,
                                method::getName, " "));
                        if(method.getComponent(StaticIdentifiable.class).isStatic) lines.add("\t\tstatic");
                        if(method.hasComponent(LocalVariableTable.Namespaced.class)) {
                            LocalVariableTable.Namespaced lvt = method.getComponent(LocalVariableTable.Namespaced.class);
                            lvt.getLocalVariableIndexes().forEach(index -> {
                                String names = NamingUtil.concatNamespaces(namespaces, namespace -> {
                                    String name = lvt.getLocalVariableName(index, namespace);
                                    if(name != null && name.isBlank()) return "o";
                                    return name;
                                }, " ");
                                lines.add("\t\t" + index + ' ' + names);
                            });
                        }
                    }
                });
            });
            return lines;
        }

        private static void genDescriptorLine(ObjectArrayList<String> lines, String namespace0, NamespacedMapping method, String names) {
            Descriptor.Namespaced desc = method.getComponent(Descriptor.Namespaced.class);
            if(!namespace0.equals(desc.getDescriptorNamespace())) throw new IllegalArgumentException();
            int i = names.indexOf(' ');
            lines.add('\t' + names.substring(0, i + 1) + desc.getUnmappedDescriptor() + names.substring(i));
        }

        @Override
        public ObjectList<String> generatePackages(ObjectList<NamespacedMapping> packages) {
            if (packages.isEmpty()) return ObjectLists.emptyList();
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            ObjectSet<String> namespaces = packages.get(0).getNamespaces();
            packages.parallelStream().forEach(pkg -> {
                synchronized(lines) {
                    lines.add(NamingUtil.concatNamespaces(namespaces, pkg::getName, " "));
                }
            });
            return lines;
        }
    };

    public static final MappingGenerator.Classified<PairedMapping> PROGUARD = new MappingGenerator.Classified<>() {
        @Override
        public MappingType<PairedMapping, ObjectList<ClassMapping<PairedMapping>>> getType() {
            return MappingTypes.PROGUARD;
        }

        @Override
        public ObjectList<String> generate(ObjectList<ClassMapping<PairedMapping>> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.isEmpty()) return lines;
            mappings.forEach(cls -> {
                PairedMapping mapping = cls.mapping;
                lines.add(NamingUtil.asJavaName(mapping.getMappedName()) + " -> " +
                        NamingUtil.asJavaName(mapping.getUnmappedName()) + ':');
                cls.getFields().parallelStream().forEach(field -> {
                    if(!field.hasComponent(Owned.class)) throw new UnsupportedOperationException();
                    MappingUtil.checkOwner(field.getOwned(), cls);
                    String mappedDesc;
                    if(field.hasComponent(Descriptor.Mapped.class)) {
                        mappedDesc = field.getComponent(Descriptor.Mapped.class).getMappedDescriptor();
                    } else if(remapper != null && field.hasComponent(Descriptor.class)) {
                        mappedDesc = remapper.mapToMapped(Type.getType(field.getComponent(Descriptor.class)
                                .getUnmappedDescriptor()));
                    } else throw new UnsupportedOperationException();
                    synchronized(lines) {
                        lines.add("    " + Type.getType(mappedDesc).getClassName() + ' ' +
                                field.getMappedName() + " -> " + field.getUnmappedName());
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    if(!method.hasComponent(Owned.class)) throw new UnsupportedOperationException();
                    MappingUtil.checkOwner(method.getOwned(), cls);
                    String mappedDesc;
                    if(method.hasComponent(Descriptor.Mapped.class)) {
                        mappedDesc = method.getComponent(Descriptor.Mapped.class).getMappedDescriptor();
                    } else if(remapper != null && method.hasComponent(Descriptor.class)) {
                        mappedDesc = remapper.getMappedDescByUnmappedDesc(method.getComponent(Descriptor.class)
                                .getUnmappedDescriptor());
                    } else throw new UnsupportedOperationException();
                    String args = String.join(",", Utils.mapArray(Type.getArgumentTypes(mappedDesc),
                            String[]::new, Type::getClassName));
                    if(method.hasComponent(LineNumber.class)) {
                        LineNumber lineNumber = method.getComponent(LineNumber.class);
                        synchronized(lines) {
                            lines.add("    " + lineNumber.getStartLineNumber() + ':' + lineNumber.getEndLineNumber() + ':' +
                                    Type.getReturnType(mappedDesc).getClassName() + ' ' + method.getMappedName() + '(' +
                                    args + ") -> " + method.getUnmappedName());
                        }
                    } else synchronized(lines) {
                        lines.add("    " + Type.getReturnType(mappedDesc).getClassName() + ' ' + method.getMappedName() +
                                '(' + args + ") -> " + method.getUnmappedName());
                    }
                });
            });
            return lines;
        }
    };

    public static final MappingGenerator.Classified<NamespacedMapping> TINY_V1 = new MappingGenerator.Classified<>() {
        @Override
        public MappingType<NamespacedMapping, ObjectList<ClassMapping<NamespacedMapping>>> getType() {
            return MappingTypes.TINY_V1;
        }

        @Override
        public ObjectList<String> generate(ObjectList<ClassMapping<NamespacedMapping>> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.isEmpty()) return lines;
            ObjectSet<String> namespaces = mappings.get(0).mapping.getNamespaces();
            String namespace0 = namespaces.iterator().next();
            lines.add("v1\t" + String.join("\t", namespaces));
            mappings.parallelStream().forEach(cls -> {
                NamespacedMapping classMapping = cls.mapping;
                synchronized(lines) {
                    lines.add("CLASS\t" + NamingUtil.concatNamespaces(namespaces, classMapping::getName, "\t"));
                }
                cls.getFields().parallelStream().forEach(field -> {
                    String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, field);
                    synchronized(lines) {
                        lines.add("FIELD\t" + classMapping.getName(namespace0) + '\t' + desc + '\t' +
                                NamingUtil.concatNamespaces(namespaces, field::getName, "\t"));
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, method);
                    synchronized(lines) {
                        lines.add("METHOD\t" + classMapping.getName(namespace0) + '\t' + desc + '\t' +
                                NamingUtil.concatNamespaces(namespaces, method::getName, "\t"));
                    }
                });
            });
            return lines;
        }
    };

    public static final MappingGenerator.Classified<NamespacedMapping> TINY_V2 = new MappingGenerator.Classified<>() {
        @Override
        public MappingType<NamespacedMapping, ObjectList<ClassMapping<NamespacedMapping>>> getType() {
            return MappingTypes.TINY_V2;
        }

        @Override
        public ObjectList<String> generate(ObjectList<ClassMapping<NamespacedMapping>> mappings, ClassifiedMappingRemapper remapper) {
            ObjectArrayList<String> lines = new ObjectArrayList<>();
            if (mappings.isEmpty()) return lines;
            ObjectSet<String> namespaces = mappings.get(0).mapping.getNamespaces();
            String namespace0 = namespaces.iterator().next();
            lines.add("tiny\t2\t0\t" + String.join("\t", namespaces));
            mappings.forEach(cls -> {
                lines.add("c\t" + NamingUtil.concatNamespaces(namespaces, cls.mapping::getName, "\t"));
                cls.getFields().parallelStream().forEach(field -> {
                    String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, field);
                    synchronized(lines) {
                        lines.add("\tf\t" + desc + '\t' + NamingUtil.concatNamespaces(namespaces, field::getName, "\t"));
                        if(field.hasComponent(Documented.class)) {
                            String doc = field.getComponent(Documented.class).getDoc();
                            if(doc != null && !doc.isBlank()) lines.add("\t\tc\t" + doc);
                        }
                    }
                });
                cls.getMethods().parallelStream().forEach(method -> {
                    String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, method);
                    synchronized(lines) {
                        lines.add("\tm\t" + desc + '\t' + NamingUtil.concatNamespaces(namespaces, method::getName, "\t"));
                        if(method.hasComponent(Documented.class)) {
                            String doc = method.getComponent(Documented.class).getDoc();
                            if(doc != null && !doc.isBlank()) lines.add("\t\tc\t" + doc);
                        }
                        if(method.hasComponent(LocalVariableTable.Namespaced.class)) {
                            LocalVariableTable.Namespaced lvt = method.getComponent(LocalVariableTable.Namespaced.class);
                            boolean omittedThis = method.hasComponent(StaticIdentifiable.class) &&
                                    !method.getComponent(StaticIdentifiable.class).isStatic;
                            lvt.getLocalVariableIndexes().forEach(index -> {
                                String names = NamingUtil.concatNamespaces(namespaces, namespace -> {
                                    String name = lvt.getLocalVariableName(omittedThis ? index + 1 : index, namespace);
                                    if(name != null && name.isBlank()) return "";
                                    return name;
                                }, "\t");
                                lines.add("\t\tp\t" + index + '\t' + names);
                                if(method.hasComponent(Documented.LocalVariable.class)) {
                                    String doc = method.getComponent(Documented.LocalVariable.class).getLocalVariableDoc(index);
                                    if(doc != null && !doc.isBlank()) lines.add("\t\t\tc\t" + doc);
                                }
                            });
                        }
                    }
                });
            });
            return lines;
        }
    };
}
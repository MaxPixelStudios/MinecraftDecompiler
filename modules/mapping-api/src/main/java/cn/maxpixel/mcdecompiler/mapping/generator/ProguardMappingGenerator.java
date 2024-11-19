package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.common.util.NamingUtil;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.LineNumber;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum ProguardMappingGenerator implements MappingGenerator.Classified<PairedMapping> {
    INSTANCE;

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
}
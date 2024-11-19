package cn.maxpixel.mcdecompiler.mapping.processor;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum TsrgV1MappingProcessor implements MappingProcessor.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.TSRG_V1;
    }

    @Override
    public ClassifiedMapping<PairedMapping> process(ObjectList<String> content) {
        ClassifiedMapping<PairedMapping> mappings = new ClassifiedMapping<>();
        for (int i = 0, len = content.size(); i < len;) {
            String[] sa = MappingUtil.split(content.get(i), ' ');
            if (sa[0].charAt(0) != '\t') {
                if (sa[0].charAt(sa[0].length() - 1) == '/') {
                    mappings.packages.add(new PairedMapping(sa[0].substring(0, sa[0].length() - 1),
                            sa[1].substring(0, sa[1].length() - 1)));
                    i++;
                } else {
                    ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(sa[0], sa[1]));
                    i = processTree(i, len, content, classMapping);
                    mappings.classes.add(classMapping);
                }
            } else error();
        }
        return mappings;
    }

    private static int processTree(int index, int size, ObjectList<String> content, ClassMapping<PairedMapping> classMapping) {
        for (index = index + 1; index < size; index++) {
            String s = content.get(index);
            if (s.charAt(0) == '\t') {
                String[] sa = MappingUtil.split(s, ' ', 1);
                switch (sa.length) {
                    case 2 -> classMapping.addField(MappingUtil.Paired.o(sa[0], sa[1]));
                    case 3 -> classMapping.addMethod(MappingUtil.Paired.duo(sa[0], sa[2], sa[1]));
                    default -> error();
                }
            } else break;
        }
        return index;
    }

    private static void error() {
        throw new IllegalArgumentException("Is this a TSRG v1 mapping file?");
    }
}